package Peers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class PeerClientServer extends UnicastRemoteObject implements PeerClientServerInterface {


    protected PeerClientServer() throws RemoteException {
        super();
    }

    @Override
    public boolean sendFile(PeerClientInterFace peerClient, String Filename) {

        File file = new File(Filename);
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        byte[] data = new byte[2000 * 2000];

        int len = 0;
        try {
            len = input.read(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(len>0){
            if(peerClient.downloading(file.getName(), data, len)){
                try {
                    System.out.println("File '"+file+"' has been sent to Requesting Peer: "+peerClient.getPeerName());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                try {
                    len = input.read(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Fault: File was NOT sent");
            }
        }

        return true;
    }
}
