package uncentralized.unet.uncentralized.Send;

import org.json.JSONArray;
import org.json.JSONObject;
import uncentralized.unet.uncentralized.Handlers.SRSocket;
import uncentralized.unet.uncentralized.Main;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;

import static uncentralized.unet.uncentralized.Handlers.General.*;
import static uncentralized.unet.uncentralized.Handlers.Parser.*;
import static uncentralized.unet.uncentralized.Send.Script.*;
import static uncentralized.unet.uncentralized.Variables.*;

public class Updates {

    public static void start(){
        update();
        checkResolverUpdates();
    }

    public static void update(){
        try{
            String check = readPreference("updating");
            if(check.equals("")){
                if(versionCode < Integer.parseInt(getData())){
                    System.out.println("Updating Uncentralized...");
                    savePreference("updating", "true");
                    File current = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());
                    File update = new File(current.getParentFile()+"/Update.jar");

                    if(update.exists()){
                        update.delete();
                    }

                    BufferedInputStream in = new BufferedInputStream(new URL("https://uncentralized.com/get.php?t=computer").openStream()); //

                    FileOutputStream fileOutputStream = new FileOutputStream(update.getPath());
                    byte dataBuffer[] = new byte[1024];
                    int bytesRead;
                    while((bytesRead = in.read(dataBuffer, 0, 1024)) != -1){
                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                    }

                    if(update.exists()){
                        current.delete();
                        update.renameTo(current);
                    }

                    ProcessBuilder pb = new ProcessBuilder("java", "-jar", current.getPath());
                    pb.redirectErrorStream(true);
                    pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                    pb.start();
                    System.exit(0);
                }
            }else{
                savePreference("updating", "");
            }
        }catch(Exception e){
            //e.printStackTrace();
        }
    }

    public static void checkResolverUpdates(){
        JSONObject settings = new JSONObject(getSettings());
        if(settings.getString("npc").equals("on")){
            String check = readPreference("me");
            if(!check.equals("")){
                JSONObject me = new JSONObject(check);
                SRSocket resolver = quickConnectDNS();

                resolver.sendEncryptedText("Request-Type: Updates\r\n" +
                        "Domain: "+me.getString("d")+"\r\n" +
                        "Key: "+me.getString("p"));

                String[] protocol = customProtocol(resolver.receiveDecryptedText());
                quickClose(resolver);
                if(protocol[0].equals("Proceed")){
                    JSONArray json = new JSONArray(protocol[1]);
                    if(json.length() > 0){
                        for(int i = 0; i < json.length(); i++){
                            File file = new File(peerData.getPath()+"/"+json.getString(i));
                            if(file.exists()){
                                file.delete();
                            }
                        }
                    }
                }
            }
        }
    }

    public static String getData()throws Exception{
        URL obj = new URL("https://uncentralized.com/check.php?t=computer");
        HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();

        conn.setUseCaches(false);
        conn.setRequestMethod("POST");

        conn.setDoOutput(true);
        conn.setDoInput(true);

        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes("");

        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while((inputLine = in.readLine()) != null){
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }
}
