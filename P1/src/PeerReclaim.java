import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;

public class PeerReclaim extends Peer implements Runnable{
    int maxSize;
    public PeerReclaim(int size) {
        this.maxSize = size;
    }

    @Override
    public void run() {
        File folder = new File(".");
        File[] listOfFiles = folder.listFiles();
        int i;
        int sizeAccumulator = 0;

        //iterate through files with size smaller than the available size
        for (i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() && listOfFiles[i].getName().startsWith("Chunk")) {
                System.out.println("File " + listOfFiles[i].getName() + "has "+ listOfFiles[i].length() + "bytes" );
                sizeAccumulator += listOfFiles[i].length();
            }
            if(sizeAccumulator > this.maxSize){
                break;
            }
        }
        for (; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() && listOfFiles[i].getName().startsWith("Chunk")) {
                //REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
                System.out.println("File " + listOfFiles[i].getName() + "has "+ listOfFiles[i].length() + "bytes" );
                String chunkNo = listOfFiles[i].getName().replace("Chunk","").replaceFirst("of.*","");
                String fileId = listOfFiles[i].getName().replaceFirst("Chunk.+?of","");
                listOfFiles[i].delete();
                File file = new File("countChunk"+ chunkNo+"of" + fileId);
                file.delete();
                System.out.println("Chunk " + chunkNo);
                System.out.println("fileId " + fileId);
                String message = "REMOVED " + version + " " + id + " " + fileId + " " + chunkNo + " " + crlf + crlf;
                byte[] result = message.getBytes();
                DatagramPacket packet = new DatagramPacket(result, result.length, mcc_ip, mcc_port);
                try {
                    mcc_socket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("total size" + sizeAccumulator);
    }
}
