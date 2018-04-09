import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Peer implements ControlInterface {
    protected static int id;
    protected static InetAddress mdc_ip;
    protected static int mdc_port;
    protected static MulticastSocket mdc_socket;
    protected static InetAddress mcc_ip;
    protected static int mcc_port;
    protected static MulticastSocket mcc_socket;
    protected static InetAddress mdr_ip;
    protected static int mdr_port;
    protected static MulticastSocket mdr_socket;
    protected static String crlf = "" + (char)0xD + (char)0xA;
    protected static String version = "1.0";
    protected static Map<String, Integer> storedsReceived = new ConcurrentHashMap<>();
    protected static boolean wasChunkReceived = false;
    protected static boolean initiatorPeer = false;
    protected static byte[] receivedChunk;
    protected static int chunkMaxSize = 60000;
    protected static int maxWaitingTime = 400;
    protected static int peerMaxSize;
    protected static int peerCurrSize;
    public static Peer peer;

    public Peer(){
        this.peer = this;
    }

    public static void main(String[] args) throws IOException{

        if (args.length != 8) {
            System.out.println("Wrong arguments! Try:");
            System.out.println("Peer <id> <version> <MDC_ip> <MDC_port> <MCC_ip> <MCC_port> <MDR_ip> <MDR_port>");
            return;
        }

        bootSockets(args);

        File sizeFile = new File("peer" + id + "Size");
        if (!sizeFile.exists()){
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(sizeFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            String sizeContent = new Integer(Integer.MAX_VALUE).toString() + " 0";
            out.write(sizeContent.getBytes());
            peerMaxSize = Integer.MAX_VALUE;
            peerCurrSize = 0;
        } else {
            Scanner scanner = new Scanner(sizeFile);
            peerMaxSize = scanner.nextInt();
            peerCurrSize = scanner.nextInt();
        }

        SocketRunnable mdcRunnable = new SocketRunnable(mdc_ip,mdc_port,peer);
        SocketRunnable mccRunnable = new SocketRunnable(mcc_ip,mcc_port,peer);
        SocketRunnable mdrRunnable = new SocketRunnable(mdr_ip,mdr_port,peer);
        Thread mdcThread = new Thread(mdcRunnable);
        Thread mccThread = new Thread(mccRunnable);
        Thread mdrThread = new Thread(mdrRunnable);

        mdcThread.start();
        mccThread.start();
        mdrThread.start();

        System.out.println("Dei start");

        try {
            Peer obj = new Peer();
            ControlInterface stub = (ControlInterface) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("peer"+id, stub);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }


    protected static void bootSockets(String[] args) throws IOException {
        id = Integer.parseInt(args[0]);
        version = args[1];
        mdc_ip = InetAddress.getByName(args[2]);
        mdc_port = Integer.parseInt(args[3]);
        mcc_ip = InetAddress.getByName(args[4]);
        mcc_port = Integer.parseInt(args[5]);
        mdr_ip = InetAddress.getByName(args[6]);
        mdr_port = Integer.parseInt(args[7]);

        mdc_socket = new MulticastSocket(mdc_port);
        mcc_socket = new MulticastSocket(mcc_port);
        mdr_socket = new MulticastSocket(mdr_port);
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

    @Override
    public int backup(String file_name, String repl) throws IOException, InterruptedException {
        PeerBackup peerBackup = new PeerBackup(file_name,repl);
        peerBackup.run();
        return 0;
    }

    @Override
    public int delete(String file_name) {
        PeerSendDelete peerSendDelete = new PeerSendDelete(file_name);
        Thread sendDelete = new Thread(peerSendDelete);
        sendDelete.start();

        return 0;
    }

    @Override
    public int restore(String file_name) throws IOException, InterruptedException {
        initiatorPeer = true;
        PeerRestore peerRestore = new PeerRestore(file_name);
        peerRestore.run();
        initiatorPeer = false;
        return 0;
    }

    @Override
    public int reclaim(int size) throws IOException, InterruptedException {
        PeerReclaim peerReclaim = new PeerReclaim(size);
        peerReclaim.run();
        return 0;
    }
}
