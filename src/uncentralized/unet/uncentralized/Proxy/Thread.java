package uncentralized.unet.uncentralized.Proxy;

import org.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import uncentralized.unet.uncentralized.Send.Script;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uncentralized.unet.uncentralized.Handlers.General.*;
import static uncentralized.unet.uncentralized.Handlers.Mimes.*;
import static uncentralized.unet.uncentralized.Handlers.Parser.*;
import static uncentralized.unet.uncentralized.Handlers.Protocol.*;
import static uncentralized.unet.uncentralized.Send.Get.*;
import static uncentralized.unet.uncentralized.Send.Post.*;

public class Thread extends java.lang.Thread {

    private Socket socket;

    public Thread(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run(){
        try{
            socket.setKeepAlive(true);
            socket.setSoTimeout(10000);
            Pattern pattern = Pattern.compile("(GET|CONNECT|POST) (.+)(://|:)(.+) HTTP/(1\\.[01])", Pattern.CASE_INSENSITIVE); //HEAD IS A WEIRD ONE - UNSURE HOW TOO HANDLE IT

            for(int i = 0; i < 3000; i++){
                String response = readLine(socket);
                Matcher matcher = pattern.matcher(response.split("\r\n")[0]);

                if(!response.equals("")){
                    if(matcher.matches()){
                        if(matcher.group(1).equals("CONNECT")){
                            connect(matcher);
                        }else if(matcher.group(1).equals("GET")){
                            URL url = new URL(matcher.group(2)+matcher.group(3)+matcher.group(4));
                            get(url, response);
                        }else if(matcher.group(1).equals("POST")){
                            URL url = new URL(matcher.group(2)+matcher.group(3)+matcher.group(4));
                            post(url, response);
                        }
                    }
                    break;
                }
            }
        }catch(Exception e){
            //e.printStackTrace();
        }finally{
            quickClose(socket);
        }
    }

    //CONNECT SECTION
    public void connect(Matcher matcher)throws Exception {
        InetAddress address = InetAddress.getByName(new URL("https://"+matcher.group(2)).getHost());
        Socket server = new Socket(address, Integer.parseInt(matcher.group(4)));

        OutputStream out = socket.getOutputStream();
        out.write(("HTTP/"+matcher.group(5)+" 200 Connection established\r\n").getBytes());
        out.write(("Protocol-agent: UNet/0.1\r\n").getBytes());
        out.write(("\r\n").getBytes());
        out.flush();

        new java.lang.Thread(new Runnable(){
            @Override
            public void run(){
                forwardData(server, socket);
            }
        }).start();

        forwardData(socket, server);
        quickClose(server);
    }

    //GET SECTION
    public void get(URL url, String response)throws Exception {
        if(url.getHost().equalsIgnoreCase("me.unet")){
            getLocal(url, response);
        }else if(url.getHost().endsWith(".unet")){
            getOnline(url, response);
        }else{
            getNormal(url, response);
        }
    }

    public void getNormal(URL url, String response)throws Exception {
        InetAddress address = InetAddress.getByName(url.getHost());
        Socket server = new Socket(address, 80);

        byte[] buffer = response.getBytes();
        OutputStream out = server.getOutputStream();
        out.write(buffer, 0, buffer.length);
        out.flush();

        new java.lang.Thread(new Runnable(){
            @Override
            public void run(){
                forwardData(socket, server);
            }
        }).start();

        forwardData(server, socket);

        if(!server.isOutputShutdown()){
            server.shutdownOutput();
        }
        if(!server.isInputShutdown()){
            server.shutdownInput();
        }

        server.close();
    }

    public void getLocal(URL url, String response)throws Exception {
        String[] headers = getHeaders(response);
        String[] path = parsePath(url.getPath());

        System.out.println("L: "+path[1]);

        if(path[1].endsWith(".html")){
            JSONObject data = new JSONObject();
            data.put("j", headers[2]);
            if(headers[0] != null){
                data.put("c", parseCookies(headers[0]));
            }

            if(url.getQuery() != null){
                data.put("g", parseGet(URLDecoder.decode(url.getQuery(), "UTF-8")));
            }

            runScript(sendLocal(path[1]), data);
        }else{
            sendHeader(socket.getOutputStream(), mime.getContentType("/"+path[1]), "");

            OutputStream out = socket.getOutputStream();
            InputStream in = getClass().getResourceAsStream("/"+path[1]);
            int read;
            byte[] bytes = new byte[4096];

            while((read = in.read(bytes)) != -1){
                out.write(bytes, 0, read);
            }
        }
    }

    public void getOnline(URL url, String response)throws Exception {
        String[] headers = getHeaders(response);

        JSONObject json = new JSONObject();
        json.put("j", headers[2]);
        if(headers[0] != null){
            json.put("c", parseCookies(headers[0]));
        }

        if(url.getQuery() != null){
            json.put("g", parseGet(url.getQuery()));
            withVars(url, json, socket.getOutputStream());
        }else{
            noVars(url, json, socket.getOutputStream());
        }
    }

    //POST SECTION
    public void post(URL url, String response)throws Exception {
        if(url.getHost().equalsIgnoreCase("me.unet")){
            postLocal(url, response);
        }else if(url.getHost().endsWith(".unet")){
            postOnline(url, response);
        }else{
            getNormal(url, response);
        }
    }

    public void postOnline(URL url, String response)throws Exception {
        String[] headers = getHeaders(response);

        JSONObject data = new JSONObject();
        data.put("j", headers[2]);
        if(headers[0] != null){
            data.put("c", parseCookies(headers[0]));
        }

        if(url.getQuery() != null){
            data.put("g", parseGet(url.getQuery()));
        }

        if(headers[1].startsWith("multipart/form-data")){
            String end = "--"+headers[1].split("multipart/form-data; boundary=")[1];

            response = readLine(socket);
            String[] disposition = handleDisposition(response);

            data.put("f", new JSONObject());

            //JUST FILE
            if(disposition.length > 0){
                JSONObject extras = receiveFile(socket, end, disposition);

                if(extras != null){
                    data.put("f", extras);
                    online(url, data, socket.getOutputStream());
                }else{
                    sendErrorHeader(socket.getOutputStream());
                }

            //MULTIPART
            }else{
                String builder = response;

                while(true){
                    response = readLine(socket);
                    disposition = handleDisposition(response);
                    builder += response;
                    if(disposition.length > 0){
                        data.getJSONObject("f").put(disposition[0], disposition[1]);
                        break;
                    }
                }

                data.put("p", parsePost(builder));

                if(data.has("f")){
                    JSONObject extras = receiveFile(socket, end, disposition);

                    if(extras != null){
                        data.put("f", extras);
                        online(url, data, socket.getOutputStream());
                    }else{
                        sendErrorHeader(socket.getOutputStream());
                    }
                }
            }

        //NORMAL
        }else{
            response = readLine(socket);

            data.put("p", new JSONObject());

            for(String vars : response.split("&")){
                if(vars.split("=").length > 1){
                    String key = vars.split("=")[0];
                    vars = vars.split("=")[1];
                    data.getJSONObject("p").put(key, vars);
                }else{
                    data.getJSONObject("p").put(vars.split("=")[0], "");
                }
            }

            withVars(url, data, socket.getOutputStream());
        }
    }

    public void postLocal(URL url, String response)throws Exception {
        String[] headers = getHeaders(response);

        String[] path = parsePath(url.getPath());

        JSONObject data = new JSONObject();
        data.put("j", headers[2]);
        if(headers[0] != null){
            data.put("c", parseCookies(headers[0]));
        }

        if(url.getQuery() != null){
            data.put("g", parseGet(url.getQuery()));
        }

        if(headers[1].startsWith("multipart/form-data")){
            String end = "--"+headers[1].split("multipart/form-data; boundary=")[1];

            response = readLine(socket);
            String[] disposition = handleDisposition(response);

            data.put("f", new JSONObject());

            //JUST FILE
            if(disposition.length > 0){
                JSONObject extras = receiveFile(socket, end, disposition);

                if(extras != null){
                    data.put("f", extras);
                    runScript(sendLocal(path[1]), data);
                }else{
                    sendErrorHeader(socket.getOutputStream());
                }

            //MULTIPART
            }else{
                String builder = response;

                while(true){
                    response = readLine(socket);
                    disposition = handleDisposition(response);
                    builder += response;
                    if(disposition.length > 0){
                        data.getJSONObject("f").put(disposition[0], disposition[1]);
                        break;
                    }
                }

                data.put("p", parsePost(builder));

                if(data.has("f")){
                    JSONObject extras = receiveFile(socket, end, disposition);

                    if(extras != null){
                        data.put("f", extras);
                        runScript(sendLocal(path[1]), data);
                    }else{
                        sendErrorHeader(socket.getOutputStream());
                    }
                }
            }

        //NORMAL
        }else{
            response = readLine(socket);

            data.put("p", new JSONObject());

            for(String vars : response.split("&")){
                if(vars.split("=").length > 1){
                    String key = vars.split("=")[0];
                    vars = vars.split("=")[1];
                    data.getJSONObject("p").put(key, vars);
                }else{
                    data.getJSONObject("p").put(vars.split("=")[0], "");
                }
            }

            runScript(sendLocal(path[1]), data);
        }
    }

    public void runScript(String file, JSONObject data){
        Context context = Context.enter();
        try{
            Scriptable scope = context.initStandardObjects();

            Object print = Context.javaToJS(System.out, scope);
            ScriptableObject.putProperty(scope, "out", print);

            Object script = Context.javaToJS(new Script(), scope);
            ScriptableObject.putProperty(scope, "unet", script);

            String run = parseBackend(file);

            String preRun = "function addCookie(key, value, expires){" +
                    "setcookies += 'Set-Cookie: '+encodeURI(key)+'='+encodeURI(value)+'; Expires='+expires+';\\r\\n';" +
                    "}" +
                    "var userAgent = '"+data.getString("j")+"';" +
                    "var echo = 'No Response';" +
                    "var setcookies = '';" +
                    "var redirect = '';";

            String check = readPreference("me");
            if(!check.equals("")){
                JSONObject me = new JSONObject(check);
                preRun += "var d = '"+me.getString("d")+"';";
            }

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

            context.evaluateString(scope, preRun+run, "<cmd>", 1, null);

            OutputStream out = socket.getOutputStream();

            String cookies = (scope.get("setcookies", scope).equals("")) ? scope.get("setcookies", scope).toString() : "";
            if(!scope.get("redirect", scope).equals("")){
                sendRedirectHeader(out, scope.get("redirect", scope).toString(), cookies);
                out.flush();
            }else{
                sendHeader(out, "text/html", cookies);
                out.write(scope.get("echo", scope).toString().getBytes());
                out.flush();
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        context.exit();
    }
}
