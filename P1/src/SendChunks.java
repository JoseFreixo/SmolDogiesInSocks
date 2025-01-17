import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Map;

public class SendChunks implements Runnable{
    DatagramPacket packet;
    MulticastSocket socket;
    PacketData packetData;
    Map<String,Integer> storedsRecieved;

    int maxTries = 5;
    int sleepingTime;
    public SendChunks(DatagramPacket packet, MulticastSocket socket, PacketData packetData, Map<String,Integer> storedsRecieved) {
        this.packet = packet;
        this.socket = socket;
        this.packetData = packetData;
        this.storedsRecieved = storedsRecieved;
    }

    @Override
    public void run() {
        int tries = 0;
        sleepingTime = 1000;
        while (tries < maxTries) {
            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(sleepingTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Storeds received "+storedsRecieved.get(packetData.getChunkNo()+packetData.getFileId()));
            synchronized(System.out) {
                if (storedsRecieved.get(packetData.getChunkNo() + packetData.getFileId()) >= Integer.parseInt(packetData.getRepl()))
                    break;
            }
            tries++;
            sleepingTime*=2;
        }
        storedsRecieved.remove(packetData.getChunkNo()+ packetData.getFileId());
    }
}
