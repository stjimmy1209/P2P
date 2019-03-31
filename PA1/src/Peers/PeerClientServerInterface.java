package Peers;

import java.rmi.Remote;
import java.rmi.RemoteException;

//In this part, interfaces for the server side of the peers are defined.
public interface PeerClientServerInterface extends Remote {

    boolean sendFile(PeerClientInterFace peerClient, String Filename) throws RemoteException;

}
