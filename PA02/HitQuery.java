package MyGnutella;

import java.io.Serializable;
import java.util.ArrayList;

/** This is the class to maintain the details of hit query
 *
 */

public class HitQuery implements Serializable {

	private static final long serialVersionUID = 1L;

	public ArrayList<PeerInfo> foundPeers = new ArrayList<>();

	public ArrayList<String> paths = new ArrayList<>();

}
