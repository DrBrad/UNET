package uncentralized.unet.uncentralized.Handlers;

import org.json.JSONObject;

import java.io.File;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uncentralized.unet.uncentralized.Handlers.Protocol.*;

public class Parser {

    public static String[] getHeaders(String response){
        String[] content = new String[3];
        Pattern[] patterns = { Pattern.compile("(Cookie: (.*))"), Pattern.compile("(Content-Type: (.*))"), Pattern.compile("(User-Agent: (.*))") };

        for(String line : response.split("\r\n")){
            for(int i = 0; i < patterns.length; i++){
                Matcher matcher = patterns[i].matcher(line);
                if(matcher.matches()){
                    content[i] = matcher.group(2);
                }
            }
        }
        return content;
    }

    public static String[] handleDisposition(String response){
        String[] content = new String[2];
        Pattern pattern = Pattern.compile("(Content-Disposition: (.*?)(name=\\\"(.*?)\").*?(name=\\\"(.*?)\"))");

        for(String line : response.split("\r\n")){
            Matcher matcher = pattern.matcher(line);
            if(matcher.matches()){
                content[0] = matcher.group(4);
                content[1] = matcher.group(6);
                //break;
                return content;
            }
        }
        return new String[0];
    }

    public static JSONObject parseCookies(String cookies){
        JSONObject json = new JSONObject();

        for(String vars : cookies.split(";")){
            if(vars.contains("=")){
                String key = URLDecoder.decode(vars.split("=")[0]);
                vars = URLDecoder.decode(vars.split("=")[1]);
                json.put(key, vars);
            }else{
                json.put(URLDecoder.decode(vars), "");
            }
        }

        return json;
    }

    public static JSONObject parseGet(String url){
        JSONObject json = new JSONObject();

        for(String vars : url.split("&")){
            if(vars.contains("=")){
                String key = vars.split("=")[0];
                vars = vars.split("=")[1];
                json.put(key, vars);
            }else{
                json.put(vars, "");
            }
        }

        return json;
    }

    public static JSONObject parsePost(String builder){
        JSONObject json = new JSONObject();
        String key = "";
        for(int i = 0; i < builder.split("\r\n").length; i++){
            String line = builder.split("\r\n")[i];

            if(i%4 == 1){
                key = line;
            }else if(i%4 == 3){
                json.put(key.split("name=\"")[1].split("\"")[0], line);
            }
        }
        return json;
    }

    public static String[] customProtocol(String response){
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

    public static String[] parsePath(String request)throws Exception {
        request = URLDecoder.decode(request, "UTF-8");
        Pattern paths = Pattern.compile("(?:\\/|^)(.*\\/|)(.*)");
        Matcher matcher = paths.matcher(request);

        if(matcher.matches()){
            String fileName = matcher.group(2), folder = matcher.group(1);

            if(fileName.equals("") ||fileName.equals("index")){
                fileName = "index.html";
            }else if(!fileName.contains(".")){
                fileName = fileName+".html";
            }

            String[] parsed = { folder, fileName };
            return parsed;
        }
        return null;
    }

    public static String parseBackend(File file){
        String script = getDataBaseFile(file);

        Pattern pattern = Pattern.compile("(.*?|^)<!unet(.*?)!>((.*?(?=<!unet))|(.*))", Pattern.DOTALL | Pattern.MULTILINE); //(.*?(?=<!unet)|)
        Matcher matcher = pattern.matcher(script);

        script = "";
        while(matcher.find()){
            if(!matcher.group(1).equals("")){
                script += "echo += \""+matcher.group(1).replaceAll("\n", "").replaceAll("\"", "\\\\\"")+"\";";
            }
            script += matcher.group(2);
            if(!matcher.group(3).equals("")){
                script += "echo += \""+matcher.group(3).replaceAll("\n", "").replaceAll("\"", "\\\\\"")+"\";";
            }
        }
        return script;
    }

    public static String parseBackend(String script){
        Pattern pattern = Pattern.compile("(.*?|^)<!unet(.*?)!>((.*?(?=<!unet))|(.*))", Pattern.DOTALL | Pattern.MULTILINE); //(.*?(?=<!unet)|)
        Matcher matcher = pattern.matcher(script);

        script = "";
        while(matcher.find()){
            if(!matcher.group(1).equals("")){
                script += "echo += \""+matcher.group(1).replaceAll("\n", "").replaceAll("\"", "\\\\\"")+"\";";
            }
            script += matcher.group(2);
            if(!matcher.group(3).equals("")){
                script += "echo += \""+matcher.group(3).replaceAll("\n", "").replaceAll("\"", "\\\\\"")+"\";";
            }
        }
        return script;
    }

    public static String byteArrayToHexString(byte[] b){
        StringBuffer sb = new StringBuffer(b.length * 2);
        for(int i = 0; i < b.length; i++){
            int v = b[i] & 0xff;
            if(v < 16){
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase();
    }
}
