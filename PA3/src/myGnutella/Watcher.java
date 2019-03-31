package myGnutella;
import java.io.*;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;

public class Watcher implements Runnable{
	WatchService watchDir;
	PeerFunctions myPeer;
	public Watcher(PeerFunctions myPeer, String fileDir) throws IOException{
		this.myPeer = myPeer;
		this.watchDir = FileSystems.getDefault().newWatchService();
		Path dir = Paths.get(fileDir);
		dir.register(watchDir, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
	}
	
	//Triggering invalidate msg if the file is modified
	private void triggerMsg(String fName) {
		for (FileProperties f : myPeer.localFiles) {
    		if(f.fName.equals(fName)) {
    			if (myPeer.isPush) {
	    			f.incrementVersionNum();
	    			Thread threadInstance = new Thread(new ConnectNeighborThread(myPeer.generateMessageId(), myPeer.getInfo.ipAddr, myPeer.getInfo.ID, myPeer.getInfo.portNum, fName.toString(), f.versionNum, "invalidate"));
	    			threadInstance.start();
	    			break;
    			} else {
    				f.fileLastModified = System.currentTimeMillis();
    			}
    		}
    	}
	}


	@Override
	public void run() {
		while (true) {
		    WatchKey key;
		    try {
		        key = watchDir.take();
		    } catch (InterruptedException e) { return; }
		 
		    for (WatchEvent<?> event : key.pollEvents()) {
		        WatchEvent.Kind<?> kind = event.kind();
		        @SuppressWarnings("unchecked")
		        WatchEvent<Path> ev = (WatchEvent<Path>) event;
		        Path fName = ev.context();
		        System.out.println(kind.name() + ": " + fName);
		        if (kind == ENTRY_CREATE) {
		        }
		        else if (kind == ENTRY_DELETE) {
		        	triggerMsg(fName.toString());
		        }
		        else if (kind == ENTRY_MODIFY) {
		        	triggerMsg(fName.toString());
		        }
		    }
		    boolean valid = key.reset();
		    if (!valid) { break; }
		}
		
	}
}
