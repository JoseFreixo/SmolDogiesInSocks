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
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Control implements ControlInterface {
    private static int id;
    private static InetAddress mdc_ip;
    private static int mdc_port;
    private static InetAddress mcc_ip;
    private static int mcc_port;
    private static MulticastSocket mdc_socket;
    private static MulticastSocket mcc_socket;
    private static String crlf = "" + (char)0xD + (char)0xA;
    private static String version = "1.0";

    public Control(String [] args) throws IOException {
        bootSockets(args);
    }


    private static void bootSockets(String[] args) throws IOException {
        id = Integer.parseInt(args[0]);
        mdc_ip = InetAddress.getByName(args[1]);
        mdc_port = Integer.parseInt(args[2]);
        mcc_ip = InetAddress.getByName(args[3]);
        mcc_port = Integer.parseInt(args[4]);

        mdc_socket = new MulticastSocket(mdc_port);
        mcc_socket = new MulticastSocket(mcc_port);

        mdc_socket.joinGroup(mdc_ip);
        mcc_socket.joinGroup(mcc_ip);
    }
    private static String encodeSHA256(String text) {
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
    public int backup(String file_name, String repl) throws IOException {
        int timeout = 1000;
        mdc_socket.setSoTimeout(timeout);

        File file = new File(file_name);
        Path path = Paths.get(file.getAbsolutePath());
        BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
        byte[] body = Files.readAllBytes(path);
        String fileId = encodeSHA256(file_name + attr.creationTime() + attr.lastModifiedTime() + attr.size());
        String chunkNo = "0";
        String message = "PUTCHUNK " + version + " " + id + " " + fileId + " " + chunkNo + " " + repl + " " + crlf + crlf;
        byte[] sbuf1 = message.getBytes();
        byte[] result = concat(sbuf1,body);

        System.out.println(result.length);
        DatagramPacket packet = new DatagramPacket(result, result.length, mdc_ip, mdc_port);

        /*int tries = 0;
        boolean done = false;

        while (tries < 5 && !done) {
            mdc_socket.send(packet);
            int i;
            for (i = Integer.parseInt(repl); i > 0; i++) {
                try {
                    mcc_socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    tries++;
                    break;
                }
            }
            if (i <= 0)
                done = true;
        }*/

        /*Path newfile = Paths.get("Copy of " + file_name);
        Files.write(newfile, body);*/

        System.out.println("Hello Manel");
        return 0;
    }
}
