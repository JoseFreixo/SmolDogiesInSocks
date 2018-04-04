import java.io.File;

public class PeerDelete implements Runnable {
    PacketData packetData;
    public PeerDelete(PacketData packetData) {
        this.packetData = packetData;
    }

    @Override
    public void run() {
        String fileId = packetData.getFileId();
        int chunk = 0;
        while (true){
            File f = new File("Chunk"+chunk+"of"+fileId);
            if(!f.isFile())
                break;
            System.out.println("Deleted: Chunk"+chunk+"of"+fileId);
            f.delete();
            chunk++;
        }
        chunk = 0;
        while (true){
            File f = new File("countChunk"+chunk+"of"+fileId);
            if(!f.isFile())
                break;
            System.out.println("Deleted: countChunk"+chunk+"of"+fileId);
            f.delete();
            chunk++;
        }
    }
}
