import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteStub;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

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

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

        if (args.length != 5) {
            System.out.println("Wrong arguments! Try:");
            System.out.println("Peer <id> <MDC_ip> <MDC_port> <MCC_id> <MCC_port>");
            return;
        }

        Control control = new Control(args);
        ControlInterface proxy = (ControlInterface) UnicastRemoteObject.exportObject(control, 0);

        Registry reg = LocateRegistry.getRegistry();
        reg.rebind("Manel", proxy);

        return;
    }

    @Override
    public void run() {

    }
}
