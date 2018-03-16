import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ControlInterface extends Remote{
    int backup(String file_name, String repl) throws RemoteException, IOException;
}
