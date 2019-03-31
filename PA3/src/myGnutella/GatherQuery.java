package myGnutella;
import java.io.*;
import java.util.*;

//COllecting Hit Query Result class
public class GatherQuery implements Serializable {
	private static final long versionID = 1L;
	public ArrayList<PeerProperties> resultArr = new ArrayList<PeerProperties>();
	public ArrayList<String> pathArr = new ArrayList<String>();
}
