package Socket.practice;

import java.io.*;  
import java.net.*;  

public class MyClient {  
    public static void main(String args[]) throws Exception {  
        Socket s = new Socket("localhost", 3333);  
        DataInputStream din = new DataInputStream(s.getInputStream());  
        DataOutputStream dout = new DataOutputStream(s.getOutputStream());  
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));  

        // 接收消息的執行緒
        Thread receiveThread = new Thread(() -> {
            try {
                String str;
                while (true) {
                    str = din.readUTF();  // 讀取伺服器的回應
                    if (str.equals("stop")) {
                        System.out.println("Server has stopped communication.");
                        break;
                    }
                    System.out.println("Server says: " + str);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        receiveThread.start(); // 啟動接收消息的執行緒

        // 發送消息的主循環
        String str;
        while (true) {
            str = br.readLine();  // 讀取用戶輸入
            dout.writeUTF(str);  
            dout.flush();  
            if (str.equals("stop")) {
                break;
            }
        }

        // 關閉資源
        dout.writeUTF("stop");
        dout.flush();
        din.close();  
        dout.close();  
        s.close();  
    }
}
