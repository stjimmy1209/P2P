package myGnutella;
import java.util.*;

public class MainEntrance {

	public static void main(String args[]) {

        System.out.println("===========================================");
		System.out.println("=========== My Gnutella P2P System ========");
        System.out.println("===========================================");

        System.out.println("Please enter peer ID: ");
        Scanner scanner = new Scanner(System.in);
        String ID = scanner.nextLine();

	    PeerFunctions peerFunctions = new PeerFunctions();
		PeerFunctions getPeer = new PeerFunctions(Integer.parseInt(peerFunctions.readConfig().getProperty(ID +".id")),
                Integer.parseInt(peerFunctions.readConfig().getProperty(ID + ".port")));
		try {
			Scanner scan = new Scanner(System.in);
			while (true) {

				//Displaying menu for PUSH and PULL approaches
				if (getPeer.isPush){
                    System.out.println("===========================================");
					System.out.println("=========== My Gnutella P2P System ========");
					System.out.println("==================  PUSH  =================");
					System.out.println("Please choose from menu: ");
					System.out.println("1. Search and download a file.");
					System.out.println("2. Modify a file.");
					System.out.println("Exit. Exit system.");
				}
				else if (!getPeer.isPush){
                    System.out.println("===========================================");
					System.out.println("=========== My Gnutella P2P System ========");
					System.out.println("================  PULL  ===================");
					System.out.println("Please choose from menu: ");
					System.out.println("1. Search and download a file.");
					System.out.println("2. Modify a file.");
					System.out.println("3. Update the Peers.");
					System.out.println("Exit. Exit system");
				}
				switch (scan.next()) {
					// case 1-> shows search results for the file and displays the peers where the file exists to download
					case "1":
						System.out.println("Please enter the file name to search:");
						String fNameLookup = scan.next();
						ArrayList<PeerProperties> peerOutput = getPeer.searchFile(fNameLookup);
						System.out.println("" + fNameLookup + (peerOutput.size() == 0 ? " File does not exist.": " - File exists on peers:"));
						for (PeerProperties pOut: peerOutput)
							System.out.println(peerOutput.indexOf(pOut) + " -> Peer" + pOut.ID + " with IP:Port# = 127.0.0.1:" + pOut.portNum);
						// If the file found on the any of the peers, pass the peer name to download procedure
						if (peerOutput.size()>0)
							getPeer.downloadFunction(peerOutput, fNameLookup);
						break;

					//case 2-> to modify a given file					
					case "2":
						System.out.println("Please enter file name to modify:");
						String modifyFuncName = scan.next();
						getPeer.modifyFunction(modifyFuncName);
						break;

					// case 3-> In case of Pull approach, auto update File on the peers	
					case "3":
						if (!getPeer.isPush)
						System.out.println("Updating file from peers....");
							getPeer.updateFile();
						break;

					// case 4-> to exit from the program
					case "Exit":
						System.out.println("BYE!");
						System.exit(1);
					// default case -> Wrong Input

					default:
						System.out.println("INVALID INPUT!");
						break;
				}
			}
		}
		catch (Exception e) { System.out.println(e.getMessage()); }
	}
}
