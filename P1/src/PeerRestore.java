import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

public class PeerRestore extends Peer implements Runnable{
    String file_name;
    public PeerRestore(String file_name) {
        this.file_name = file_name;
    }

    @Override
    public void run() {
        int timeout = 100000;
        int waitingTime = 600;
        try {
            mcc_socket.setSoTimeout(timeout);
            mcc_socket.setTimeToLive(2);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        //GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
        File file = new File(file_name);
        File restoredFile = new File("restored" + file_name);
        FileOutputStream output = null;
        try {
             output = new FileOutputStream(restoredFile, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Path path = Paths.get(file.getAbsolutePath());
        try {
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            String fileId = encodeSHA256(file_name + attr.creationTime() + attr.lastModifiedTime() + attr.size());
            double noOfChunksDouble = (int)attr.size()/(double)chunkMaxSize;
            int noOfChunks = (int)Math.ceil(noOfChunksDouble);
            for (int chunkNo = 0; chunkNo < noOfChunks; chunkNo++) {
                String message = "GETCHUNK " + version + " " + id + " " + fileId + " " + chunkNo + " "+crlf + crlf;
                byte[] result = message.getBytes();
                DatagramPacket packet = new DatagramPacket(result, result.length, mcc_ip, mcc_port);
                System.out.println("Sent getchunk to " + mcc_ip + ", " + mcc_port);
                mcc_socket.send(packet);
                Thread.sleep(waitingTime);
                if(wasChunkReceived){
                    output.write(receivedChunk);
                    wasChunkReceived = false;
                }
            }
            output.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
