package uncentralized.unet.uncentralized.Handlers;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.security.spec.KeySpec;
import java.util.Base64;

public class FileHandler {

    public static String split(File source, File dest){
        String key = generateKey();
        encrypt(source, dest, key);
        splitFile(dest, new File(dest.getPath()+"part1"));

        return key;
    }

    private static void splitFile(File source, File dest){
        try{
            InputStream is = null;
            OutputStream os = null;
            try{
                is = new FileInputStream(source);
                os = new FileOutputStream(dest);

                byte[] buffer;
                if(source.length() > 24576){
                    buffer = new byte[4096];
                }else{
                    buffer = new byte[(Math.toIntExact(source.length())/3)+1];
                }

                int length, count = 2;
                while((length = is.read(buffer)) > 0){
                    os.write(buffer, 0, length);
                    if(source.length()/3 > dest.length()){
                    }else if(count < 4){
                        os.close();
                        dest = new File(dest.getPath().substring(0, dest.getPath().length()-1)+count);
                        os = new FileOutputStream(dest);
                        count++;
                    }
                }
            }finally{
                is.close();
                os.close();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void encrypt(File source, File dest, String password){
        try{
            FileInputStream inFile = new FileInputStream(source.getPath());
            OutputStream outFile = Base64.getEncoder().wrap(new FileOutputStream(dest.getPath()));

            byte[] salt = new byte[8], iv = new byte[16];

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            SecretKey secretKey = factory.generateSecret(keySpec);
            SecretKey secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(iv));

            byte[] input = new byte[4096];
            int bytesRead;

            while((bytesRead = inFile.read(input)) != -1){
                byte[] output = cipher.update(input, 0, bytesRead);
                if(output != null){
                    outFile.write(output);
                }
            }

            byte[] output = cipher.doFinal();
            if(output != null){
                outFile.write(output);
            }

            inFile.close();
            outFile.flush();
            outFile.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static String generateKey(){
        String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        String result = "";
        for(int i = 0; i < 21; i++){
            result += characters.charAt((int)(Math.random()*characters.length()));
        }

        return result;
    }

    public static boolean sendFile(Socket socket, File file){
        try{
            OutputStream out = socket.getOutputStream();
            FileInputStream in = new FileInputStream(file.getPath());

            byte[] buffer = new byte[4096];
            int length;
            while((length = in.read(buffer)) > 0){
                out.write(buffer, 0, length);
            }

            out.flush();

            return true;
        }catch(Exception e){
            return false;
        }
    }

    public static void receiveFile(InputStream in, OutputStream out, Cipher cipher){
        try{
            byte[] buffer = new byte[4096];
            int length;
            while((length = in.read(buffer)) != -1){
                byte[] output = cipher.update(buffer, 0, length);
                if(output != null){
                    out.write(output);
                }
            }

            byte[] output = cipher.doFinal();
            if(output != null){
                out.write(output);
            }
        }catch(Exception e){
        }
    }
}
