package CentralIndexServer;

/**
 * This part is the main design of the index server in which files in peers would be indexed with Arraylist.
 * And real time status of files would be synchronized
 * Peers would register all of the files they each have to the index server
 */

import Peers.PeerClientInterFace;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;


public class IndexServer extends UnicastRemoteObject implements IndexServerInterface{

    protected IndexServer() throws RemoteException {
        super();
    }

    public static void main(String[] args) throws MalformedURLException, RemoteException {

        IndexServer indexserver = new IndexServer();
        /**
         * Here we use class Naming which is even more convenient than class Registry.
         * */
        Naming.rebind("rmi//localhost:" + args[0] + "/indexserver",indexserver);//associate remote object with a new name
        System.out.println("************************");    // this part is the status UI of the central index server
        System.out.println("P2P File Sharing System");
        System.out.println("************************");
        System.out.println("MESSAGE: INDEX SERVER IS NOW UP AND RUNNING.");

    }



    private static final long serialVersionUID = 1L;
    // use an arraylist to store the peers that is available
    private ArrayList<PeerClientInterFace> peerclients = new ArrayList<PeerClientInterFace>();

    @Override
    public synchronized String registerClient(PeerClientInterFace peerC) throws RemoteException {
        peerclients.add(peerC);
        String peersfiles = " ";
        String[] files = peerC.retrieveFile();

        for (int i = 0; i < files.length; i++) {
            peersfiles = peersfiles + "\n" + files[i];
        }

        System.out.println("Peers has been registered.");
        return peerC.getPeerName()
                +"has been registered to the Index Server.\n They have the following files:\n"
                + peersfiles;

    }

    @Override
    public synchronized PeerClientInterFace[] lookupFile(String file, String connectedpeer) throws RemoteException {

        //define a default condition for the result of searching
        Boolean found = false;
        PeerClientInterFace[] peer = new PeerClientInterFace[peerclients.size()];

        int a = 0;
        for (int i = 0; i < peerclients.size(); i++) {

            String[] listoffiles = peerclients.get(i).retrieveFile();
            for (int j = 0; j < listoffiles.length; j++) {

                if(file.equals(listoffiles[j]))
                {
                    found = true;
                    peer[a] = peerclients.get(i);
                    a++;
                }

            }

        }

        if(found == true) {
            System.out.println(connectedpeer + "\twill receive the needed file" + file);

            return peer;
        } else {
            System.out.println("Search Failed.");

            return null;
        }


    }

    @Override
    public boolean updatedClient(PeerClientInterFace peerClient) throws RemoteException {
        //this part update the server with the changes to the files
        for (int i=0; i<peerclients.size(); i++ ) {

            if(peerClient.getPeerName().equals(peerclients.get(i).getPeerName())){
                peerclients.remove(i);
                peerclients.add(peerClient);
            }
        }
        System.out.println("\nServer is now updated.");
        return true;
    }





}
