import java.io.*;
import java.net.*;
import java.util.*;

public class ChatRoom {
    private static final Map<String, PrintWriter> clients = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) {
        System.out.println("Enter port:");
        try (Scanner scanner = new Scanner(System.in)) {
            int port = scanner.nextInt();
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private static void handleClient(Socket socket) {
        String clientName = null;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println("Welcome to the chat! Enter your name:");
            clientName = in.readLine();

            synchronized (clients) {
                while (clients.containsKey(clientName)) {
                    out.println("Name already taken. Choose another:");
                    clientName = in.readLine();
                }
                clients.put(clientName, out);
            }

            broadcast(clientName + " joined the chat. Active users: " + clients.size());

            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("/msg")) {
                    sendPrivateMessage(clientName, message);
                } else if (message.startsWith("/rename")) {
                    clientName = renameClient(clientName, message, out);
                } else if (message.startsWith("/exit")) {
                    break;
                } else {
                    broadcast(clientName + ": " + message);
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            if (clientName != null) {
                removeClient(clientName);
                broadcast(clientName + " left the chat. Active users: " + clients.size());
            }
        }
    }

    private static void broadcast(String message) {
        synchronized (clients) {
            for (PrintWriter writer : clients.values()) {
                writer.println(message);
            }
        }
    }

    private static void sendPrivateMessage(String sender, String message) {
        String[] parts = message.split(" ", 3);
        if (parts.length < 3) {
            return;
        }
        String recipient = parts[1];
        String privateMessage = parts[2];

        synchronized (clients) {
            PrintWriter writer = clients.get(recipient);
            if (writer != null) {
                writer.println("[Private] " + sender + ": " + privateMessage);
            }
        }
    }

    private static String renameClient(String oldName, String message, PrintWriter out) {
        String[] parts = message.split(" ", 2);
        if (parts.length < 2) {
            return oldName;
        }
        String newName = parts[1];

        synchronized (clients) {
            if (!clients.containsKey(newName)) {
                clients.remove(oldName);
                clients.put(newName, out);
                broadcast(oldName + " renamed to " + newName);
                return newName;
            } else {
                out.println("Name already taken. Choose another.");
                return oldName;
            }
        }
    }

    private static void removeClient(String name) {
        synchronized (clients) {
            clients.remove(name);
        }
    }
}

