package myGnutella;
import java.io.*;
import java.nio.file.*;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.*;
import java.util.*;

// All Peer related Operations/Procedures are written under PeerFunctions class
public class PeerFunctions {
	public Boolean isPush;
	private int TTL = 7;
	private int messageCtr = 0;
	private int TTR;	
	private String dataDir;	
	private MainInterface stub;
	private ArrayList<PeerProperties> peerOutput;
	public ArrayList<FileProperties> downloadFiles = new ArrayList<>();
	public ArrayList<FileProperties> localFiles = new ArrayList<>();
	public PeerProperties getInfo= new PeerProperties();

    public PeerFunctions() {
    }

    // Initializing Peer with Id and Port#
	PeerFunctions(int ID, int portNum) {
		this.getInfo.ID = ID;
		this.getInfo.portNum = portNum;
		this.getInfo.ipAddr = "127.0.0.1";
		this.dataDir = "data/" + ID;
		selectApproach();
		runAsServer(this.getInfo.ID, this.getInfo.portNum, this.dataDir);
		if (!isPush)
			autoUpdate();
	}
	
	// Setting approach from properties file
	private void selectApproach() {
		InputStream input;
		try {
			Properties prop = new Properties();
			input = new FileInputStream("config.properties");
			prop.load(input);
			isPush = prop.getProperty("approach").equalsIgnoreCase("push");		
			TTR = Integer.parseInt(prop.getProperty("TTR"));
		} catch (IOException e) { e.printStackTrace();}

	}

    public Properties readConfig() {
        Properties properties = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream("config.properties");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            assert input != null;
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
		
	// Search in nearby neighbours for the file
	public ArrayList<PeerProperties> searchFile(String fNameLookup) throws InterruptedException {
		List<Thread> threads = new ArrayList<Thread>();
		peerOutput = new ArrayList<>();
		ArrayList<PeerProperties> nPeer = new ArrayList<>();
		ArrayList<ConnectNeighborThread> threadingForNeighbour = new ArrayList<>();
		String msgId = generateMessageId();
		
		//get the list of neighbours for the given peer
		neighboursList(nPeer, getInfo.ID);			
		for (PeerProperties neighbourP: nPeer) {
			ConnectNeighborThread connection = new ConnectNeighborThread(fNameLookup, neighbourP.ipAddr, neighbourP.portNum, getInfo.ID, neighbourP.ID, msgId,TTL, "download");
			Thread threadInstance = new Thread(connection);
			threadInstance.start();		
			threads.add(threadInstance);
			threadingForNeighbour.add(connection);
		}
		for (Thread thread: threads)
			thread.join();
		for (ConnectNeighborThread neighbourTh: threadingForNeighbour) {
			GatherQuery hitQuery = neighbourTh.getResult();
			if (hitQuery.resultArr.size() > 0){
				peerOutput.addAll(hitQuery.resultArr);
			}
		}
		return peerOutput;
	}
	
	// Generate messages with ID
	public String generateMessageId(){
		++messageCtr;
		return System.currentTimeMillis()+ "-" + getInfo.ID + "-"+ messageCtr;
	}
		
	//Printing all neighbouring peers
	public void neighboursList(ArrayList<PeerProperties> neighborPeers, int peerId) {
		InputStream input = null;

		try {
			// Read configuration file
			Properties prop = new Properties();
			input = new FileInputStream("config.properties");
			prop.load(input);
			for (String myString: prop.getProperty(peerId + ".neighboursTopo").split(",")) {
				PeerProperties tempPeer = new PeerProperties();
				tempPeer.ID = Integer.parseInt(myString);
				tempPeer.portNum = Integer.parseInt(prop.getProperty(myString + ".port"));
				tempPeer.ipAddr = prop.getProperty(myString + ".ip");
				neighborPeers.add(tempPeer);
			}
			System.out.println();
			input.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	// To index all the files shared b/w Peers 
	public void getSharedFiles(String dataDir) {
		for (String file: Objects.requireNonNull((new File(dataDir)).list()))
			localFiles.add(new FileProperties(file, getInfo, TTR));
	}

	//Using RMI to make the Peer run as a Server
	public void runAsServer(int peerId, int portNum, String dataDir) {
		try {
			LocateRegistry.createRegistry(portNum);
			getSharedFiles(dataDir+"/sharedDir");
			stub = new MainInterfaceImpl(dataDir + "/sharedDir", peerId, portNum, this);
			Naming.rebind("rmi://localhost:" + portNum + "/peerServer", stub);
			System.out.println("Peer " + peerId + " has started at 127.0.0.1:" + portNum);	
			(new Thread(new Watcher(this, dataDir + "/sharedDir"))).start();
		} catch (Exception e) { System.out.println(e);}
	}
	
	private void autoUpdate() {
		new java.util.Timer().schedule( 
	        new java.util.TimerTask() {
	            @Override
	            public void run() {
	                updateFile();
	            }
	        }, 
	        TTR 
		);
	}
	
	public void updateFile() {
		for (FileProperties f : downloadFiles) {
    		MainInterface peerServer;
			try {
				peerServer = (MainInterface) Naming.lookup("rmi://localhost:" + f.myServer.portNum + "/peerServer");
				FileProperties info = peerServer.getFileInfo(f.fName);
				
				if (info.fileLastModified > f.fileLastModified) {
					System.out.println("Updating the file from other peers....");
					downloadFromPort(f.myServer.portNum, f.fName);
				}
				System.out.println("File is now up-to-date!");
			
			} catch (MalformedURLException | RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
        }
	}
	
	//File Modification function
	public void modifyFunction(String filename) {
		try {
		    Files.write(Paths.get(dataDir + "/sharedDir/" + filename), " ".getBytes(), StandardOpenOption.APPEND);
		}catch (IOException e) {
		    System.out.println(e);
		}
	}
	
	// Next 3 Functions: to download the file
	public void downloadFunction(ArrayList<PeerProperties> searchResult_Peers, String fName) {
		try {
			Scanner scan = new Scanner(System.in);
			System.out.println("\n Download file ?? (Y/N): ");
			switch (scan.next()) {
				case "y":
				case "Y":
					System.out.println("Please choose a peer: ");
					// trigger download function to download the file
					startDownloda(searchResult_Peers, searchResult_Peers.get(Integer.parseInt(scan.next())).ID, fName);
					System.out.println("File has been downloaded.");
					System.out.println();
					break;
				case "n":
				case "N":
				System.out.println("Download Canceled.");
				System.out.println();
					break;
				default:
					System.out.println("Invalid Input.");
					System.out.println();
					break;
			}
		} catch (Exception e) { System.out.println(e.getMessage());}
	}

	public FileProperties startDownloda(ArrayList<PeerProperties> peerOutput, int peerId, String fName) {
		FileProperties info = null;
		for (PeerProperties pOut : peerOutput) {
			if (peerId == pOut.ID) {	
				MainInterface peerServer;
				try {
					peerServer = (MainInterface) Naming.lookup("rmi://localhost:" + pOut.portNum + "/peerServer");
					info = peerServer.getFileInfo(fName);
					
					downloadFromPort(pOut.portNum, fName);
					info.newServer = pOut;
					downloadFiles.add(info);
					break;
					
				} catch (MalformedURLException | RemoteException | NotBoundException e) {
					e.printStackTrace();
				}
				
			}
		}
		return info;
	}
	
	private void downloadFromPort(int port, String fName) {
		try {
			MainInterface peerServer = (MainInterface) Naming.lookup("rmi://localhost:" + port + "/peerServer");
			byte[] out = peerServer.getFile(fName); // RMI fuction - getFile
			BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(dataDir  + "/downloadedFiles/" + fName));
			output.write(out, 0, out.length); // creating new file in downloadedFiles dir for the Peer
			
			output.flush();
			output.close();	
		} catch (NotBoundException | IOException e) { e.printStackTrace();}	
	}
	


}
