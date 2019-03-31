package myGnutella;
import java.io.Serializable;
// PeerProperties class -> gives versionID, ID, port# and ipAddr
public class PeerProperties implements Serializable {

	private static final long versionID = 1L;
	public int ID;	
	public String ipAddr;
	public int portNum;

}
