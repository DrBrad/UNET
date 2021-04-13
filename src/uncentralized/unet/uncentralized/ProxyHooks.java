package uncentralized.unet.uncentralized;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;

import static uncentralized.unet.uncentralized.Handlers.General.*;
import static uncentralized.unet.uncentralized.Variables.*;

public class ProxyHooks {

    public ProxyHooks(){
        String os = System.getProperty("os.name").toLowerCase();

        try{
            //WINDOWS
            if(os.contains("win")){

            //MAC
            }else if(os.contains("mac")){
                new ProcessBuilder("networksetup", "-setwebproxy", "\"Wi-fi\"", "127.0.0.1", "8080").start();
                new ProcessBuilder("networksetup", "-setwebproxystate", "\"Wi-fi\"", "On").start();

                //GET STATUS
                // networksetup -getwebproxy "Wi-Fi"
                tmp = new File("/Users/"+System.getProperty("user.name")+"/.unet/tmp");
                peerData = new File("/Users/"+System.getProperty("user.name")+"/.unet/PeerData");
                dataBase = new File("/Users/"+System.getProperty("user.name")+"/.unet/DataBase");

            //LINUX
            }else if(os.contains("nix") || os.contains("nux") || os.contains("aix")){

                //UBUNTU
                new ProcessBuilder("gsettings", "set", "org.gnome.system.proxy.http", "host", "127.0.0.1").start();
                new ProcessBuilder("gsettings", "set", "org.gnome.system.proxy.http", "port", "8080").start();
                new ProcessBuilder("gsettings", "set", "org.gnome.system.proxy", "mode", "'manual'").start();

                tmp = new File("/home/"+System.getProperty("user.name")+"/.unet/tmp");
                peerData = new File("/home/"+System.getProperty("user.name")+"/.unet/PeerData");
                dataBase = new File("/home/"+System.getProperty("user.name")+"/.unet/DataBase");

            //UNSURE - CANT SET PROXY
            }else{
                System.out.println("YOU GOT ONE WEIRD OS MY DUDE...");
            }

            //ONLY IF ITS THE FIRST TIME AND THE USER HAS NEVER LOGGED AN ACCOUNT...
            if(!readPreference("firstTime").equals("TRUE")){
                savePreference("firstTime", "TRUE");
                URI uri = new URI("http://me.unet");
                Desktop.getDesktop().browse(uri);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        createDirs();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    //WINDOWS
                    if(os.contains("win")){

                        //MAC
                    }else if(os.contains("mac")){
                        new ProcessBuilder("networksetup", "-setwebproxystate", "\"Wi-fi\"", "Off").start();

                        //LINUX
                    }else if(os.contains("nix") || os.contains("nux") || os.contains("aix")){
                        //UBUNTU
                        new ProcessBuilder("gsettings", "set", "org.gnome.system.proxy", "mode", "'none'").start();

                        //UNSURE - CANT SET PROXY
                    }else{
                        System.out.println("YOU GOT ONE WEIRD OS MY DUDE...");
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }));
    }

    public static void createDirs(){
        try{
            if(!tmp.exists()){
                tmp.mkdirs();
            }

            if(!peerData.exists()){
                peerData.mkdirs();
            }

            if(!dataBase.exists()){
                dataBase.mkdirs();
                File fprivate = new File(dataBase.getPath()+"/Private");
                fprivate.mkdirs();
            }

            File readme = new File(tmp.getParentFile().getPath()+"/READ-ME.txt");
            if(!readme.exists()){
                BufferedWriter writer = new BufferedWriter(new FileWriter(readme));
                writer.write("These files and folders will regenerate if you decide to delete any of them.\r\nPlease note that deleting the \"PeerData\" folder will lower your reputation significantly with the resolver,\r\nif you have too low of a score your site will be deleted.");
                writer.close();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
