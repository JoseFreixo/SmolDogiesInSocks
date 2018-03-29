import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.*;

public class PeerBackup extends Peer implements Runnable{
    String file_name;
    String repl;

    int  corePoolSize  =    5;
    int  maxPoolSize   =   10;
    long keepAliveTime = 5000;
    ExecutorService threadPoolExecutor =
            new ThreadPoolExecutor(
            corePoolSize,
            maxPoolSize,
            keepAliveTime,
            TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<>()
            );
    public PeerBackup(String file_name, String repl) {
        super();
        this.file_name = file_name;
        this.repl = repl;
    }

    public int backup() throws IOException, InterruptedException {
        int timeout = 1000;
        mdc_socket.setSoTimeout(timeout);
        mdc_socket.setTimeToLive(2);

        try {
            File file = new File(file_name);
            FileInputStream is = new FileInputStream(file);
            Path path = Paths.get(file.getAbsolutePath());
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            byte[] body = new byte[6000];
            int chunkLen = 0;
            while ((chunkLen = is.read(body)) != -1) {
                String fileId = encodeSHA256(file_name + attr.creationTime() + attr.lastModifiedTime() + attr.size());
                int chunkNo = 0;
                String message = "PUTCHUNK " + version + " " + id + " " + fileId + " " + chunkNo + " " + repl + " " + crlf + crlf;
                byte[] sbuf1 = message.getBytes();
                byte[] result = concat(sbuf1,body);
                DatagramPacket packet = new DatagramPacket(result, result.length, mdc_ip, mdc_port);

                storedsRecieved.put(chunkNo+fileId,0);
                PacketData packetData = new PacketData(packet);
                chunkNo++;
                SendChunks sendChunks = new SendChunks(packet,mdc_socket,packetData,storedsRecieved);

                threadPoolExecutor.execute(sendChunks);
            }
        } catch (FileNotFoundException fnfE) {
            // file not found, handle case
        } catch (IOException ioE) {
            // problem reading, handle case
        }

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
