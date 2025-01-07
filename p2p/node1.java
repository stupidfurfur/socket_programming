
public class node1 {
    public static void main(String[] args) {
        P2PNode node1 = new P2PNode(5000);
        node1.startServer();
        
        // Start chat with Node 2
        node1.startChat("localhost", 5001);

    }
}


