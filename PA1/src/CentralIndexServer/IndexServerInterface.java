package CentralIndexServer;

//here we declare methods for server interface

import Peers.PeerClientInterFace;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IndexServerInterface extends Remote {

    boolean updatedClient(PeerClientInterFace peerClient) throws RemoteException;//used for keep the server updated when changed are made to peer

    String registerClient(PeerClientInterFace peerC) throws RemoteException;//used for registering client

    PeerClientInterFace[] lookupFile(String file, String connectedpeer) throws RemoteException;//to lookup the file in peers


}
