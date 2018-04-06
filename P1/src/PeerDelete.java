import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PeerDelete extends Peer implements Runnable {
    PacketData packetData;
    public PeerDelete(PacketData packetData) {
        this.packetData = packetData;
    }

    @Override
    public void run() {
        String fileId = packetData.getFileId();
        int chunk = 0;
        int size = 0;
        while (true){
            File f = new File("peer" + id + "Chunk"+chunk+"of"+fileId);
            if(!f.isFile())
                break;
            System.out.println("Deleted: peer" + id + "Chunk"+chunk+"of"+fileId);
            size += f.length();
            f.delete();
            chunk++;
        }
        peerCurrSize -= size;

        File peerSize = new File("peer" + id + "Size");
        Path pathPeerSize = Paths.get(peerSize.getAbsolutePath());
        String sizeContent = new Integer(peerMaxSize).toString() + " " + new Integer(peerCurrSize).toString();
        try {
            Files.write(pathPeerSize, sizeContent.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        chunk = 0;
        while (true){
            File f = new File("peer" + id + "countChunk"+chunk+"of"+fileId);
            if(!f.isFile())
                break;
            System.out.println("Deleted: peer" + id + "countChunk"+chunk+"of"+fileId);
            f.delete();
            chunk++;
        }
    }
}
