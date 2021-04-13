package uncentralized.unet.uncentralized.Receiver;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import uncentralized.unet.uncentralized.Handlers.SRSocket;
import uncentralized.unet.uncentralized.Send.Script;

import java.io.*;
import java.net.InetSocketAddress;

import static uncentralized.unet.uncentralized.Handlers.General.*;
import static uncentralized.unet.uncentralized.Handlers.Parser.*;
import static uncentralized.unet.uncentralized.Variables.*;

public class Thread extends java.lang.Thread {

    private final SRSocket socket;

    public Thread(SRSocket socket){
        this.socket = socket;
    }

    @Override
    public void run(){
        try{
            socket.setKeepAlive(true);
            socket.setSoTimeout(10000);
            socket.setSoLinger(true, 10000);
            socket.startHandshake();

            String[] headers = customProtocol(socket.receiveDecryptedText());
            InetSocketAddress address = (InetSocketAddress) socket.getRemoteSocketAddress();

            if(headers[0].equals("Get")){
                get(headers);

            }else if(headers[0].equals("Upload-P2P")){
                uploadP2P(headers);

            }else if(resolvers.contains(address.getAddress().toString().replaceFirst("/", ""))){
                JSONObject user = new JSONObject(readPreference("me"));
                if(headers[1].equals(user.getString("p"))){
                    if(headers[0].equals("Get-B")){
                        getBackend(headers);

                    }else if(headers[0].equals("Upload")){
                        upload(headers);

                    }else if(headers[0].equals("Upload-O")){
                        uploadOnline(headers);

                    }else if(headers[0].equals("Delete")){
                        delete(headers);
                    }
                }
            }
        }catch(Exception e){
            //e.printStackTrace();
        }finally {
            quickClose(socket);
        }
    }

    //BETTER
    public void get(String[] headers){
        File file = new File(peerData.getPath()+"/"+headers[1]);
        if(file.exists()){
            socket.sendText("Response: Proceed");

            String[] protocol = customProtocol(socket.receiveText());
            if(protocol[0].equals("Proceed")){
                sendFile(file);
            }
        }else{
            socket.sendText("Response: Error\r\n" +
                    "Reason: File no longer exists.");
        }
    }

    public void getBackend(String headers[]){
        File file = new File(dataBase.getPath()+"/"+headers[3]);
        if(file.exists()){
            Context context = Context.enter();
            try{
                Scriptable scope = context.initStandardObjects();

                Object print = Context.javaToJS(System.out, scope);
                ScriptableObject.putProperty(scope, "out", print);

                Object script = Context.javaToJS(new Script(), scope);
                ScriptableObject.putProperty(scope, "unet", script);

                JSONObject data = new JSONObject(headers[4]);
                String run = parseBackend(file);

                String preRun = "function addCookie(key, value, expires){" +
                        "setcookies += 'Set-Cookie: '+encodeURI(key)+'='+encodeURI(value)+'; Expires='+expires+';\\r\\n';" +
                        "}" +
                        "var link = '"+headers[2]+"';" +
                        "var userAgent = '"+data.getString("j")+"';" +
                        "var echo = 'No Response';" +
                        "var setcookies = '';" +
                        "var redirect = '';";

                if(data.has("p")){
                    preRun += "var post = JSON.parse('"+data.getJSONObject("p").toString()+"');";
                }
                if(data.has("g")){
                    preRun += "var get = JSON.parse('"+data.getJSONObject("g").toString()+"');";
                }
                if(data.has("c")){
                    preRun += "var cookies = JSON.parse('"+data.getJSONObject("c").toString()+"');";
                }

                context.evaluateString(scope, preRun+run, "<cmd>", 1, null);

                JSONObject json = new JSONObject();
                json.put("r", scope.get("echo", scope));
                if(!scope.get("redirect", scope).equals("")){
                    json.put("m", scope.get("redirect", scope));
                }
                if(!scope.get("setcookies", scope).equals("")){
                    json.put("c", scope.get("setcookies", scope));
                }

                socket.sendEncryptedText("Response: Proceed\r\n" +
                        "Data: "+json.toString());

            }catch(Exception e){
                e.printStackTrace();
                socket.sendEncryptedText("Response: Error\r\n" +
                        "Reason: Error with file.");
            }

            Context.exit();
        }else{
            socket.sendEncryptedText("Response: Error\r\n" +
                    "Reason: File no longer exists.");
        }
    }

    //BETTER
    public void uploadP2P(String[] headers){
        if(downloads.containsKey(headers[1])){
            int filePart = Integer.parseInt(headers[2]);
            long fileSize = Long.parseLong(headers[3]);

            if(downloads.get(headers[1])-fileSize > -8192 && downloads.get(headers[1])-fileSize < 8192){
                socket.sendText("Response: Proceed");

                File file = new File(peerData.getPath()+"/"+fileNames.get(headers[1])+"part"+filePart);
                if(file.exists()){
                    file.delete();
                }

                receiveFile(file, fileSize);

                downloading -= downloads.get(headers[1]);
                downloads.remove(headers[1]);
                fileNames.remove(headers[1]);

                String[] protocol = customProtocol(socket.receiveText());
                if(protocol[0].equals("Error")){
                    file.delete();
                }
            }else{
                socket.sendText("Response: Error\r\n" +
                        "Reason: File size is not as specified.");
            }
        }else{
            socket.sendText("Response: Error\r\n" +
                    "Reason: Key is not in list.");
        }
    }

    //BETTER --
    public void upload(String[] headers){
        long fileSize = Long.parseLong(headers[4]);

        JSONArray json = new JSONArray(headers[5]);
        if(json.length() > 0){
            for(int i = 0; i < json.length(); i++){
                File file = new File(peerData.getPath()+"/"+json.getString(i));
                if(file.exists()){
                    file.delete();
                }
            }
        }

        if(new File("/").getFreeSpace()+downloading > fileSize){
            downloads.put(headers[2], fileSize);
            downloading += fileSize;
            fileNames.put(headers[2], headers[3]);
            socket.sendEncryptedText("Response: Proceed");
        }else{
            socket.sendEncryptedText("Response: Error\r\n" +
                    "Reason: No storage available.");
        }
    }

    //BETTER --
    public void uploadOnline(String[] headers){
        File file = new File(dataBase.getPath()+"/"+headers[3]);
        if(file.exists()){
            Context context = Context.enter();
            try{
                Scriptable scope = context.initStandardObjects();

                Object print = Context.javaToJS(System.out, scope);
                ScriptableObject.putProperty(scope, "out", print);

                Object script = Context.javaToJS(new Script(), scope);
                ScriptableObject.putProperty(scope, "unet", script);

                JSONObject data = new JSONObject(headers[4]);
                String run = parseBackend(file);

                String preRun = "function addCookie(key, value, expires){" +
                        "setcookies += 'Set-Cookie: '+encodeURI(key)+'='+encodeURI(value)+'; Expires='+expires+';\\r\\n';" +
                        "}" +
                        "var link = '"+headers[2]+"';" +
                        "var userAgent = '"+data.getString("j")+"';" +
                        "var echo = 'No Response';" +
                        "var setcookies = '';" +
                        "var redirect = '';";

                if(data.has("p")){
                    preRun += "var post = JSON.parse('"+data.getJSONObject("p").toString()+"');";
                }
                if(data.has("g")){
                    preRun += "var get = JSON.parse('"+data.getJSONObject("g").toString()+"');";
                }
                if(data.has("c")){
                    preRun += "var cookies = JSON.parse('"+data.getJSONObject("c").toString()+"');";
                }
                if(data.has("f")){
                    preRun += "var files = JSON.parse('"+data.getJSONObject("f").toString()+"');";
                }

                run += "if(typeof files != 'undefined'){ files = JSON.stringify(files); }";

                context.evaluateString(scope, preRun+run, "<cmd>", 1, null);

                JSONObject json = new JSONObject();
                if(scope.get("files", scope) != null){
                    if(((String)scope.get("files", scope)).length() > 2){
                        json.put("f", new JSONObject(scope.get("files", scope)));
                    }
                }

                json.put("r", scope.get("echo", scope));
                if(!scope.get("redirect", scope).equals("")){
                    json.put("m", scope.get("redirect", scope));
                }
                if(!scope.get("setcookies", scope).equals("")){
                    json.put("c", scope.get("setcookies", scope));
                }

                socket.sendEncryptedText("Response: Proceed\r\n" +
                        "Data: "+json.toString());

            }catch(Exception e){
                e.printStackTrace();
                socket.sendEncryptedText("Response: Error\r\n" +
                        "Reason: Error with file.");
            }

            Context.exit();
        }else{
            socket.sendEncryptedText("Response: Error\r\n" +
                    "Reason: File no longer exists.");
        }
    }

    //BETTER --
    public void delete(String[] headers){
        File file = new File(peerData.getPath()+"/"+headers[2]);
        System.out.println(file.getPath());
        if(file.exists()){
            file.delete();
        }

        socket.sendEncryptedText("Response: Proceed");
    }

    //BETTER
    public void sendFile(File file){
        try{
            OutputStream out = socket.getOutputStream();

            FileInputStream in = new FileInputStream(file.getPath());
            byte[] buffer = new byte[4096];
            int length;
            while((length = in.read(buffer)) > 0){
                out.write(buffer, 0, length);
            }

            out.flush();
        }catch(Exception e){
        }
    }

    //BETTER
    public void receiveFile(File file, long fileSize){
        try{
            InputStream in = socket.getInputStream();
            FileOutputStream out = new FileOutputStream(file.getPath());

            long newSize = 0;
            byte[] buffer = new byte[4096];
            int length;

            while(newSize < fileSize){
                length = in.read(buffer);
                newSize += length;
                out.write(buffer, 0, length);
            }

            out.flush();
            out.close();

        }catch(Exception e){
            file.delete();
            e.printStackTrace();
        }
    }
}
