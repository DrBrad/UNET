package uncentralized.unet.uncentralized;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Variables {

    public static long downloading;
    public static HashMap<String, Long> downloads = new HashMap<>();
    public static HashMap<String, String> fileNames = new HashMap<>();
    public static ArrayList<String> resolvers = new ArrayList<>();
    public static String[] udns = { "192.168.0.7", "97.118.110.99", "udns10.com", "udns11.com", "udns12.com" };
    public static int versionCode = 1;
    public static String dns;

    public static File tmp, peerData, dataBase;
}
