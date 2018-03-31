import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
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
        System.out.println("runnable a fazer cenas");
        System.out.println(ip);
        System.out.println(port);
        while(true){
            try {
                socket.receive(packet);
                System.out.println("Recebi cenas");
                PacketData packetData = new PacketData(packet);
                if(this.peer.id == Integer.parseInt(packetData.getSenderId())){
                    continue;
                }
                System.out.println(packetData.getType());
                if(packetData.getType().equals("STORED")){
                    if (this.peer.storedsRecieved.containsKey(packetData.getChunkNo() + packetData.getFileId())){
                        System.out.println("recebi pacotee stored");
                        synchronized(System.out) {
                            Integer i = this.peer.storedsRecieved.get(packetData.getChunkNo()+packetData.getFileId());
                            this.peer.storedsRecieved.put(packetData.getChunkNo()+ packetData.getFileId(),i + 1);
                            System.out.println("meti os storeds a " + this.peer.storedsRecieved.get(packetData.getChunkNo()+ packetData.getFileId()));
                        }
                    }else {
                        File file = new File("countChunk"+ packetData.getChunkNo()+"of" + packetData.getFileId());
                        Path path = Paths.get(file.getAbsolutePath());
                        String replStoreds[] = new String(Files.readAllBytes(path)).split(" ");
                        Integer count = Integer.parseInt(replStoreds[1]) + 1;
                        String store = replStoreds[0] + " " + count;
                        Files.write(path,store.getBytes());
                    }
                }
                if(packetData.getType().equals("PUTCHUNK")){
                    if(packetData.getFileId().equals(this.putChunkFileId))
                        this.putChunkArrived = true;
                    System.out.println("era putchunk vou guardar");
                    PeerStore peerStore = new PeerStore(packetData);
                    threadPoolExecutor.execute(peerStore);
                }
                if(packetData.getType().equals("DELETE")){
                    System.out.println("era Delete vou apagar cenas");
                    PeerDelete peerDelete = new PeerDelete(packetData);
                    threadPoolExecutor.execute(peerDelete);
                }
                if(packetData.getType().equals("GETCHUNK")){
                    System.out.println("era get chunk vou à procura do chunk para mandar");
                    this.peer.wasChunkReceived = false;
                    PeerGetChunks peerGetChunks = new PeerGetChunks(packetData);
                    threadPoolExecutor.execute(peerGetChunks);
                }
                if(packetData.getType().equals("CHUNK")){
                    System.out.println("Chunk foi recebido");
                    this.peer.wasChunkReceived = true;
                }
                if(packetData.getType().equals("CHUNK") && this.peer.initiatorPeer){
                    System.out.println("Chunk foi recebido pelo initiatorPeer");
                    this.peer.receivedChunk = packetData.getBody();
                }
                if(packetData.getType().equals("REMOVED")){
                    File file = new File("countChunk"+ packetData.getChunkNo()+"of" + packetData.getFileId());
                    if(file.exists()){
                        Path path = Paths.get(file.getAbsolutePath());
                        byte[] storedArray = Files.readAllBytes(path);
                        String stored = new String(storedArray);
                        String replStoreds[] = stored.split(" ");
                        Integer count = Integer.parseInt(replStoreds[1]) - 1;
                        String store = replStoreds[0] + " " + count;
                        Files.write(path,store.getBytes());
                        Integer repl =  Integer.parseInt(replStoreds[0]);
                        System.out.println(packetData.getChunkNo());
                        System.out.println("repl " + repl);
                        System.out.println("count " + count);
                        if(repl > count){
                            System.out.println("repl menor do que storeds");
                            this.putChunkFileId = packetData.getFileId();
                            this.putChunkArrived = false;
                            Random rand = new Random();
                            int waitingTime = rand.nextInt(this.peer.maxWaitingTime);
                            Thread.sleep(waitingTime);
                            if(!putChunkArrived){
                                System.out.println("vou mandar o chunk outra vez");
                                this.peer.storedsRecieved.put(packetData.getChunkNo()+packetData.getFileId(),0);
                                SendChunks sendChunks = new SendChunks(packet,this.peer.mdc_socket,packetData,this.peer.storedsRecieved);
                                threadPoolExecutor.execute(sendChunks);
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
