package myGnutella;

import java.io.Serializable;

public class FileProperties implements Serializable{
	
	private static final long versionID = 1L;
	public String checkFState, fName;
	public int versionNum, TTR;
	public PeerProperties myServer;
	public PeerProperties newServer;
	public long fileLastModified;
	public FileProperties(String s, PeerProperties nPeer, int ttr) {

		fName = s;
		versionNum = 0;
		myServer = nPeer;
		checkFState = "Valid";
		TTR = ttr;
		fileLastModified = System.currentTimeMillis();

	}


	public void incrementVersionNum() {
		versionNum += 1;
	}

}
