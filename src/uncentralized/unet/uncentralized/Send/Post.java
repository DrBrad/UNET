package uncentralized.unet.uncentralized.Send;

import org.json.JSONArray;
import org.json.JSONObject;
import uncentralized.unet.uncentralized.Handlers.SRSocket;

import java.io.File;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import static uncentralized.unet.uncentralized.Handlers.FileHandler.*;
import static uncentralized.unet.uncentralized.Handlers.General.*;
import static uncentralized.unet.uncentralized.Handlers.Parser.*;
import static uncentralized.unet.uncentralized.Handlers.Protocol.sendErrorHeader;
import static uncentralized.unet.uncentralized.Handlers.Protocol.sendHeader;
import static uncentralized.unet.uncentralized.Handlers.Protocol.sendRedirectHeader;
import static uncentralized.unet.uncentralized.Variables.*;

public class Post {

    //THIS IS FOR CLEANING UP AFTER UPLOADING
    public static boolean local(String location, JSONObject data)throws Exception {
        boolean result = localUpload(location, data);

        Iterator keys = data.keys();
        while(keys.hasNext()){
            String key = (String)keys.next();
            Object value = data.get(key);

            if(value instanceof JSONObject){
                File file = new File(tmp.getPath()+"/"+((JSONObject) value).getString("tmp")),
                        encrypted = new File(tmp.getPath()+"/"+((JSONObject) value).getString("tmp")+".e");

                if(file.exists()){
                    file.delete();
                }

                if(encrypted.exists()){
                    encrypted.delete();
                }

                for(int j = 1; j < 4; j++){
                    File tmp = new File(encrypted.getPath()+"part"+j);
                    if(tmp.exists()){
                        tmp.delete();
                    }
                }

            }else if(value instanceof JSONArray){
                JSONArray jvalue = ((JSONArray) value);
                for(int i = 0; i < jvalue.length(); i++){
                    File file = new File(tmp.getPath()+"/"+jvalue.getJSONObject(i).getString("tmp")),
                            encrypted = new File(tmp.getPath()+"/"+jvalue.getJSONObject(i).getString("tmp")+".e");

                    if(file.exists()){
                        file.delete();
                    }

                    if(encrypted.exists()){
                        encrypted.delete();
                    }

                    for(int j = 1; j < 4; j++){
                        File tmp = new File(encrypted.getPath()+"part"+j);
                        if(tmp.exists()){
                            tmp.delete();
                        }
                    }
                }
            }
        }

        return result;
    }

    //BETTER --
    public static boolean localUpload(String location, JSONObject data)throws Exception {
        JSONObject me = new JSONObject(readPreference("me"));
        String[] path = parsePath(location);

        Iterator keys = data.keys();
        while(keys.hasNext()){
            String key = (String)keys.next();
            Object value = data.get(key);

            if(value instanceof JSONObject){
                File file = new File(tmp.getPath()+"/"+((JSONObject) value).getString("tmp")),
                        encrypted = new File(tmp.getPath()+"/"+((JSONObject) value).getString("tmp")+".e");

                if(file.exists()){
                    data.getJSONObject(key).put("key", split(file, encrypted));
                    data.getJSONObject(key).put("size", encrypted.length());
                    data.getJSONObject(key).put("rsize", file.length());
                }else{
                    return false;
                }

            }else if(value instanceof JSONArray){
                JSONArray jvalue = ((JSONArray) value);
                for(int i = 0; i < jvalue.length(); i++){
                    File file = new File(tmp.getPath()+"/"+jvalue.getJSONObject(i).getString("tmp")),
                            encrypted = new File(tmp.getPath()+"/"+jvalue.getJSONObject(i).getString("tmp")+".e");

                    if(file.exists()){
                        data.getJSONArray(key).getJSONObject(i).put("key", split(file, encrypted));
                        data.getJSONArray(key).getJSONObject(i).put("size", encrypted.length());
                        data.getJSONArray(key).getJSONObject(i).put("rsize", file.length());
                    }else{
                        return false;
                    }
                }
            }
        }

        System.out.println(data.toString());

        //IN THE CASE OF FAIL DELETE FILES>............................................................................................................................

        SRSocket resolver = quickConnectDNS();

        resolver.sendEncryptedText("Request-Type: Upload-L\r\n" +
                "Domain: "+me.getString("d")+"\r\n" +
                "Key: "+me.getString("p")+"\r\n" +
                "Folder: "+path[0]+"\r\n" +
                "Data: "+data.toString());

        String[] protocol = customProtocol(resolver.receiveDecryptedText());
        if(protocol[0].startsWith("Proceed")){
            data = new JSONObject(protocol[1]);

            if(upload(resolver, data)){
                return true;
            }
        }

        quickClose(resolver);
        return false;
    }

    //THIS IS FOR CLEANING UP AFTER UPLOADING
    public static void online(URL url, JSONObject data, OutputStream out)throws Exception {
        onlineUpload(url, data, out);

        Iterator keys = data.getJSONObject("f").keys();
        while(keys.hasNext()){
            String key = (String)keys.next();
            Object value = data.getJSONObject("f").get(key);

            if(value instanceof JSONObject){
                File file = new File(tmp.getPath()+"/"+((JSONObject) value).getString("tmp")),
                        encrypted = new File(tmp.getPath()+"/"+((JSONObject) value).getString("tmp")+".e");

                if(file.exists()){
                    file.delete();
                }

                if(encrypted.exists()){
                    encrypted.delete();
                }

                for(int j = 1; j < 4; j++){
                    File tmp = new File(encrypted.getPath()+"part"+j);
                    if(tmp.exists()){
                        tmp.delete();
                    }
                }

            }else if(value instanceof JSONArray){
                JSONArray jvalue = ((JSONArray) value);
                for(int i = 0; i < jvalue.length(); i++){
                    File file = new File(tmp.getPath()+"/"+jvalue.getJSONObject(i).getString("tmp")),
                            encrypted = new File(tmp.getPath()+"/"+jvalue.getJSONObject(i).getString("tmp")+".e");

                    if(file.exists()){
                        file.delete();
                    }

                    if(encrypted.exists()){
                        encrypted.delete();
                    }

                    for(int j = 1; j < 4; j++){
                        File tmp = new File(encrypted.getPath()+"part"+j);
                        if(tmp.exists()){
                            tmp.delete();
                        }
                    }
                }
            }
        }
    }

    //BETTER --
    public static void onlineUpload(URL url, JSONObject data, OutputStream out)throws Exception {
        String[] path = parsePath(url.getPath());

        //PUT THIS BACK TO THE OLD METHOD AS WE DONT NEED THE SECRET AT ALL...
        Iterator keys = data.getJSONObject("f").keys();
        while(keys.hasNext()){
            String key = (String)keys.next();
            Object value = data.getJSONObject("f").get(key);

            if(value instanceof JSONObject){
                File file = new File(tmp.getPath()+"/"+((JSONObject) value).getString("tmp")),
                        encrypted = new File(tmp.getPath()+"/"+((JSONObject) value).getString("tmp")+".e");

                if(file.exists()){
                    //split(file, encrypted, ((JSONObject) value).getString("name"));
                    data.getJSONObject("f").getJSONObject(key).put("key", split(file, encrypted));
                    data.getJSONObject("f").getJSONObject(key).put("size", encrypted.length());
                    data.getJSONObject("f").getJSONObject(key).put("rsize", file.length());
                }else{
                    sendErrorHeader(out);
                    return;
                }

            }else if(value instanceof JSONArray){
                JSONArray jvalue = ((JSONArray) value);
                for(int i = 0; i < jvalue.length(); i++){
                    File file = new File(tmp.getPath()+"/"+jvalue.getJSONObject(i).getString("tmp")),
                            encrypted = new File(tmp.getPath()+"/"+jvalue.getJSONObject(i).getString("tmp")+".e");

                    if(file.exists()){
                        //split(file, encrypted, jvalue.getJSONObject(i).getString("name"));
                        data.getJSONObject("f").getJSONArray(key).getJSONObject(i).put("key", split(file, encrypted));
                        data.getJSONObject("f").getJSONArray(key).getJSONObject(i).put("size", encrypted.length());
                        data.getJSONObject("f").getJSONArray(key).getJSONObject(i).put("rsize", file.length());
                    }else{
                        sendErrorHeader(out);
                        return;
                    }
                }
            }
        }

        //IN THE CASE OF FAIL DELETE FILES>............................................................................................................................

        SRSocket resolver = quickConnectDNS();

        resolver.sendEncryptedText("Request-Type: Upload-O\r\n" +
                "Domain: "+url.getHost().split("\\.")[0]+"\r\n" +
                "Folder: "+path[0]+"\r\n" +
                "File-name: "+path[1]+"\r\n" +
                "Data: "+data.toString());

        String[] protocol = customProtocol(resolver.receiveDecryptedText());
        if(protocol[0].startsWith("Proceed")){
            data = new JSONObject(protocol[1]);

            if(data.has("f")){
                if(upload(resolver, data.getJSONObject("f"))){
                    String cookies = (data.has("c")) ? data.getString("c") : "";
                    if(data.has("m")){
                        sendRedirectHeader(out, data.getString("m"), cookies);
                        out.flush();
                    }else{
                        sendHeader(out, "text/html", cookies);
                        out.write(data.getString("r").getBytes());
                        out.flush();
                    }
                }else{
                    sendErrorHeader(out);
                }
            }else{
                sendHeader(out, "text/html", "");
            }
        }else{
            sendErrorHeader(out);
        }
        quickClose(resolver);
    }

    // NEEEDS SOME WORK --
    public static boolean upload(SRSocket resolver, JSONObject data)throws Exception {
        boolean proceed = true;
        ArrayList<SRSocket> peers = new ArrayList<>();

        JSONObject full = new JSONObject();

        Iterator keys = data.keys();
        while(keys.hasNext()){
            String key = (String)keys.next();

            File encrypted = new File(tmp.getPath()+"/"+key+".e");

            JSONObject parts = new JSONObject();
            parts.put("1", new JSONArray());
            parts.put("2", new JSONArray());
            parts.put("3", new JSONArray());

            for(int i = 0; i < data.getJSONArray(key).length(); i++){
                int part = (i%3)+1;
                JSONObject peerInfo = data.getJSONArray(key).getJSONObject(i);

                try{
                    SRSocket peer = quickConnectPeer(peerInfo.getString("i"), 8081);

                    peer.sendEncryptedText("Request-Type: Upload-P2P\r\n" +
                            "Key: "+peerInfo.getString("c")+"\r\n" +
                            "Part: "+part+"\r\n" +
                            "Size: "+new File(encrypted.getPath()+"part"+part).length());

                    String[] protocol = customProtocol(peer.receiveText());
                    if(protocol[0].equals("Proceed")){
                        if(sendFile(peer, new File(encrypted.getPath()+"part"+part))){
                            parts.getJSONArray(part+"").put(peerInfo.getString("i"));
                            peers.add(peer);
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            if(parts.getJSONArray("1").length() > 0 &&
                    parts.getJSONArray("2").length() > 0 &&
                    parts.getJSONArray("3").length() > 0){ //THIS WILL NEED TO BE SET HIGHER IN THE FUTURE...
                full.put(key, parts);
            }else{
                proceed = false;
                break;
            }
        }

        Thread.sleep(500);

        if(proceed){
            for(SRSocket peer : peers){
                peer.sendText("Response: Proceed");
            }

            resolver.sendEncryptedText("Response: Proceed\r\n" +
                    "Data: "+full.toString());

            return true;

        }else{
            for(SRSocket peer : peers){
                peer.sendText("Response: Error\r\n" +
                        "Reason: Not all peers received file.");
            }

            resolver.sendEncryptedText("Response: Error\r\n" +
                    "Reason: Not all peers received file.");

            return false;
        }
    }
}
