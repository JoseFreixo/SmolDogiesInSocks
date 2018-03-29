import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class Peer implements ControlInterface {
    protected static int id;
    protected static InetAddress mdc_ip;
    protected static int mdc_port;
    protected static InetAddress mcc_ip;
    protected static int mcc_port;
    protected static MulticastSocket mdc_socket;
    protected static MulticastSocket mcc_socket;
    protected static String crlf = "" + (char)0xD + (char)0xA;
    protected static String version = "1.0";
    public static AbstractMap<String,Integer> storedsRecieved;
    public int maxTries = 5;
    public static String fileSent;
    public static Peer peer;

    public Peer(){
        this.peer = this;
    }

    public static void main(String[] args) throws IOException{

        if (args.length != 5) {
            System.out.println("Wrong arguments! Try:");
            System.out.println("Peer <id> <MDC_ip> <MDC_port> <MCC_id> <MCC_port>");
            return;
        }

        bootSockets(args);
        SocketRunnable mdcRunnable = new SocketRunnable(mdc_ip,mdc_port,peer);
        SocketRunnable mccRunnable = new SocketRunnable(mcc_ip,mcc_port,peer);
        Thread mdcThread = new Thread(mdcRunnable);
        Thread mccThread = new Thread(mccRunnable);

        mdcThread.start();
        mccThread.start();

        System.out.println("Dei start");

        try {
            Peer obj = new Peer();
            ControlInterface stub = (ControlInterface) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("Manel", stub);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }


    protected static void bootSockets(String[] args) throws IOException {
        id = Integer.parseInt(args[0]);
        mdc_ip = InetAddress.getByName(args[1]);
        mdc_port = Integer.parseInt(args[2]);
        mcc_ip = InetAddress.getByName(args[3]);
        mcc_port = Integer.parseInt(args[4]);

        mdc_socket = new MulticastSocket(mdc_port);
        mcc_socket = new MulticastSocket(mcc_port);
    }
    protected static String encodeSHA256(String text) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public static byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }


    public void store(PacketData packetData){

    }

    @Override
    public int backup(String file_name, String repl) throws IOException, InterruptedException {
        PeerBackup peerBackup = new PeerBackup(file_name,repl);
        peerBackup.run();
        return 0;
    }
}
