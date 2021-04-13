package uncentralized.unet.uncentralized.Handlers;

import org.json.JSONArray;
import org.json.JSONObject;
import uncentralized.unet.uncentralized.Main;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static uncentralized.unet.uncentralized.Handlers.Parser.*;
import static uncentralized.unet.uncentralized.Variables.*;

public class Protocol {

    public static String readLine(Socket socket)throws IOException {
        InputStream in = socket.getInputStream();

        //VERSION 2.5
        String builder = "";
        char i;
        while(in.available() > 0){
            int c = in.read();

            i = ((char)((byte)c));
            builder += i;
            if(builder.endsWith("\r\n\r\n")){
                break;
            }
        }

        return builder;
    }

    public static void forwardData(Socket inputSocket, Socket outputSocket){
        try{
            InputStream inputStream = inputSocket.getInputStream();
            try{
                OutputStream outputStream = outputSocket.getOutputStream();
                try{
                    byte[] buffer = new byte[4096];//4096
                    int read;
                    do{
                        read = inputStream.read(buffer);
                        if(read > 0){
                            outputStream.write(buffer, 0, read);
                            if(inputStream.available() < 1){
                                outputStream.flush();
                            }
                        }
                    }while(read >= 0);
                }catch(Exception e){
                }finally{
                    if(!outputSocket.isOutputShutdown()){
                        outputSocket.shutdownOutput();
                    }
                }
            }finally{
                if(!inputSocket.isInputShutdown()){
                    inputSocket.shutdownInput();
                }
            }
        }catch(IOException e){
        }
    }

    public static void sendHeader(OutputStream out, String mimeType, String cookies){
        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            //OutputStream out = socket.getOutputStream();
            out.write(("HTTP/1.1 200 Connection established\r\n").getBytes());
            //out.write(("Protocol-agent: UNet/0.1\r\n").getBytes());
            out.write(("Server: Uncentralized\r\n").getBytes());
            out.write(("Content-type: "+mimeType+"\r\n").getBytes());
            //out.write(("Accept-ranges: bytes\r\n").getBytes());
            out.write(("Date: "+dateFormat.format(new Date().getTime())+"\r\n").getBytes());
            out.write(cookies.getBytes());

            //out.write(("Set-Cookie: TEFS=AWD AWDAWD; Expires="+dateFormat.format(new Date().getTime()+(1000*60*24))+";\r\n").getBytes());
            /*
            out.write(("Accept-ranges: bytes\r\n").getBytes());
            out.write(("Vary: Accept-Encoding,User-Agent\r\n").getBytes());
            out.write(("Range: bytes=7733248-\r\n").getBytes());
            */
            //out.write(("Content-Length: "+length+"\r\n").getBytes());
            //out.write(("Cache-Control: max-age=31536000\r\n").getBytes());
            out.write(("\r\n").getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void sendHeader(OutputStream out, String mimeType, long size){
        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            //OutputStream out = socket.getOutputStream();
            out.write(("HTTP/1.1 200 Connection established\r\n").getBytes());
            //out.write(("Protocol-agent: UNet/0.1\r\n").getBytes());
            out.write(("Server: Uncentralized\r\n").getBytes());
            out.write(("Content-type: "+mimeType+"\r\n").getBytes());
            //out.write(("Accept-ranges: bytes\r\n").getBytes());
            out.write(("Date: "+dateFormat.format(new Date().getTime())+"\r\n").getBytes());


            //out.write(("Set-Cookie: TEFS=AWD AWDAWD; Expires="+dateFormat.format(new Date().getTime()+(1000*60*24))+";\r\n").getBytes());
            out.write(("Accept-ranges: bytes\r\n").getBytes());
            out.write(("Vary: Accept-Encoding,User-Agent\r\n").getBytes());
            //out.write(("Range: bytes=7733248-\r\n").getBytes());
            out.write(("Content-Length: "+size+"\r\n").getBytes());
            out.write(("Cache-Control: max-age=31536000\r\n").getBytes());
            //out.write(cookies.getBytes());

            out.write(("\r\n").getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void sendRedirectHeader(OutputStream out, String location, String cookies){
        try{
            out.write(("HTTP/1.1 301 Moved Permanently\r\n").getBytes());
            out.write(("Location: "+location+"\r\n").getBytes());
            out.write(cookies.getBytes());
            out.write(("\r\n").getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void sendErrorHeader(OutputStream out){
        try{
            //OutputStream out = socket.getOutputStream();
            out.write(("HTTP/1.1 404 Not Found\r\n").getBytes());
            out.write(("Protocol-agent: UNet/0.1\r\n").getBytes());
            out.write(("Content-type: text/html\r\n").getBytes());
            out.write(("\r\n").getBytes());
            out.write("Error Page doesn't Exist...".getBytes());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static String sendLocal(String fileName)throws Exception {
        InputStream in = Main.class.getResourceAsStream("/"+fileName);
        int read;
        byte[] bytes = new byte[4096];
        String builder = "";

        while((read = in.read(bytes)) != -1){
            builder += new String(bytes, 0, read);
        }

        return builder;
    }

    //BETTER
    public static JSONObject receiveFile(Socket socket, String end, String[] disposition){
        String fileName = generateKey();
        File file = new File(tmp.getPath()+"/"+fileName);
        try{
            if(file.exists()){
                file.delete();
            }

            JSONObject data = new JSONObject(), info = new JSONObject();
            info.put("tmp", fileName);
            info.put("name", disposition[1]);
            data.put(disposition[0], info);

            InputStream in = socket.getInputStream();
            FileOutputStream out = new FileOutputStream(file.getPath());
            byte[] buffer = new byte[4096];
            int length;

            while(true){
                length = in.read(buffer);

                String response = new String(buffer, 0, length, "ISO-8859-1");
                if(response.contains(end)){
                    out.write(response.split(end)[0].getBytes("ISO-8859-1"), 0, response.split(end)[0].getBytes("ISO-8859-1").length);

                    try{
                        if(response.split(end).length > 0){
                            disposition = handleDisposition(response.split(end)[1]);
                            out.flush();
                            out.close();

                            if(file.length() < 300){
                                System.err.println("CANT UPLOAD FILE, TOO SMALL");
                                file.delete();
                                return null;
                            }

                            fileName = generateKey();

                            //KEY ALREADY EXISTS SO WE MUST MAKE IT AN ARRAY...
                            if(data.has(disposition[0])){

                                //MAKE IT INTO AN ARRAY
                                if(data.get(disposition[0]) instanceof JSONObject){
                                    JSONArray filesArr = new JSONArray();
                                    info = new JSONObject();
                                    info.put("tmp", data.getJSONObject(disposition[0]).getString("tmp"));
                                    info.put("name", data.getJSONObject(disposition[0]).getString("name"));
                                    //info.put("secret", 0);
                                    filesArr.put(info);

                                    info = new JSONObject();
                                    info.put("tmp", fileName);//disposition[1]);
                                    info.put("name", disposition[1]);
                                    //info.put("secret", 1);
                                    filesArr.put(info);

                                    data.put(disposition[0], filesArr);
                                }else{
                                    info = new JSONObject();
                                    info.put("tmp", fileName);//disposition[1]);
                                    info.put("name", disposition[1]);
                                    //info.put("secret", data.getJSONArray(disposition[0]).length());
                                    data.getJSONArray(disposition[0]).put(info);
                                }
                            }else{
                                info = new JSONObject();
                                info.put("tmp", fileName);//disposition[1]);
                                info.put("name", disposition[1]);
                                data.put(disposition[0], info);
                            }

                            //START A NEW FILE
                            file = new File(tmp.getPath()+"/"+fileName);//disposition[1]);
                            out = new FileOutputStream(file);
                            out.write(response.split(end)[1].split("\r\n\r\n")[1].getBytes("ISO-8859-1"));
                        }else{
                            break;
                        }
                    }catch(Exception e){
                        //e.printStackTrace();
                        break;
                    }
                }else{
                    out.write(buffer, 0, length);
                }
            }

            out.flush();
            out.close();

            if(file.length() < 300){
                System.err.println("CANT UPLOAD FILE, TOO SMALL");
                file.delete();
            }else{
                return data;
            }

        }catch(Exception e){
            e.printStackTrace();
            file.delete();
        }
        return null;
    }

    public static String generateKey(){
        String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        String result = "";
        for(int i = 0; i < 21; i++){
            result += characters.charAt((int)(Math.random()*characters.length()));
        }

        File file = new File(tmp.getPath()+"/"+result);
        if(file.exists()){
            return generateKey();
        }else{
            return result;
        }
    }

    public static String getDataBaseFile(File file){
        try{
            FileInputStream in = new FileInputStream(file.getPath());
            byte[] buffer = new byte[4096];
            int length;
            String builder = "";
            while((length = in.read(buffer)) > 0){
                builder += new String(buffer, 0, length);
            }

            return builder;
        }catch(Exception e){
            return null;
        }
    }
}
