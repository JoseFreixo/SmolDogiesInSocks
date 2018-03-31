import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class PeerGetChunks extends Peer implements Runnable {
    PacketData packetData;

    public PeerGetChunks(PacketData packetData) {
        this.packetData = packetData;
    }

    @Override
    public void run() {
        Random rand = new Random();
        File file = new File("Chunk"+packetData.getChunkNo()+"of"+packetData.getFileId());
        Path path = Paths.get(file.getAbsolutePath());
        byte[] body = null;
        try {
            body = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        //CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
        String message = "CHUNK " + version + " " + id + " " + packetData.getFileId() + " " + packetData.getChunkNo() + " " + crlf + crlf;
        byte[] sbuf1 = message.getBytes();
        byte[] result = concat(sbuf1,body);
        DatagramPacket packet = new DatagramPacket(result, result.length, mdr_ip, mdr_port);
        int waitingTime = rand.nextInt(maxWaitingTime);
        try {
            Thread.sleep(waitingTime);
            if(wasChunkReceived){
                System.out.println("Chunk recebido");
                return;
            }
            System.out.println("Chunk enviado");
            mdr_socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
