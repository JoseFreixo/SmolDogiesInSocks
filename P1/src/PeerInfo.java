public class PeerInfo {
    protected String host;
    protected int port = 1099;
    protected String peerId;

    public PeerInfo(String info) {
        if (info.startsWith("//")){
            String noBars = info.substring(2);
            String division[] = noBars.split("/");
            String hostPort[] = division[0].split(":");
            if (hostPort.length > 1)
                port = Integer.parseInt(hostPort[1]);
            host = hostPort[0];
            peerId = division[1];
        } else {
            host = null;
            peerId = info;
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPeerId() {
        return peerId;
    }
}
