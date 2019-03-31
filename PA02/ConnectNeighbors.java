package MyGnutella;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class ConnectNeighbors extends Thread{

	/**
	 * Class to handle the connection with neighbor peers
	 */
	
	int port;
	String ip;
	String fileName;
	String msgId;
	int fromPeerId;

	HitQuery hitQueryResult = new HitQuery();
	String toPeerId;

	ConnectNeighbors(String ip, int port, String fileName, String msgId, int fromPeerId, String toPeerId){

	this.ip=ip;
	this.port=port;
	this.fromPeerId=fromPeerId;
	this.fileName=fileName;
	this.msgId=msgId;
	this.toPeerId=toPeerId;

}
	@Override
	public void run() {
		PeerRemoteInterface peer;
			 try {
				 // setup the connection
				peer=(PeerRemoteInterface) Naming.lookup("rmi://"+ip+":"+port+"/peerServer");
				 // call remote method query
				hitQueryResult=peer.query(fromPeerId,msgId, fileName);			
			 } catch (MalformedURLException | RemoteException | NotBoundException e) {
			 	//e.printStackTrace();
				 System.out.println("Failed to connect to " + toPeerId +" : "+e.getMessage());
			}
		
	}
	public HitQuery getValue(){
		// Method to return the hit query result
		return hitQueryResult;
	}

}
