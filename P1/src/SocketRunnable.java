import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SocketRunnable implements Runnable {
    private static Peer peer;
    private static InetAddress ip;
    private static int port;
    private static MulticastSocket socket;

    public SocketRunnable(InetAddress ip, int port, Peer peer) throws IOException {
        this.ip = ip;
        this.port = port;
        socket = new MulticastSocket(this.port);
        socket.joinGroup(ip);
        this.peer = peer;

        int  corePoolSize  =    5;
        int  maxPoolSize   =   10;
        long keepAliveTime = 5000;

        ExecutorService threadPoolExecutor =
                new ThreadPoolExecutor(
                        corePoolSize,
                        maxPoolSize,
                        keepAliveTime,
                        TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>()
                );
    }

    @Override
    public void run() {
        byte[] rbuf = new byte[70000];
        DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
        System.out.println("runnable a fazer cenas");
        while(true){
            try {
                socket.receive(packet);
                System.out.println("recebi cenas");
                PacketData packetData = new PacketData(packet);
                if(packetData.getType() == "STORED" && packetData.getFileId() == this.peer.fileSent){
                    System.out.println("recebi pacotee stored");
                    this.peer.storedsRecieved++;
                }
                if(packetData.getType() == "PUTCHUNK"){
                    this.peer.store(packetData);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
