package uncentralized.unet.uncentralized.Proxy;

import org.json.JSONObject;

import java.net.ServerSocket;
import java.net.Socket;

import static uncentralized.unet.uncentralized.Send.Script.getSettings;

public class Proxy {

    public static void start(){
        JSONObject settings = new JSONObject(getSettings());
        boolean mydev = (settings.getString("mydev").equals("on")) ? true : false;

        new java.lang.Thread(new Runnable(){
            private Socket socket;

            @Override
            public void run(){
                try{
                    ServerSocket serverSocket = new ServerSocket(settings.getInt("port")); //SHOULD BE 1080
                    System.out.println("Proxy started on port "+settings.getInt("port"));

                    while((socket = serverSocket.accept()) != null){
                        if(mydev){
                            if(socket.getRemoteSocketAddress().toString().startsWith("/127.0.0.1")){
                                (new Thread(socket)).start();
                            }else{
                                socket.close();
                            }
                        }else{
                            (new Thread(socket)).start();
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
