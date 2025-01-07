public class node2 {
    public static void main(String[] args) {
        P2PNode node2 = new P2PNode(5001);
        node2.startServer();
        
        // Start chat with Node 1
        node2.startChat("localhost", 5000);
        
    }
}
