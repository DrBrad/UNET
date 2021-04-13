package uncentralized.unet.uncentralized;

import uncentralized.unet.uncentralized.Handlers.Mimes;
import uncentralized.unet.uncentralized.Proxy.Proxy;
import uncentralized.unet.uncentralized.Receiver.Receiver;
import uncentralized.unet.uncentralized.Send.Updates;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.*;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.prefs.Preferences;

import static uncentralized.unet.uncentralized.Variables.*;

public class Main {

    //ALL WE NEED NOW IS TO LET
    // ALLOW BACKEND TO SET SET SESSION AND SOME HEADERS
    // FRONT END JS CANNOT USE // unless its in seprate JS file

    // MAKE THE APP BETTER
    // IMPLEMENT AES EVERYWHERE
    // THE REVERSER PROTOCOL
    // FINALLY THE CONCEPT OF HUFFMAN COMPRESSION

    public static void main(String args[]){
        System.out.println(" _    _                      _             _ _             _ ");
        System.out.println("| |  | |                    | |           | (_)           | |");
        System.out.println("| |  | |_ __   ___ ___ _ __ | |_ _ __ __ _| |_ _______  __| |");
        System.out.println("| |  | | '_ \\ / __/ _ \\ '_ \\| __| '__/ _` | | |_  / _ \\/ _` |");
        System.out.println("| |__| | | | | (_|  __/ | | | |_| | | (_| | | |/ /  __/ (_| |");
        System.out.println(" \\____/|_| |_|\\___\\___|_| |_|\\__|_|  \\__,_|_|_/___\\___|\\__,_|");
        System.out.println();

        new Mimes();

        for(String dns : udns){
            if(isOnline(dns)){
                try{
                    InetAddress address = InetAddress.getByName(dns);
                    resolvers.add(address.getHostAddress());
                }catch(Exception e){
                }
            }
        }

        if(resolvers.size() > 0){
            dns = resolvers.get(0);
            System.out.println("Resolver: "+dns);

            Updates.start();
            Receiver.start();
            Proxy.start();

            new ProxyHooks();
        }
    }

    public static boolean isOnline(String url){
        try{
            InetAddress address = InetAddress.getByName(new URL("http://"+url).getHost());
            InetSocketAddress sa = new InetSocketAddress(address.getHostAddress(), 8000);
            Socket ss = new Socket();
            ss.setSoTimeout(100);
            ss.connect(sa, 100);
            ss.close();
            return true;
        }catch(Exception e){
            return false;
        }
    }

    public static void checkLegitimacy(){
        try{
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");

            File client = new File("/home/brad/Downloads/products-page.png");

            byte[] buffer = new byte[4096];
            InputStream is = new FileInputStream(client);

            int length;
            while((length = is.read(buffer)) > 0){
                messageDigest.update(buffer, 0, length);
            }

            String key = Base64.getEncoder().encodeToString(messageDigest.digest());

            System.out.println(key);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void kill(){
        try{
            Preferences prefs = Preferences.userNodeForPackage(Main.class);
            String[] keys = prefs.keys();

            for(String key : keys){
                prefs.remove(key);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}