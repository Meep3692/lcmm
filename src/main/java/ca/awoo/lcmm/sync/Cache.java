package ca.awoo.lcmm.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Base64;

public class Cache {
    private final File mods;
    private final File profiles;

    public Cache(File location) {
        this.mods = new File(location, "mods");
        if(!mods.exists()){
            mods.mkdirs();
        }
        this.profiles = new File(location, "profiles");
        if(!profiles.exists()){
            profiles.mkdirs();
        }
    }

    private InputStream getWebStream(URL url) throws IOException {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if(responseCode == 200){
                return connection.getInputStream();
            }else{
                throw new IOException("File not found: " + url);
            }
    }

    public InputStream getProfile(String uuid) throws IOException {
        File local = new File(profiles, uuid + ".zip");
        if(!local.exists()){
            URL url = new URL("https://gcdn.thunderstore.io/live/modpacks/legacyprofile/" + uuid);
            InputStream base64 = getWebStream(url);
            //Read past hash comment line
            for(int next = base64.read(); next != -1; next = base64.read()) {
                if (next == 10) {
                    break;
                }
            }
            Base64.Decoder decoder = Base64.getDecoder();
            InputStream zip = decoder.wrap(base64);
            Files.copy(zip, local.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        //TODO: check corruption
        return new FileInputStream(local);
    }

    public InputStream[] getProfiles() throws IOException {
        File[] files = profiles.listFiles();
        InputStream[] streams = new InputStream[files.length];
        for(int i = 0; i < files.length; i++){
            streams[i] = new FileInputStream(files[i]);
        }
        return streams;
    }

    public InputStream getMod(String dependancyString) throws IOException {
        File local = new File(mods, dependancyString + ".zip");
        if(!local.exists()){
            URL url = new URL("https://gcdn.thunderstore.io/live/repository/packages/" + dependancyString + ".zip");
            InputStream zip = getWebStream(url);
            Files.copy(zip, local.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        //TODO: check corruption
        return new FileInputStream(local);
    }
}
