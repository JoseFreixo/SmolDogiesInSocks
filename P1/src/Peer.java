import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class Peer {
    private int id;
    private InetAddress mdc_ip;
    private int mdc_port;
    private InetAddress mcc_ip;
    private int mcc_port;

    public void main(String[] args) throws IOException {
        if (args.length == 5) {
            boot_peer(args);
            listen();
        } else if (args.length == 7) {
            boot_peer(args);
            send_file();
        } else {
            System.out.println("Wrong arguments! Use one of the following:");
            System.out.println("java Peer <id> <MDC_ip> <MDC_port> <MCC_id> <MCC_port>");
            System.out.println("java Peer <id> <MDC_ip> <MDC_port> <MCC_id> <MCC_port> <file_name> <repl>");
        }
        return;
    }

    public void boot_peer(String[] args) throws IOException {
        id = Integer.parseInt(args[0]);
        mdc_ip = InetAddress.getByName(args[1]);
        mdc_port = Integer.parseInt(args[2]);
        mcc_ip = InetAddress.getByName(args[3]);
        mcc_port = Integer.parseInt(args[4]);

        MulticastSocket mdc_socket = new MulticastSocket(mdc_port);
        MulticastSocket mcc_socket = new MulticastSocket(mcc_port);

        mdc_socket.joinGroup(mdc_ip);
        mcc_socket.joinGroup(mcc_ip);
    }

    private void listen() {
        while (true){

            if (false)
                break;
        }
        return;
    }

    public void send_file() throws IOException {
        return;
    }
}
