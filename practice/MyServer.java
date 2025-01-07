package Socket.practice;

import java.io.*;  
import java.net.*;  

public class MyServer {
    public static void main(String args[]) throws Exception {  
        ServerSocket ss = new ServerSocket(3333);  
        System.out.println("Server started, waiting for client...");

        Socket s = ss.accept();  
        System.out.println("Client connected.");

        DataInputStream din = new DataInputStream(s.getInputStream());  
        DataOutputStream dout = new DataOutputStream(s.getOutputStream());  
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));  

        // 接收消息的執行緒
        Thread receiveThread = new Thread(() -> {
            try {
                String str;
                while (true) {
                    str = din.readUTF();  // 讀取客戶端消息
                    if (str.equals("stop")) {
                        System.out.println("Client has stopped communication.");
                        break;
                    }
                    System.out.println("Client says: " + str);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        receiveThread.start(); // 啟動接收消息的執行緒

        // 發送消息的主循環
        String str2;
        while (true) {
            str2 = br.readLine();  // 伺服器輸入
            dout.writeUTF(str2);  
            dout.flush();  
            if (str2.equals("stop")) {
                break;
            }
        }

        // 關閉資源
        dout.writeUTF("stop");
        dout.flush();
        din.close();  
        dout.close();  
        s.close();  
        ss.close();  
    }
}
