import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    private Client() {}

    public static void main(String[] args) {

        int response = 0;
        PeerInfo initiatorPeer = new PeerInfo(args[0]);
        try {
            Registry registry = LocateRegistry.getRegistry(initiatorPeer.getHost(),initiatorPeer.getPort());
            ControlInterface stub = (ControlInterface) registry.lookup(initiatorPeer.getPeerId());
            if(args[1].equals("BACKUP")){
                response = stub.backup(args[2],args[3]);
            }else if (args[1].equals("DELETE")){
                response = stub.delete(args[2]);
            }else if (args[1].equals("RESTORE")){
                response = stub.restore(args[2]);
            }else if (args[1].equals("RECLAIM")){
                response = stub.reclaim(Integer.parseInt(args[2]));
            }else if (args[1].equals("STATE")){
                System.out.println(stub.state());
                return;
            }
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
        System.out.println("response: " + response);
    }
}
