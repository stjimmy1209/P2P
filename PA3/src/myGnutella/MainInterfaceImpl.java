package myGnutella;
import java.io.*;
import java.util.*;
import java.rmi.*;
import java.rmi.server.*;

//Implementing interface MainInterface for RMI
public class MainInterfaceImpl extends UnicastRemoteObject implements MainInterface {

	private String mySDir; // <mySDir>/<ID>/<myDownDir>
	private int ID, currNodePort; // Peer ID + Port num
	private static final long versionID = 1L;	
	private PeerFunctions currNode;
	private ArrayList<String> interfaceMsg;

	// constructor parameters initialization
	MainInterfaceImpl(String dataDir, int ID, int currNodePort, PeerFunctions currNode) throws RemoteException {
		super();
		this.mySDir = dataDir;
		this.ID = ID;
		this.currNodePort=currNodePort;
		this.currNode = currNode;
		this.interfaceMsg=new ArrayList<>();
	}
	//RMI method to download file
	public synchronized byte[] getFile(String filename) throws RemoteException {
		byte[] bytesArr = null;	
		String fileNamePath = mySDir + "/" + filename;
		try {
			bytesArr = new byte[(int) (new File(fileNamePath)).length()];
			BufferedInputStream input = new BufferedInputStream(new FileInputStream(fileNamePath));
			input.read(bytesArr, 0, bytesArr.length);
			input.close();
		} catch (Exception e) {	System.out.println(e.getMessage());}		
		return bytesArr;
	}
	
	public synchronized FileProperties getFileInfo(String filename) throws RemoteException {
		return queryCurrNode(filename);
	}

	//RMI func for invalidation
	public void Invalidate(String msgID, int originNode, String filename, int versionNumber) throws RemoteException {
		
		synchronized(this){
			if (interfaceMsg.contains(msgID)){ return; }	
		} 		
		interfaceMsg.add(msgID);
		FileProperties info = queryCurrNode(filename);
		if(info != null){
			info.checkFState = "invalidate";
			System.out.println("Invalidate the file: "+ filename +" from Peer-" + ID);
		}	
		ArrayList<PeerProperties> nPeer = new ArrayList<PeerProperties>();
		ArrayList<ConnectNeighborThread> peerThreads = new ArrayList<ConnectNeighborThread>();
		currNode.neighboursList(nPeer, ID);	
		List<Thread> threads = new ArrayList<Thread>();
		for(PeerProperties neighbouringPeer : nPeer){
			if (neighbouringPeer.ID == originNode)
				continue;		
			ConnectNeighborThread ths = new ConnectNeighborThread(msgID, neighbouringPeer.ipAddr, originNode, neighbouringPeer.portNum, filename, versionNumber, "invalidate");
			Thread ts = new Thread(ths);
			ts.start();
			threads.add(ts);
			peerThreads.add(ths);
		}
		
		try {
			for (Thread thread: threads)
				thread.join();
		} catch (InterruptedException e) { 	e.printStackTrace();}
			
	}

	// RMI function for search query
	public GatherQuery queryFunction(String fName, int fromNodeId, String msgID, int TTL) throws RemoteException {
		
		ArrayList<PeerProperties> foundArr = new ArrayList<PeerProperties>();
		ArrayList<PeerProperties> nPeer = new ArrayList<PeerProperties>();
		ArrayList<FileProperties> fileInfo = new ArrayList<FileProperties>();
		ArrayList<String> path = new ArrayList<String>();
		GatherQuery peerQueryOut = new GatherQuery();
		ArrayList<ConnectNeighborThread> peerThreads = new ArrayList<ConnectNeighborThread>();
		
		synchronized(this){
			if (interfaceMsg.contains(msgID)){
				System.out.println("Request for Peer-"+ID+" coming from Peer-"+fromNodeId+" Action: No Action .... Duplicate request, already addressed with MsgId: " + String.valueOf(msgID));
				return peerQueryOut;	
			}
			if (TTL==0) {
				return peerQueryOut;	
			}
		} 
		interfaceMsg.add(msgID);
		System.out.println("Request for Peer-"+ID+" coming from Peer-"+fromNodeId+" Action: Search file on localhost and send request to the neighboring peers, MsgID: " + String.valueOf(msgID));
		FileProperties info = queryCurrNode(fName);
		if(info != null){
			System.out.println("Success: File found.");
			PeerProperties myObj = new PeerProperties();
			myObj.ipAddr = "localhost";
			myObj.ID = ID;
			myObj.portNum = currNodePort;
			foundArr.add(myObj);		
			fileInfo.add(info);
		}

		currNode.neighboursList(nPeer, ID);
		if (nPeer.size() == 0)
			path.add(Integer.toString(ID));
		
		TTL--;
		List<Thread> threads = new ArrayList<Thread>();
		for(PeerProperties neighbouringPeer : nPeer){
			if (neighbouringPeer.ID == fromNodeId)
				continue;
			ConnectNeighborThread ths = new ConnectNeighborThread(fName, neighbouringPeer.ipAddr, neighbouringPeer.portNum,ID, neighbouringPeer.ID, msgID, TTL, "download");
			Thread ts = new Thread(ths);
			ts.start();
			threads.add(ts);
			peerThreads.add(ths);
		}
			
		try {
			for (Thread thread: peerThreads)
				thread.join();
		} catch (InterruptedException e) { e.printStackTrace(); }

		
		for (ConnectNeighborThread peerThread: peerThreads){
			GatherQuery myObj =  peerThread.getResult();
			if(myObj.resultArr.size()>0)
				foundArr.addAll(myObj.resultArr);
			
			for (int count=0;count<myObj.pathArr.size();count++)
				path.add(ID + myObj.pathArr.get(count));
		}
		
		if (path.size()==0)
			path.add(Integer.toString(ID));
		peerQueryOut.resultArr.addAll(foundArr);
		peerQueryOut.pathArr.addAll(path);	
		return peerQueryOut;
	}

	//Query current peer for file
	private FileProperties queryCurrNode(String filename) {
		for (FileProperties info: currNode.localFiles)
			if (info.fName.equals(filename)) {
				return info;
			}
		return null;
	}


}
