package Peers;

import java.io.IOException;
import java.nio.file.*;
import java.rmi.RemoteException;

public class Listener implements Runnable{

    private PeerClientInterFace peerClient;

    public Listener(PeerClient peerClient) {

        this.peerClient = peerClient;

    }

    public void run() {

        WatchService watch = null;
        try {
            watch = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Path direc = Paths.get(peerClient.getPeerDirec());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        WatchKey watchKey;

        while (true) {

            try {
                watchKey = watch.take();
            } catch (InterruptedException e) {
                return;
            }


            boolean updateNewFile = false;
            for (WatchEvent<?> event : watchKey.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                // This key is registered only for ENTRY_CREATE, ENTRY_DELETE, and ENTRY_MODIFY events,
                // but an OVERFLOW event can occur regardless if events are lost or discarded.
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }
                if (kind == StandardWatchEventKinds.ENTRY_DELETE || kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    try {
                        peerClient.syncServer();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    if (updateNewFile) {
                        try {
                            peerClient.syncServer();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    } else
                        updateNewFile = true;


                }

            }

            boolean valid = watchKey.reset();
            if (!valid) {
                break;
            }

        }
    }
}