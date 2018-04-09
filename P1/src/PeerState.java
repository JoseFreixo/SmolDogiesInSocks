import java.io.File;
import java.io.FileNotFoundException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Scanner;

public class PeerState extends Peer{
    protected static String state;
    public PeerState() {
        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.FLOOR);
        state = "Peer maximum capacity: " + df.format((float)peerMaxSize / 1024) + "\n";
        state += "Peer current capacity: " + df.format((float)peerCurrSize / 1024) + "\n\n";
        File folder = new File(".");
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles){
            if (file.isFile() && file.getName().startsWith("peer" + id + "Chunk")) {
                state += "Chunk:\n";
                String chunkNo = file.getName().replace("Chunk","").replaceFirst("of.*","").replaceFirst("peer" + id,"");
                String fileId = file.getName().replaceFirst("peer" + id + "Chunk.+?of","");
                state += "\tId: " + chunkNo + "\n";
                state += "\tSize: " + df.format((float)file.length() / 1024) + "\n";
                File countFile = new File("peer" + id + "countChunk"+ chunkNo+"of" + fileId);
                Scanner scanner = null;
                try {
                    scanner = new Scanner(countFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                scanner.nextInt();
                state += "\tPerceived Replication Degree: " + scanner.nextInt() + "\n\n";
            }
        }
    }

    public String getState() {
        return state;
    }
}
