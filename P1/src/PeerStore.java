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
        if (packetData.getBody().length + peerCurrSize > peerMaxSize)
            return;
        System.out.println(id + " is Storing");
        File file = new File("peer" + id + "Chunk"+ packetData.getChunkNo()+"of" + packetData.getFileId());
        Path newFile = Paths.get("peer" + id + "Chunk"+ packetData.getChunkNo()+"of" + packetData.getFileId());
        Path countFile = Paths.get("peer" + id + "countChunk"+ packetData.getChunkNo()+"of" + packetData.getFileId());
        try {
            Files.write(newFile, packetData.getBody());
            String count = packetData.getRepl() + " " + 1;
            Files.write(countFile,count.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        peerCurrSize += packetData.getBody().length;

        File peerSize = new File("peer" + id + "Size");
        Path pathPeerSize = Paths.get(peerSize.getAbsolutePath());
        String sizeContent = new Integer(peerMaxSize).toString() + " " + new Integer(peerCurrSize).toString();
        try {
            Files.write(pathPeerSize, sizeContent.getBytes());
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
