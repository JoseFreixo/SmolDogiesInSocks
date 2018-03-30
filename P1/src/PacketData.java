import java.net.DatagramPacket;
import java.util.Arrays;

public class PacketData {
    public String[] packetSplit;
    public byte[] body;
    //STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
    //PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
    //DELETE <Version> <SenderId> <FileId> <CRLF><CRLF>
    //GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
    //CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
    //REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
    public PacketData(DatagramPacket packet) {
        byte[] data = Arrays.copyOf(packet.getData(), packet.getLength());
        byte[] remainder = null;
        for (int i = 0; i < data.length; i++) {
           if(data[i] == '\r' && data[i+1] == '\n' && data[i+2] == '\r' && data[i+3] == '\n'){
               body = Arrays.copyOfRange(data, i+4, data.length);
               remainder = Arrays.copyOfRange(data, 0, i-1);
               break;
           }
        }
        String header = new String(remainder).trim();
        packetSplit = header.split(" ");
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

    public String getRepl(){
        return packetSplit[5];
    }

    public byte[] getBody(){
        return body;
    }
}
