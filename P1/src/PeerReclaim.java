import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

public class PeerReclaim extends Peer implements Runnable{
    public PeerReclaim(int size) throws IOException {
        System.out.println("Reclaiming to " + size);
        peerMaxSize = size;
    }

    @Override
    public void run() {
        File folder = new File(".");
        File[] listOfFiles = folder.listFiles();
        int i;
        int sizeAccumulator = 0;

        //iterate through files with size smaller than the available size
        for (i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() && listOfFiles[i].getName().startsWith("peer" + id + "Chunk")) {
                sizeAccumulator += listOfFiles[i].length();
            }
            if(sizeAccumulator > peerMaxSize){
                break;
            }
        }
        int sizeRemoved = 0;
        for (; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() && listOfFiles[i].getName().startsWith("peer" + id + "Chunk")) {
                //REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
                String chunkNo = listOfFiles[i].getName().replace("Chunk","").replaceFirst("of.*","").replaceFirst("peer" + id,"");
                String fileId = listOfFiles[i].getName().replaceFirst("peer" + id + "Chunk.+?of","");
                sizeRemoved += listOfFiles[i].length();
                listOfFiles[i].delete();
                File file = new File("peer" + id + "countChunk"+ chunkNo+"of" + fileId);
                file.delete();
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

        peerCurrSize -= sizeRemoved;
        File sizeFile = new File("peer" + id + "Size");
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(sizeFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String sizeContent = new Integer(peerMaxSize).toString() + " " + new Integer(peerCurrSize).toString();
        try {
            out.write(sizeContent.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
