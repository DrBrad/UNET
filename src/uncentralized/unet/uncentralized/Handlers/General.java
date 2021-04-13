package uncentralized.unet.uncentralized.Handlers;

import uncentralized.unet.uncentralized.Main;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.prefs.Preferences;

import static uncentralized.unet.uncentralized.Variables.*;

public class General {

    public static void savePreference(String key, String value){
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        prefs.put(key, value);
    }

    public static String readPreference(String key){
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        return prefs.get(key, "");
    }

    public static SRSocket quickConnectDNS(){
        return quickConnectPeer(dns, 8000);
    }

    public static SRSocket quickConnectPeer(String ip, int port){
        try{
            SRSocket socket = new SRSocket(true);
            socket.bind(new InetSocketAddress(0));
            socket.connect(new InetSocketAddress(ip, port), 10000);
            socket.startHandshake();

            return socket;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static void quickClose(Socket socket){
        try{
            if(!socket.isOutputShutdown()){
                socket.shutdownOutput();
            }
            if(!socket.isInputShutdown()){
                socket.shutdownInput();
            }

            socket.close();
        }catch(Exception e){
            //e.printStackTrace();
        }
    }
}
