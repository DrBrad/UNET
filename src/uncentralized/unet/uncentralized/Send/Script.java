package uncentralized.unet.uncentralized.Send;

import org.json.JSONObject;
import uncentralized.unet.uncentralized.Handlers.SRSocket;
import uncentralized.unet.uncentralized.Variables;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uncentralized.unet.uncentralized.Handlers.General.*;
import static uncentralized.unet.uncentralized.Handlers.Parser.*;
import static uncentralized.unet.uncentralized.Handlers.Protocol.*;
import static uncentralized.unet.uncentralized.Send.Post.*;
import static uncentralized.unet.uncentralized.Variables.*;

public class Script {

    //BETTER
    public static boolean hasAccount(){
        String check = readPreference("me");
        if(!check.equals("")){
            return true;
        }
        return false;
    }

    public static void saveSettings(String data){
        JSONObject json = new JSONObject(data);
        if(!json.has("mydev")){
            json.put("mydev", "off");
        }

        if(!json.has("npc")){
            json.put("npc", "off");
        }
        savePreference("settings", json.toString());
    }

    public static String getSettings(){
        String check = readPreference("settings");
        if(check.equals("")){
            return "{\"boot\":\"off\",\"mydev\":\"on\",\"npc\":\"off\",\"bridge\":\"\",\"port\":\"8080\"}";
        }else{
            return check;
        }
    }

    //BETTER --
    public static String pullAllFiles(String dir){
        JSONObject me = new JSONObject(readPreference("me"));
        JSONObject json = new JSONObject();

        SRSocket resolver = quickConnectDNS();

        resolver.sendEncryptedText("Request-Type: Pull-Files\r\n" +
                "Domain: "+me.getString("d")+"\r\n" +
                "Key: "+me.getString("p")+"\r\n" +
                "Folder: "+dir);

        String[] protocol = customProtocol(resolver.receiveDecryptedText());
        quickClose(resolver);
        if(protocol[0].equals("Proceed")){
            json = new JSONObject(protocol[1]);
        }

        return json.toString();
    }

    //BETTER --
    public static boolean createDir(String location)throws Exception {
        location = URLDecoder.decode(location, "UTF-8");
        Pattern paths = Pattern.compile("(?:\\/|^)(.*\\/|)(.*)");
        Matcher matcher = paths.matcher(location);

        if(matcher.matches()){
            String fileName = matcher.group(2), folder = matcher.group(1);
            String[] path = { folder, fileName };

            JSONObject me = new JSONObject(readPreference("me"));

            SRSocket resolver = quickConnectDNS();

            resolver.sendEncryptedText("Request-Type: Create-Dir\r\n" +
                    "Domain: "+me.getString("d")+"\r\n" +
                    "Key: "+me.getString("p")+"\r\n" +
                    "Folder: "+path[0]+"\r\n" +
                    "Dir-name: "+path[1]);

            String[] protocol = customProtocol(resolver.receiveDecryptedText());
            quickClose(resolver);
            if(protocol[0].equals("Proceed")){
                return true;
            }
        }

        return false;
    }

    //BETTER --
    public static boolean delete(String location)throws Exception {
        location = URLDecoder.decode(location, "UTF-8");
        Pattern paths = Pattern.compile("(?:\\/|^)(.*\\/|)(.*)");
        Matcher matcher = paths.matcher(location);

        if(matcher.matches()){
            String fileName = matcher.group(2), folder = matcher.group(1);
            String[] path = { folder, fileName };

            JSONObject me = new JSONObject(readPreference("me"));
            SRSocket resolver = quickConnectDNS();

            resolver.sendEncryptedText("Request-Type: Delete\r\n" +
                    "Domain: "+me.getString("d")+"\r\n" +
                    "Key: "+me.getString("p")+"\r\n" +
                    "Folder: "+path[0]+"\r\n" +
                    "File-name: "+path[1]);

            String[] protocol = customProtocol(resolver.receiveDecryptedText());
            quickClose(resolver);
            if(protocol[0].equals("Proceed")){
                return true;
            }
        }

        return false;
    }

    //BETTER --
    public static boolean changeVisibility(String location)throws Exception {
        location = URLDecoder.decode(location, "UTF-8");
        Pattern paths = Pattern.compile("(?:\\/|^)(.*\\/|)(.*)");
        Matcher matcher = paths.matcher(location);

        if(matcher.matches()){
            String fileName = matcher.group(2), folder = matcher.group(1);
            String[] path = { folder, fileName };

            JSONObject me = new JSONObject(readPreference("me"));

            SRSocket resolver = quickConnectDNS();

            resolver.sendEncryptedText("Request-Type: Change-Vis\r\n" +
                    "Domain: "+me.getString("d")+"\r\n" +
                    "Key: "+me.getString("p")+"\r\n" +
                    "Folder: "+path[0]+"\r\n" +
                    "Dir-name: "+path[1]);

            String[] protocol = customProtocol(resolver.receiveDecryptedText());
            quickClose(resolver);
            if(protocol[0].equals("Proceed")){
                return true;
            }
        }

        return false;
    }

    //BETTER
    public static boolean uploadLocal(String location, String data)throws Exception {
        return local(location, new JSONObject(data));
    }

    //BETTER --
    public static String createDomain(String domain, String password, String email, String captcha, String hidden)throws Exception {
        SRSocket resolver = quickConnectDNS();

        resolver.sendEncryptedText("Request-Type: Create\r\n" +
                "Domain: "+URLDecoder.decode(domain, "UTF-8")+"\r\n" +
                "Key: "+URLDecoder.decode(password, "UTF-8")+"\r\n" +
                "Email: "+URLDecoder.decode(email, "UTF-8")+"\r\n" +
                "Captcha: "+captcha+"\r\n" +
                "Hidden: "+hidden);

        String[] protocol = customProtocol(resolver.receiveDecryptedText());
        quickClose(resolver);
        if(protocol[0].equals("Proceed")){
            savePreference("me", "");
            return "1";
        }else{
            return protocol[1];
        }
    }

    //FUCKING WEIRD ASS BUG - BUT WORKS...
    public static String getLocal(String fileName)throws Exception {
        fileName = dataBase.getPath()+"/Private/"+fileName;
        File file = new File(fileName);
        if(file.exists()){
            FileInputStream in = new FileInputStream(fileName);

            byte[] buffer = new byte[4096];
            int length;
            String builder = "";
            while(in.available() > 0){
                length = in.read(buffer);
                builder += new String(buffer, 0, length);
            }

            return builder;
        }
        return "";
    }

    //BETTER
    public static boolean saveLocal(String fileName, String content)throws Exception {
        File file = new File(dataBase.getPath()+"/Private/"+fileName);
        if(!file.exists()){
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
            writer.close();
            return true;
        }
        return false;
    }

    //BETTER --
    public static String verifyDomain(String domain, String secret)throws Exception {
        SRSocket resolver = quickConnectDNS();

        resolver.sendEncryptedText("Request-Type: Verify\r\n" +
                "Domain: "+URLDecoder.decode(domain, "UTF-8")+"\r\n" +
                "Secret: "+secret);

        String[] protocol = customProtocol(resolver.receiveDecryptedText());
        quickClose(resolver);
        if(protocol[0].equals("Proceed")){
            return "1";
        }else{
            return protocol[1];
        }
    }

    //BETTER --
    public static String login(String domain, String password, String captcha, String hidden)throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] pBytes = messageDigest.digest((URLDecoder.decode(password, "UTF-8")).getBytes());
        password = byteArrayToHexString(pBytes);

        SRSocket resolver = quickConnectDNS();

        resolver.sendEncryptedText("Request-Type: Login\r\n" +
                "Domain: "+URLDecoder.decode(domain, "UTF-8")+"\r\n" +
                "Key: "+password+"\r\n" +
                "Captcha: "+captcha+"\r\n" +
                "Hidden: "+hidden);

        String[] protocol = customProtocol(resolver.receiveDecryptedText());
        quickClose(resolver);
        if(protocol[0].equals("Proceed")){
            JSONObject user = new JSONObject();
            user.put("d", domain);
            user.put("p", password);

            String prefs = readPreference("me").equals("") ? "{\"boot\":\"off\",\"mydev\":\"on\",\"npc\":\"on\",\"bridge\":\"\",\"port\":\"8080\"}" : "{\"boot\":\"off\",\"mydev\":\"on\",\"npc\":\"off\",\"bridge\":\"\",\"port\":\"8080\"}";
            savePreference("settings", prefs);
            savePreference("me", user.toString());
            return "1";
        }else{
            return protocol[1];
        }
    }

    //BETTER --
    public static String changeKey(String oldKey, String newkey, String captcha, String hidden)throws Exception {
        String check = readPreference("me");
        if(!check.equals("")){
            JSONObject me = new JSONObject(check);
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] pBytes = messageDigest.digest((URLDecoder.decode(oldKey, "UTF-8")).getBytes());
            oldKey = byteArrayToHexString(pBytes);

            SRSocket resolver = quickConnectDNS();

            resolver.sendEncryptedText("Request-Type: Change-Key\r\n" +
                    "Domain: "+me.getString("d")+"\r\n" +
                    "Key: "+oldKey+"\r\n" +
                    "New-Key: "+URLDecoder.decode(newkey, "UTF-8")+"\r\n" +
                    "Captcha: "+captcha+"\r\n" +
                    "Hidden: "+hidden);

            String[] protocol = customProtocol(resolver.receiveDecryptedText());
            quickClose(resolver);
            if(protocol[0].equals("Proceed")){
                messageDigest = MessageDigest.getInstance("SHA-256");
                pBytes = messageDigest.digest((URLDecoder.decode(newkey, "UTF-8")).getBytes());
                me.put("p", byteArrayToHexString(pBytes));
                savePreference("me", me.toString());

                return "1";
            }else{
                return protocol[1];
            }
        }else{
            return "You aren't logged in.";
        }
    }

    //BETTER --
    public static String forgotKey(String domain, String email, String captcha, String hidden)throws Exception {
        SRSocket resolver = quickConnectDNS();

        resolver.sendEncryptedText("Request-Type: Forgot-Key\r\n" +
                "Domain: "+URLDecoder.decode(domain, "UTF-8")+"\r\n" +
                "Email: "+URLDecoder.decode(email, "UTF-8")+"\r\n" +
                "Captcha: "+captcha+"\r\n" +
                "Hidden: "+hidden);

        String[] protocol = customProtocol(resolver.receiveDecryptedText());
        quickClose(resolver);
        if(protocol[0].equals("Proceed")){
            return "1";
        }else{
            return protocol[1];
        }
    }

    public static String fchangeKey(String domain, String secret, String password, String captcha, String hidden)throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] pBytes = messageDigest.digest((URLDecoder.decode(password, "UTF-8")).getBytes());
        password = byteArrayToHexString(pBytes);

        SRSocket resolver = quickConnectDNS();

        resolver.sendEncryptedText("Request-Type: FChange-Key\r\n" +
                "Domain: "+URLDecoder.decode(domain, "UTF-8")+"\r\n" +
                "Secret: "+secret+"\r\n" +
                "Key: "+password+"\r\n" +
                "Captcha: "+captcha+"\r\n" +
                "Hidden: "+hidden);

        String[] protocol = customProtocol(resolver.receiveDecryptedText());
        quickClose(resolver);
        if(protocol[0].equals("Proceed")){
            JSONObject user = new JSONObject();
            user.put("d", domain);
            user.put("p", password);
            savePreference("me", user.toString());

            return "1";
        }else{
            return protocol[1];
        }
    }

    //BETTER --
    public static String getCaptcha(){
        SRSocket resolver = quickConnectDNS();

        resolver.sendEncryptedText("Request-Type: Captcha");

        String[] protocol = customProtocol(resolver.receiveDecryptedText());
        quickClose(resolver);
        if(protocol[0].equals("Proceed")){
            String response = "<input class='login' type='hidden' name='h' value='"+protocol[1]+"' required>" +
                    "<img src='data:image/png;base64,"+protocol[2]+"' style='float: left; margin-top: 20px; border-radius: 5px'>";

            return response;
        }else{
            return null;
        }
    }

    //BETTER -/
    public static String get(String location)throws Exception {
        String[] path = parsePath(location);
        JSONObject user = new JSONObject(readPreference("me"));

        SRSocket resolver = quickConnectDNS();
        resolver.sendEncryptedText("Request-Type: Get\r\n" +
                "Domain: "+user.getString("d")+"\r\n" +
                "Folder: "+path[0]+"\r\n" +
                "File-name: "+path[1]);

        String[] protocol = customProtocol(resolver.receiveDecryptedText());
        quickClose(resolver);
        if(protocol[0].startsWith("Proceed")){
            JSONObject pson = new JSONObject(protocol[2]);
            ArrayList<SRSocket> peers = new ArrayList<>();

            for(int i = 1; i < 4; i++){
                for(int j = 0; j < pson.getJSONArray(i+"").length(); j++){
                    try{
                        SRSocket peer = quickConnectPeer(pson.getJSONArray(i+"").getString(j), 8081);

                        peer.sendEncryptedText("Request-Type: Get\r\n" +
                                "File-name: "+protocol[1]+"part"+i);

                        String[] peerProtocol = customProtocol(peer.receiveText());
                        if(peerProtocol[0].startsWith("Proceed")){
                            peers.add(peer);
                            break;
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }

            if(peers.size() > 2){
                byte[] salt = new byte[8], iv = new byte[16];

                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                KeySpec keySpec = new PBEKeySpec((pson.getString("k")).toCharArray(), salt, 65536, 256);
                SecretKey tmp = factory.generateSecret(keySpec);
                SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));

                for(SRSocket peer : peers){
                    peer.sendText("Response: Proceed");
                }

                InputStream in = Base64.getDecoder().wrap(new SequenceInputStream(new SequenceInputStream(peers.get(0).getInputStream(),
                        peers.get(1).getInputStream()),
                        peers.get(2).getInputStream()));

                return receiveText(cipher, in);
            }
        }
        return null;
    }

    //BETTER
    public static boolean upload(String location, String content){
        try{
            String tmp = generateKey();
            File file = new File(Variables.tmp.getPath()+"/"+tmp);
            FileOutputStream out = new FileOutputStream(file);

            out.write(content.getBytes());
            out.flush();
            out.close();

            JSONObject data = new JSONObject(), info = new JSONObject();
            info.put("tmp", tmp);
            info.put("name", location);
            data.put("upload", info);

            return local(location, data);
        }catch(Exception e){
            return false;
        }
    }

    public static String receiveText(Cipher cipher, InputStream in){
        try{
            String builder = "";
            byte[] buffer = new byte[4096];
            int length;
            while((length = in.read(buffer)) != -1){
                byte[] output = cipher.update(buffer, 0, length);
                if(output != null){
                    builder += new String(output);
                }
            }

            byte[] output = cipher.doFinal();
            if(output != null){
                builder += new String(output);
            }
            return builder;
        }catch(Exception e){
            return null;
        }
    }
}
