import java.io.IOException;

public class PeerStore extends Peer implements Runnable{
    String file_name;
    String repl;
    public PeerStore(PacketData packetData) {
        super();
    }

    @Override
    public void run() {
        System.out.println(id + " is Storing");
        /*Path newfile = Paths.get("Copy of " + file_name);
        Files.write(newfile, body);*/
    }
}
