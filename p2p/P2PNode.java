import java.io.*;
import java.net.*;
import java.util.Scanner;

public class P2PNode {
    private int port;
    private ServerSocket serverSocket;

    public P2PNode(int port) {
        this.port = port;
    }

    public void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);  // Use the instance variable here
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    handleClient(clientSocket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleClient(Socket socket) {
        new Thread(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendMessage(String host, int port, String message) {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startChat(String otherHost, int otherPort) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter messages to send (type 'exit' to quit):");

        String message;
        while (true) {
            message = scanner.nextLine();
            if (message.equalsIgnoreCase("exit")) {
                break;
            }
            sendMessage(otherHost, otherPort, message);
        }
        scanner.close();
        stopServer();
    }

    public void stopServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("socket disconnected.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
