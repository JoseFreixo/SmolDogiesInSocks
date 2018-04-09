import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SocketRunnable implements Runnable {
    private Peer peer;
    private InetAddress ip;
    private int port;
    private MulticastSocket socket;
    ExecutorService threadPoolExecutor;
    boolean putChunkArrived = false;
    String putChunkFileId = "";
    int timeout = 1000;

    public SocketRunnable(InetAddress ip, int port, Peer peer) throws IOException {
        this.ip = ip;
        this.port = port;
        socket = new MulticastSocket(this.port);
        socket.joinGroup(this.ip);
        this.peer = peer;

        int  corePoolSize  =    5;
        int  maxPoolSize   =   15;
        long keepAliveTime = 5000;

        threadPoolExecutor =
                new ThreadPoolExecutor(
                        corePoolSize,
                        maxPoolSize,
                        keepAliveTime,
                        TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<>()
                );
    }

    @Override
    public void run(){
        byte [] rbuf = new byte[70000];
        DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
        while(true){
            try {
                socket.receive(packet);
                PacketData packetData = new PacketData(packet);

                if(packetData.getType().equals("DELETE")){
                    System.out.println("DELETE received!");
                    PeerDelete peerDelete = new PeerDelete(packetData);
                    threadPoolExecutor.execute(peerDelete);
                }
                if(this.peer.id == Integer.parseInt(packetData.getSenderId())){
                    continue;
                }
                if(packetData.getType().equals("STORED")){
                    System.out.println("STORED received!");
                    if (this.peer.storedsReceived.containsKey(packetData.getChunkNo() + packetData.getFileId())){
                        synchronized(System.out) {
                            Integer i = this.peer.storedsReceived.get(packetData.getChunkNo()+packetData.getFileId());
                            this.peer.storedsReceived.put(packetData.getChunkNo()+ packetData.getFileId(),i + 1);
                        }
                    }else {
                        File file = new File("peer" + this.peer.id + "countChunk"+ packetData.getChunkNo()+"of" + packetData.getFileId());
                        Path path = Paths.get(file.getAbsolutePath());
                        String replStoreds[];
                        try {
                            replStoreds = new String(Files.readAllBytes(path)).split(" ");
                        } catch (NoSuchFileException e) {
                            continue;
                        }
                        Integer count = Integer.parseInt(replStoreds[1]) + 1;
                        String store = replStoreds[0] + " " + count;
                        Files.write(path,store.getBytes());
                    }
                }
                if(packetData.getType().equals("PUTCHUNK")){
                    if(packetData.getFileId().equals(this.putChunkFileId))
                        this.putChunkArrived = true;
                    System.out.println("PUTCHUNK received!");
                    PeerStore peerStore = new PeerStore(packetData);
                    threadPoolExecutor.execute(peerStore);
                }
                if(packetData.getType().equals("GETCHUNK")){
                    System.out.println("GETCHUNK received!");
                    this.peer.wasChunkReceived = false;
                    PeerGetChunks peerGetChunks = new PeerGetChunks(packetData);
                    threadPoolExecutor.execute(peerGetChunks);
                }
                if(packetData.getType().equals("CHUNK")){
                    System.out.println("CHUNK received!");
                    this.peer.wasChunkReceived = true;
                }
                if(packetData.getType().equals("CHUNK") && this.peer.initiatorPeer){
                    System.out.println("INITIATOR: CHUNK received!");
                    this.peer.receivedChunk = packetData.getBody();
                }
                if(packetData.getType().equals("REMOVED")){
                    System.out.println("REMOVED received!");
                    File file = new File("peer" + this.peer.id + "countChunk"+ packetData.getChunkNo()+"of" + packetData.getFileId());
                    if(file.exists()){
                        Path path = Paths.get(file.getAbsolutePath());
                        byte[] storedArray = Files.readAllBytes(path);
                        String stored = new String(storedArray);
                        String replStoreds[] = stored.split(" ");
                        Integer count = Integer.parseInt(replStoreds[1]) - 1;
                        String store = replStoreds[0] + " " + count;
                        Files.write(path,store.getBytes());
                        Integer repl =  Integer.parseInt(replStoreds[0]);
                        if(repl > count){
                            System.out.println("Repl > Stored");
                            this.putChunkFileId = packetData.getFileId();
                            this.putChunkArrived = false;
                            Random rand = new Random();
                            int waitingTime = rand.nextInt(this.peer.maxWaitingTime);
                            Thread.sleep(waitingTime);
                            if(!putChunkArrived){
                                System.out.println("Resending chunk!");
                                this.peer.storedsReceived.put(packetData.getChunkNo()+packetData.getFileId(),0);
                                ReclaimChunk reclaimChunk = new ReclaimChunk(packetData,repl);
                                threadPoolExecutor.execute(reclaimChunk);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
