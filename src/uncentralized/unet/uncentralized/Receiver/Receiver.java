package uncentralized.unet.uncentralized.Receiver;

import uncentralized.unet.uncentralized.Handlers.SRServerSocket;
import uncentralized.unet.uncentralized.Handlers.SRSocket;

import java.net.ServerSocket;
import java.net.Socket;

public class Receiver {

    public static void start(){
        new java.lang.Thread(new Runnable(){
            private SRSocket socket;

            @Override
            public void run(){
                try{
                    SRServerSocket serverSocket = new SRServerSocket(8081); //SHOULD BE 1080

                    System.out.println("Peer started on port 8081");

                    while((socket = serverSocket.accept()) != null){
                        (new Thread(socket)).start();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
