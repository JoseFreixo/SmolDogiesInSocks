import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
                    for (int i = 0; i < 20; i++) {
                        System.out.println(packetData.getBody()[i]);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
