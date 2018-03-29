import java.io.IOException;
import java.net.*;
import java.util.Arrays;
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
        int  maxPoolSize   =   10;
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
        while(true){
            try {
                socket.receive(packet);
                System.out.println("recebi cenas");
                PacketData packetData = new PacketData(packet);
                if(this.peer.id == Integer.parseInt(packetData.getSenderId())){
                    continue;
                }
                System.out.println(packetData.getType());
                System.out.println(packetData.getFileId());
                System.out.println(this.peer.fileSent);
                if(packetData.getType().equals("STORED") && packetData.getFileId().equals(this.peer.fileSent)){
                    System.out.println("recebi pacotee stored");
                    Integer i = this.peer.storedsRecieved.get(packetData.getChunkNo()+packetData.getFileId());
                    this.peer.storedsRecieved.put(packetData.getChunkNo()+ packetData.getFileId(),i + 1);
                }
                if(packetData.getType().equals("PUTCHUNK")){
                    System.out.println("era putchunk vou guardar");
                    PeerStore peerStore = new PeerStore(packetData);
                    threadPoolExecutor.execute(peerStore);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
