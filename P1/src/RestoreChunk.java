import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RestoreChunk extends Peer implements Runnable{
    PacketData packetData;
    int repl;
    public RestoreChunk(PacketData packetData,int repl) {
        this.packetData = packetData;
        this.repl = repl;
    }

    @Override
    public void run() {
        try {
        int timeout = 1000;
        mdc_socket.setSoTimeout(timeout);
        mdc_socket.setTimeToLive(2);
        File file = new File("Chunk"+ packetData.getChunkNo()+"of" + packetData.getFileId());
        Path path = Paths.get(file.getAbsolutePath());
        byte[] body = Files.readAllBytes(path);
        String message = "PUTCHUNK " + version + " " + id + " " + packetData.getFileId() + " " + packetData.getChunkNo() + " " + repl + " " + crlf + crlf;
        byte[] sbuf1 = message.getBytes();
        byte[] result = concat(sbuf1,body);
        DatagramPacket packet = new DatagramPacket(result, result.length, mdc_ip, mdc_port);
        SendChunks sendChunks = new SendChunks(packet,mdc_socket,packetData,storedsRecieved);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
