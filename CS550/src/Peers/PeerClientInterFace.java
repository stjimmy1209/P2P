package Peers;
// in this part, interfaces for the client side of the peers are declared.
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PeerClientInterFace extends Remote {

    String getPortNo () throws RemoteException;
    String getPeerDirec() throws RemoteException;
    String getPeerName() throws RemoteException;
    String[] retrieveFile() throws RemoteException;
    void syncServer() throws RemoteException;
    boolean downloading(String filename, byte[] data, int len);



}
