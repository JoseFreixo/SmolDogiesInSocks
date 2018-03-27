import java.net.DatagramPacket;
import java.nio.channels.DatagramChannel;

public class PacketData {
    public String[] packetSplit;
    //STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
    //PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
    public PacketData(DatagramPacket packet) {
        String packetInfo = new String(packet.getData());
        this.packetSplit = packetInfo.split(" ");
    }

    public String getType(){
        return packetSplit[0];
    }
    public String getVersion(){
        return packetSplit[1];
    }
    public String getSenderId(){
        return packetSplit[2];
    }
    public String getFileId(){
        return packetSplit[3];
    }
    public String getChunkNo(){
        return packetSplit[4];
    }

    public byte[] getBody(){
        return packetSplit[7].getBytes();
    }

}
