package Socket.multiple_client;

import java.io.*;  
import java.net.*;  
import java.util.concurrent.*;  
import java.util.*;  

public class MyServer {
    private static ExecutorService pool = Executors.newFixedThreadPool(10);
    private static List<ClientHandler> clientHandlers = Collections.synchronizedList(new ArrayList<>());
    private static int clientIdCounter = 1;  // 用來生成客戶端ID

    public static void main(String args[]) throws Exception {  
        ServerSocket ss = new ServerSocket(3333);  
        System.out.println("Server started, waiting for clients...");

        while (true) {
            Socket clientSocket = ss.accept();  
            System.out.println("Client connected: " + clientSocket.getInetAddress());
            pool.execute(new ClientHandler(clientSocket, clientIdCounter++)); // 分配 ID 並交給執行緒池處理
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private DataInputStream din;
        private DataOutputStream dout;
        private int clientId;

        public ClientHandler(Socket socket, int clientId) throws IOException {
            this.socket = socket;
            this.clientId = clientId;
            din = new DataInputStream(socket.getInputStream());
            dout = new DataOutputStream(socket.getOutputStream());
            clientHandlers.add(this); // 將此客戶端的處理器加入列表
            dout.writeUTF("Your client ID: " + clientId);  // 向客戶端發送它的 ID
            dout.flush();
        }

        @Override
        public void run() {
            try {
                // 接收消息的執行緒
                Thread receiveThread = new Thread(() -> {
                    String str; 
                    try {
                        while (true) {
                            str = din.readUTF();  // 讀取客戶端消息
                            if (str.equals("stop")) {
                                System.out.println("Client " + clientId + " has stopped communication.");
                                break;
                            }
                            System.out.println("Client " + clientId + " says: " + str);
                            // 向所有客戶端發送消息
                            for (ClientHandler handler : clientHandlers) {
                                handler.dout.writeUTF("Client " + clientId + " says: " + str);
                                handler.dout.flush();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        // 移除此客戶端的處理器
                        clientHandlers.remove(this);
                        try {
                            din.close();
                            dout.close();
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                receiveThread.start(); // 啟動接收消息的執行緒

                // 伺服器的輸入循環
                BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
                String serverInput;
                while (true) {
                    serverInput = consoleReader.readLine();  // 讀取伺服器輸入
                    if (serverInput.equals("stop")) {
                        for (ClientHandler handler : clientHandlers) {
                            handler.dout.writeUTF("Server is stopping.");
                            handler.dout.flush();
                        }
                        break;
                    }
                    // 將伺服器的輸入發送給所有客戶端
                    for (ClientHandler handler : clientHandlers) {
                        handler.dout.writeUTF("Server: " + serverInput);
                        handler.dout.flush();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // 關閉資源
                try {
                    din.close();
                    dout.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
