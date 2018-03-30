import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PeerStore extends Peer implements Runnable{
    PacketData packetData;
    public PeerStore(PacketData packetData) {
        super();
        this.packetData = packetData;
    }

    @Override
    public void run() {
        System.out.println(id + " is Storing");
        File file = new File("Chunk"+ packetData.getChunkNo()+"of" + packetData.getFileId());
        if(file.exists())
            return;
        Path newFile = Paths.get("Chunk"+ packetData.getChunkNo()+"of" + packetData.getFileId());
        Path countFile = Paths.get("countChunk"+ packetData.getChunkNo()+"of" + packetData.getFileId());
        try {
            Files.write(newFile, packetData.getBody());
            String count = packetData.getRepl() + " " + 1;
            Files.write(countFile,count.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String message = "STORED " +version +" " +id +" " +packetData.getFileId()+" " + packetData.getChunkNo()+" " + crlf + crlf;
        byte [] messageSend = message.getBytes();
        DatagramPacket packet = new DatagramPacket(messageSend, messageSend.length, mcc_ip, mcc_port);
        try {
            mdc_socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
