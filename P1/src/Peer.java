import java.io.*;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Peer implements Runnable {
    private static int id;
    private static InetAddress mdc_ip;
    private static int mdc_port;
    private static InetAddress mcc_ip;
    private static int mcc_port;
    private static MulticastSocket mdc_socket;
    private static MulticastSocket mcc_socket;
    private static String crlf = "" + (char)0xD + (char)0xA;
    private static String version = "1.0";

    public static ScheduledThreadPoolExecutor poolExecuter = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

        if (args.length != 5) {
            System.out.println("Wrong arguments! Try:");
            System.out.println("Peer <id> <MDC_ip> <MDC_port> <MCC_id> <MCC_port>");
            return;
        }

        parseArgs(args);

        SocketRunnable mdcRunnable = new SocketRunnable(mdc_ip,mdc_port,id);
        SocketRunnable mccRunnable = new SocketRunnable(mcc_ip,mcc_port,id);

        Thread mdcThread = new Thread(mdcRunnable);
        Thread mccThread = new Thread(mccRunnable);

        mdcThread.start();
        mccThread.start();

        System.out.println("Dei start");

        Control control = new Control(args);
        ControlInterface proxy = (ControlInterface) UnicastRemoteObject.exportObject(control, 0);

        Registry reg = LocateRegistry.getRegistry();
        reg.rebind("Manel", proxy);

        System.out.println("Manel");

        return;
    }

    private static void parseArgs(String[] args) throws IOException {
        id = Integer.parseInt(args[0]);
        mdc_ip = InetAddress.getByName(args[1]);
        mdc_port = Integer.parseInt(args[2]);
        mcc_ip = InetAddress.getByName(args[3]);
        mcc_port = Integer.parseInt(args[4]);

        /*mdc_socket = new MulticastSocket(mdc_port);
        mcc_socket = new MulticastSocket(mcc_port);

        mdc_socket.joinGroup(mdc_ip);
        mcc_socket.joinGroup(mcc_ip);*/
    }


    @Override
    public void run() {

    }
}
