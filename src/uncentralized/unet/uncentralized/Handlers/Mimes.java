package uncentralized.unet.uncentralized.Handlers;

import javax.activation.MimetypesFileTypeMap;

public class Mimes {

    public static MimetypesFileTypeMap mime = new MimetypesFileTypeMap();

    public Mimes(){
        mime.addMimeTypes("image/svg+xml svg SVG");
        mime.addMimeTypes("image/png png PNG");
        mime.addMimeTypes("text/html php PHP");
        mime.addMimeTypes("application/json json JSON");
        mime.addMimeTypes("text/css css CSS");
        mime.addMimeTypes("application/pdf pdf PDF");
        mime.addMimeTypes("application/javascript js JS");
        mime.addMimeTypes("video/mp4 mp4 MP4");
        mime.addMimeTypes("audio/mpeg mp3 MP3");
    }
}
