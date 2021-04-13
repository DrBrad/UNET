package uncentralized.unet.uncentralized.Handlers;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.*;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SRSocket extends Socket {

    public static HashMap<String, Session> serverSessions = new HashMap<>(), clientSessions = new HashMap<>();
    private String key;
    private boolean client;
    private Cipher ecipher, dcipher;

    public SRSocket(){
        super();
        client = false;
    }

    public SRSocket(boolean client){
        super();
        this.client = client;
    }

    public SRSocket(String host, int port)throws IOException {
        super(host, port);
        client = true;
    }

    public void startHandshake()throws Exception {
        if(client){
            clientHandshake();
        }else{
            serverHandshake();
        }
    }



    //CLIENT PORTION
    private void clientHandshake()throws Exception {
        InetSocketAddress address = (InetSocketAddress) getRemoteSocketAddress();
        String identifier = address.getAddress().toString()+":"+address.getPort();

        //SEND SESSION IF WE HAVE ONE
        if(clientSessions.containsKey(identifier)){
            sendText("Session: "+clientSessions.get(identifier).uuid);
        }else{
            sendText("Session: ");
        }

        String[] protocol = customProtocol(receiveText());
        if(protocol[0].equals("Proceed")){
            key = clientSessions.get(identifier).key;
        }else{
            //GET PUBLIC KEY
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(readKey());

            //SEND AES KEY
            key = UUID.randomUUID().toString();
            sendText(encryptRSA(key, publicKey));

            //SAVE SESSION ID
            Session session = new Session(key, receiveDecryptedText());
            clientSessions.put(identifier, session);
        }
    }

    private X509EncodedKeySpec readKey()throws Exception {
        while(getInputStream().available() < 1){
        }

        byte[] buffer = new byte[4096];
        while(getInputStream().available() > 0){
            getInputStream().read(buffer);
        }

        return new X509EncodedKeySpec(buffer);
    }

    private String encryptRSA(String plainText, PublicKey publicKey)throws Exception {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] cipherText = encryptCipher.doFinal(plainText.getBytes(UTF_8));

        return Base64.getEncoder().encodeToString(cipherText);
    }



    //SERVER PORTION
    private void serverHandshake()throws Exception {
        String[] protocol = customProtocol(receiveText());

        if(serverSessions.containsKey(protocol[0])){
            Session session = serverSessions.get(protocol[0]);
            key = session.key;
            sendText("Response: Proceed");
        }else{
            sendText("Response: Error");

            //CREATE KEY PAIR - PUB & PRIV
            KeyPair pair = generateKeyPair();

            //SEND PUB KEY
            getOutputStream().write(pair.getPublic().getEncoded());
            getOutputStream().flush();

            //RECEIVE AES KEY
            key = decryptRSA(receiveText(), pair.getPrivate());

            //SEND SESSION ID OVER AES
            Session session = new Session(key);
            sendEncryptedText(session.uuid);
        }
    }

    private KeyPair generateKeyPair()throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());
        KeyPair pair = generator.generateKeyPair();

        return pair;
    }

    private String decryptRSA(String cipherText, PrivateKey privateKey)throws Exception {
        byte[] bytes = Base64.getDecoder().decode(cipherText);

        Cipher decriptCipher = Cipher.getInstance("RSA");
        decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);

        return new String(decriptCipher.doFinal(bytes), UTF_8);
    }



    //AES ENCRYPTION
    private String encryptAES(String plainText)throws Exception {
        if(ecipher == null){
            byte[] salt = new byte[8], iv = new byte[16];

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec keySpec = new PBEKeySpec(key.toCharArray(), salt, 65536, 256);
            SecretKey secretKey = factory.generateSecret(keySpec);
            SecretKey secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

            ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            ecipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(iv));
        }

        byte[] cipherText = ecipher.doFinal(plainText.getBytes(UTF_8));
        return Base64.getEncoder().encodeToString(cipherText);
    }

    private String decryptAES(String cipherText)throws Exception {
        byte[] bytes = Base64.getDecoder().decode(cipherText);
        if(dcipher == null){
            byte[] salt = new byte[8], iv = new byte[16];

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec keySpec = new PBEKeySpec(key.toCharArray(), salt, 65536, 256);
            SecretKey secretKey = factory.generateSecret(keySpec);
            SecretKey secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

            dcipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            dcipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        }

        return new String(dcipher.doFinal(bytes), UTF_8);
    }



    //SENDING TEXT - WITH BYTE SIZE ENSURANCE
    public void sendText(String text){
        try{
            //VERSION 2.0 WORKS GREAT UP UNTIL WE HAVE SLOW INTERNET
            text = "Size: "+text.length()+"\r\n"+text;

            OutputStream out = getOutputStream();
            out.write(text.getBytes());
            out.flush();
        }catch(Exception e){
        }
    }

    public String receiveText(){
        try{
            //VERSION 3.2
            InputStream in = getInputStream();
            while(in.available() < 1){
            }

            Pattern pattern = Pattern.compile("Size: ([0-9]*)");

            byte[] buffer = new byte[4096];
            int length, size = 0;

            String builder = "";

            while(in.available() > 0){
                length = in.read(buffer);

                builder += new String(buffer, 0, length);
                Matcher matcher = pattern.matcher(builder.split("\r\n")[0]);
                if(matcher.matches()){
                    size = Integer.parseInt(matcher.group(1));
                    builder = builder.substring(matcher.group(0).length()+2);
                }
            }

            if(builder.length() == size){
                return builder;
            }

            while(true){
                if(in.available() > 0){
                    length = in.read(buffer);
                    builder += new String(buffer, 0, length);

                    if(builder.length() == size){
                        return builder;
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public void sendEncryptedText(String text){
        try{
            sendText(encryptAES(text));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public String receiveDecryptedText(){
        try{
            return decryptAES(receiveText());
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }



    //HANDLERS
    private String[] customProtocol(String response){
        String[] content = new String[response.split("\r\n").length];
        Pattern pattern = Pattern.compile("(.*?): (.*)");

        int count = 0;
        for(String line : response.split("\r\n")){
            Matcher matcher = pattern.matcher(line);
            if(matcher.matches()){
                content[count] = matcher.group(2);
                count++;
            }
        }
        return content;
    }

    public class Session {
        public String key, uuid;

        public Session(String key, String uuid){
            this.key = key;
            this.uuid = uuid;
        }

        public Session(String key){
            this.key = key;
            this.uuid = makeSession();
            serverSessions.put(uuid, this);
        }

        public String makeSession(){
            String uuid = UUID.randomUUID().toString();
            if(serverSessions.containsKey(uuid)){
                return makeSession();
            }else{
                return uuid;
            }
        }
    }
}
