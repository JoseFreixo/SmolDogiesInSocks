import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

public class PeerBackup extends Peer implements Runnable{
    String file_name;
    String repl;
    public PeerBackup(String file_name, String repl) {
        super();
        this.file_name = file_name;
        this.repl = repl;
    }

    public int backup() throws IOException, InterruptedException {
        int timeout = 1000;
        mdc_socket.setSoTimeout(timeout);
        mdc_socket.setTimeToLive(2);

        File file = new File(file_name);
        Path path = Paths.get(file.getAbsolutePath());
        BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
        byte[] body = Files.readAllBytes(path);
        String fileId = encodeSHA256(file_name + attr.creationTime() + attr.lastModifiedTime() + attr.size());
        String chunkNo = "0";
        String message = "PUTCHUNK " + version + " " + id + " " + fileId + " " + chunkNo + " " + repl + " " + crlf + crlf;
        byte[] sbuf1 = message.getBytes();
        byte[] result = concat(sbuf1,body);

        fileSent = fileId;
        DatagramPacket packet = new DatagramPacket(result, result.length, mdc_ip, mdc_port);
        System.out.println(mdc_ip);
        System.out.println(mdc_port);
        int tries = 0;

        int sleepingTime = 1000;
        while (tries < maxTries) {
            mdc_socket.send(packet);
            System.out.println("enviei cenas");
            Thread.sleep(sleepingTime);
            if(storedsRecieved >= Integer.parseInt(repl))
                break;
            tries++;
            sleepingTime*=2;
        }

        storedsRecieved = 0;

        System.out.println("Hello Manel");
        return 0;
    }

    @Override
    public void run() {
        try {
            backup();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
