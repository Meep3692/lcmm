package ca.awoo.lcmm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

import org.yaml.snakeyaml.Yaml;

public class Profile {
    private String name;
    private Mod[] mods;

    public Profile(String name, Mod[] mods) {
        this.name = name;
        this.mods = mods;
    }

    public String getName() {
        return name;
    }

    public Mod[] getMods() {
        return mods;
    }

    @SuppressWarnings("unchecked")
    public static Profile deserialize(InputStream is){
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(is);
        String name = (String) data.get("profileName");
        List<Object> mods = (List<Object>) data.get("mods");
        Mod[] modArray = new Mod[mods.size()];
        for (int i = 0; i < mods.size(); i++) {
            Map<String, Object> mod = (Map<String, Object>) mods.get(i);
            String modName = (String) mod.get("name");
            Map<String, Object> version = (Map<String, Object>) mod.get("version");
            int major = (int) version.get("major");
            int minor = (int) version.get("minor");
            int patch = (int) version.get("patch");
            boolean modEnabled = (boolean) mod.get("enabled");
            modArray[i] = new Mod(modName, new Version(major, minor, patch), modEnabled);
        }
        return new Profile(name, modArray);
    }

    public static Profile getProfile(String uuid) throws IOException {
        try(ZipInputStream zip = new ZipInputStream(Web.getProfile(uuid))){
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (entry.getName().equals("export.r2x")) {
                    return Profile.deserialize(zip);
                }
            }
            throw new FileNotFoundException("export.r2x not found in profile");
        }
        
    }
}
