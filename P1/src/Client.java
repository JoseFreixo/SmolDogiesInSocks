import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    private Client() {}

    public static void main(String[] args) {
        String filename = "04.jpg";
        String replDeg = "1";
        int response = 0;
        try {
            Registry registry = LocateRegistry.getRegistry(null);
            ControlInterface stub = (ControlInterface) registry.lookup("Manel");
            if(args[0].equals("BACKUP")){
                response = stub.backup(args[1],args[2]);
            }else if (args[0].equals("DELETE")){
                response = stub.delete(args[1]);
            }else if (args[0].equals("RESTORE")){
                response = stub.restore(args[1]);
            }


        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
        System.out.println("response: " + response);
    }
}
