package Peers;

import CentralIndexServer.IndexServer;
import CentralIndexServer.IndexServerInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class PeerClient extends UnicastRemoteObject implements PeerClientInterFace, Runnable {

    private static final  String INDEXSERVER ="localhost";
    static String arg = null;

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException {
        if(args.length == 3){
            arg = args[1];

            String url = "rmi://" + INDEXSERVER + ":" + args[0] + "peerserver";
            IndexServer peerServer = (IndexServer) Naming.lookup(url);
            PeerClient clientserver = new PeerClient(args[2], args[1], peerServer);

            new Thread(new peerclientserver()).start();
            new Thread(clientserver).start();


        }
    }

    static class peerclientserver implements Runnable{

        public void run(){

            String url = "rmi://" + INDEXSERVER + ":" + arg + "clientserver";
            PeerClientServerInterface ps = null;
            try {
                ps = new PeerClientServer();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                Naming.rebind(url, ps);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

        }

    }


    private String name = null;
    private String portNo;
    private String[] files;
    private static final long serialVersionUID = 1L; //use the default serialVersionUID 1L
    private IndexServerInterface peerServer;
    private String filenameX;
    private String peerRootDir = null;

    // default constructor of PeerClient
    public PeerClient() throws RemoteException {
        super();
    }
    // constructor for PeerClient to initialize the peer name, peer port number and remote object in the indexserver
    public PeerClient(String name, String portNo, IndexServerInterface peerServer) throws RemoteException {
        this.name = name;
        this.portNo = portNo;
        this.peerServer = peerServer;
        this.peerRootDir = System.getProperty("initial");
        //get files in peer directory
        File fx = new File(peerRootDir);
        this.files = fx.list();

        //registering data structure
        System.out.println(peerServer.registerClient(this)); /**remains to be double checked */
        new Thread(new Listener(this)).start();


    }

    // implementations of the methods in PeerClientInterface
    public String getPortNo(){
        return portNo;
    }

    public String getPeerName(){
        return name;
    }

    @Override
    public String[] retrieveFile() throws RemoteException {
        return files;
    }

    @Override
    public String getPeerDirec(){
        return peerRootDir;
    }

    /**
     * Here we use syncServer method to get the index server updated with the information on peers.
     **/
    @Override
    public synchronized void syncServer() throws RemoteException {

        File file = new File(peerRootDir);
        this.files = file.list();

        if(peerServer.updatedClient(this)){
            System.out.println("Index Server has been synchronized.");
        }

    }

    @Override
    public void run() {

        /**
         * Here we read the command line message.
         * */

        String command;
        String option;
        String filename;
        Scanner input = new Scanner(System.in);

        System.out.println("************************");
        System.out.println("P2P File Sharing System");
        System.out.println("************************");
        System.out.println("Press 1 to download.");

        while(true){
            command = input.nextLine();
            CharSequence s = " ";
            option = command.substring(0, command.indexOf(' '));
            filename = command.substring(command.indexOf(' ') + 1);

            filenameX = filename;
            if(option.equals(1)){
                PeerClientInterFace[] peer = findFile(filename);
                int selection = input.nextInt();
                
                PeerClientServerInterface peerClientServerInterface = null;
                
                try {
                    String url = "rmi://localhost:"+peer[selection-1].getPortNo()+"/clientserver";
                    try {
                        peerClientServerInterface = (PeerClientServerInterface) Naming.lookup(url);
                    } catch (NotBoundException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                downLoaded(peerClientServerInterface, filename);
                timeCost();
            }
            

        }


    }

    public synchronized PeerClientInterFace[] findFile(String filename){

        PeerClientInterFace[] peer = null;
        try {
            peer = peerServer.lookupFile(filename, name);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if(peer != null){
            System.out.println("Peers with this file is listed:");
            for (int i = 0; i < peer.length; i++) {

                if (peer[i] != null) {
                    try {
                        System.out.println((i+1)+". "+peer[i].getPeerName());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

            }
            System.out.println("Choose a peer:");
            return peer;


        }else{
            System.out.println("File not found!");
        }
        return null;

    }

    public synchronized boolean downloading(String filename, byte[] data, int len){

        System.out.println("File is not being downloaded.");

        File file = new File(filename);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileOutputStream output = new FileOutputStream(file, true);
            try {
                output.write(data,0,len);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void downLoaded(PeerClientServerInterface peer, String filename) {

        try {
            if (peer.sendFile(this, filename)) {
                System.out.println("Downloading Succeeded!");

            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    
    public void timeCost(){
        
        long timeCost = 0;
        long timeEnd = 0;
        for (int i = 0; i < 1000; i++) {

            long timeStart = System.currentTimeMillis();
            timeEnd = System.currentTimeMillis()-timeStart;
            timeCost = timeCost + timeEnd;
        }

        System.out.println("Time cost on peer is " + timeCost/1000.000 + "ms.");
    }
}
