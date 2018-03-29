import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    private Client() {}

    public static void main(String[] args) {

        String host = (args.length < 1) ? null : args[0];

        String filename = "04.jpg";
        String replDeg = "1";

        try {
            Registry registry = LocateRegistry.getRegistry(host);
            ControlInterface stub = (ControlInterface) registry.lookup("Manel");
            int response = stub.backup(filename,replDeg);
            System.out.println("response: " + response);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
