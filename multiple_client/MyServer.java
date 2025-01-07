package Socket.multiple_client;

import java.io.*;  
import java.net.*;  
import java.util.concurrent.*;  
import java.util.*;  

public class MyServer {
    private static ExecutorService pool = Executors.newFixedThreadPool(10);
    private static List<DataOutputStream> clientOutputs = Collections.synchronizedList(new ArrayList<>());

    public static void main(String args[]) throws Exception {  
        ServerSocket ss = new ServerSocket(3333);  
        System.out.println("Server started, waiting for clients...");

        while (true) {
            Socket clientSocket = ss.accept();  
            System.out.println("Client connected: " + clientSocket.getInetAddress());
            pool.execute(new ClientHandler(clientSocket)); // 將每個客戶端交給執行緒池處理
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private DataInputStream din;
        private DataOutputStream dout;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            din = new DataInputStream(socket.getInputStream());
            dout = new DataOutputStream(socket.getOutputStream());
            clientOutputs.add(dout); // 將此客戶端的輸出流添加到列表中
        }

        @Override
        public void run() {
            try {
                // 接收消息的執行緒
                Thread receiveThread = new Thread(() -> {
                    String str; // 將 str 定義在這裡
                    try {
                        while (true) {
                            str = din.readUTF();  // 讀取客戶端消息
                            if (str.equals("stop")) {
                                System.out.println("Client has stopped communication.");
                                break;
                            }
                            System.out.println("Client says: " + str);
                            // 向所有客戶端發送消息
                            for (DataOutputStream out : clientOutputs) {
                                out.writeUTF("Client says: " + str);
                                out.flush();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        // 移除此客戶端的輸出流
                        clientOutputs.remove(dout);
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
                        for (DataOutputStream out : clientOutputs) {
                            out.writeUTF("Server is stopping.");
                            out.flush();
                        }
                        break;
                    }
                    // 將伺服器的輸入發送給所有客戶端
                    for (DataOutputStream out : clientOutputs) {
                        out.writeUTF("Server: " + serverInput);
                        out.flush();
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
