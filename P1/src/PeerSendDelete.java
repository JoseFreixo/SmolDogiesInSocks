import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

public class PeerSendDelete extends Peer implements Runnable{
    String file_name;

    public PeerSendDelete(String file_name) {
        this.file_name = file_name;
    }

    @Override
    public void run() {
        int timeout = 100000;
        try {
            mcc_socket.setSoTimeout(timeout);
            mcc_socket.setTimeToLive(2);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        File file = new File(file_name);
        Path path = Paths.get(file.getAbsolutePath());
        try {
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            String fileId = encodeSHA256(file_name + attr.creationTime() + attr.lastModifiedTime() + attr.size());
            String message = "DELETE " + version + " " + id + " " + fileId + " " + crlf + crlf;
            byte[] result = message.getBytes();
            DatagramPacket packet = new DatagramPacket(result, result.length, mcc_ip, mcc_port);
            System.out.println("Sent delete to " + mcc_ip + ", " + mcc_port);
            mcc_socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
