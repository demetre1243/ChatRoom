import java.io.*;
import java.net.*;
import java.util.*;

public class ChatUser {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter server address:");
        String serverAddress = scanner.nextLine();
        System.out.println("Enter port:");
        int port = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        try (Socket socket = new Socket(serverAddress, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println(in.readLine()); // Welcome message
            String userInput;

            while ((userInput = scanner.nextLine()) != null) {
                out.println(userInput);
                String serverResponse = in.readLine();
                if (serverResponse != null) {
                    System.out.println(serverResponse);
                }
            }
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
}

}



