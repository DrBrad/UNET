package uncentralized.unet.uncentralized.Send;

import org.json.JSONObject;
import uncentralized.unet.uncentralized.Handlers.SRSocket;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.net.URL;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;

import static uncentralized.unet.uncentralized.Handlers.General.*;
import static uncentralized.unet.uncentralized.Handlers.Mimes.*;
import static uncentralized.unet.uncentralized.Handlers.Parser.*;
import static uncentralized.unet.uncentralized.Handlers.Protocol.*;
import static uncentralized.unet.uncentralized.Handlers.FileHandler.*;

public class Get {

    //BETTER --
    public static void noVars(URL url, JSONObject data, OutputStream out)throws Exception {
        String[] path = parsePath(url.getPath());

        SRSocket resolver = quickConnectDNS();

        resolver.sendEncryptedText("Request-Type: Get\r\n" +
                "Domain: "+url.getHost().split("\\.")[0]+"\r\n" +
                "Folder: "+path[0]+"\r\n" +
                "File-name: "+path[1]);

        String[] protocol = customProtocol(resolver.receiveDecryptedText());
        if(protocol[0].startsWith("Proceed")){
            quickClose(resolver);
            get(protocol, path, out);

        }else if(protocol[1].startsWith("Requires key.")){
            String check = readPreference("me");
            if(!check.equals("")){
                JSONObject me = new JSONObject(check);
                if(url.getHost().split("\\.")[0].equals(me.getString("d"))){
                    resolver.sendEncryptedText("Response: Proceed\r\n" +
                            "Key: "+me.getString("p"));

                    protocol = customProtocol(resolver.receiveDecryptedText());
                    quickClose(resolver);
                    if(protocol[0].startsWith("Proceed")){
                        get(protocol, path, out);
                    }
                }
            }

        }else if(protocol[1].startsWith("Try with variables?")){
            resolver.sendEncryptedText("Response: Proceed\r\n" +
                    "Data: "+data.toString());

            protocol = customProtocol(resolver.receiveDecryptedText());
            if(protocol[0].startsWith("Proceed")){
                JSONObject response = new JSONObject(protocol[1]);

                if(response != null){
                    String cookies = (response.has("c")) ? response.getString("c") : "";
                    if(response.has("m")){
                        sendRedirectHeader(out, response.getString("m"), cookies);
                        out.flush();
                    }else{
                        sendHeader(out, "text/html", cookies);
                        out.write(response.getString("r").getBytes());
                        out.flush();
                    }
                }else{
                    sendErrorHeader(out);
                }
            }else{
                sendErrorHeader(out);
            }
        }
    }

    //BETTER --
    public static void withVars(URL url, JSONObject data, OutputStream out)throws Exception {
        String[] path = parsePath(url.getPath());

        SRSocket resolver = quickConnectDNS();

        resolver.sendEncryptedText("Request-Type: Get\r\n" +
                "Domain: "+url.getHost().split("\\.")[0]+"\r\n" +
                "File-name: "+path[0]+"\r\n" +
                "Folder: "+path[1]+"\r\n" +
                "Data: "+data.toString());

        String[] protocol = customProtocol(resolver.receiveDecryptedText());
        quickClose(resolver);
        if(protocol[0].startsWith("Proceed")){
            JSONObject response = new JSONObject(protocol[1]);

            if(response != null){
                String cookies = (response.has("c")) ? response.getString("c") : "";
                if(response.has("m")){
                    sendRedirectHeader(out, response.getString("m"), cookies);
                    out.flush();
                }else{
                    sendHeader(out, "text/html", cookies);
                    out.write(response.getString("r").getBytes());
                    out.flush();
                }
            }else{
                sendErrorHeader(out);
            }
        }else{
            sendErrorHeader(out);
        }
    }

    public static void get(String[] protocol, String[] path, OutputStream out)throws Exception {
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

            sendHeader(out, mime.getContentType(path[1]), pson.getLong("s"));
            receiveFile(in, out, cipher);
        }
    }
}
