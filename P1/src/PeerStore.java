import java.io.IOException;
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
        Path newFile = Paths.get("Copy of " + packetData.getFileId());
        System.out.println(new String(packetData.getBody()));
        try {
            Files.write(newFile, packetData.getBody());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
