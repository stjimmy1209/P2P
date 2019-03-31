package MyGnutella;
import java.rmi.*;
//
// Interface for Remote method invocations
public interface PeerRemoteInterface extends Remote{

	 byte[] obtain(String filename)throws RemoteException;
	 HitQuery query(int fromPeerId,String msgId,String fileName)throws RemoteException;

}
