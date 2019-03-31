package myGnutella;
import java.rmi.*;
import java.util.Properties;

public interface MainInterface extends Remote {

	 byte[] getFile(String filename) throws RemoteException;
	 FileProperties getFileInfo(String filename) throws RemoteException;
	 GatherQuery queryFunction(String filename, int fromPeerId, String msgId, int TTL) throws RemoteException;
	 void Invalidate(String msgID, int originNode, String filename, int versionNumber) throws RemoteException;

}
