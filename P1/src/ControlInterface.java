import java.io.IOException;
import java.rmi.Remote;

public interface ControlInterface extends Remote{
    int backup(String file_name, String repl) throws IOException, InterruptedException;

    int delete(String arg) throws IOException, InterruptedException;

    int restore(String arg) throws IOException, InterruptedException;

    int reclaim(int size) throws IOException, InterruptedException;
}
