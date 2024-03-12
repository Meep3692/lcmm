package ca.awoo.lcmm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Web {

    public static final Logger logger = Logger.getLogger(Web.class.getName());

    public static InputStream getProfile(String uuid) throws IOException{
        //https://gcdn.thunderstore.io/live/modpacks/legacyprofile/{uuid}
        File local = new File("profiles/" + uuid + ".zip");
        if (local.exists()) {
            return new FileInputStream(local);
        }else{
            URL url = new URL("https://gcdn.thunderstore.io/live/modpacks/legacyprofile/" + uuid);
            logger.info("Fetching modpack at: " + url.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                logger.info("Modpack found!");
                InputStream base64 = connection.getInputStream();
                //Read past hash comment line
                logger.info("Reading past hash comment line");
                for(int next = base64.read(); next != -1; next = base64.read()) {
                    if (next == 10) {
                        break;
                    }
                }
                //Read base64 encoded modpack zip file
                logger.info("Reading base64 encoded modpack zip file");
                Base64.Decoder decoder = Base64.getDecoder();
                InputStream zip = decoder.wrap(base64);
                //Save zip file to cache
                logger.info("Saving zip file to cache");
                local.getParentFile().mkdirs();
                local.createNewFile();
                Files.copy(zip, local.toPath(), StandardCopyOption.REPLACE_EXISTING);
                zip.close();
                return new FileInputStream(local);
            } else {
                logger.warning("Modpack not found!");
                return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static ReportingFuture<InputStream> getMod(Mod mod){
        Progress progress = new Progress("Get mod: " + mod.getDependancyString(), 0, "Not started yet");
        ReportingFuture<InputStream> output = new ReportingFuture<>(progress);
        File local = new File("mods/" + mod.getDependancyString() + ".zip");
        if (local.exists()) {
            ForkJoinPool.commonPool().submit(() -> {
                try{
                    FileInputStream fis = new FileInputStream(local);
                    try(ZipInputStream zis = new ZipInputStream(fis)){
                        for(ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
                        }
                    }catch(IOException e){
                        //Corrupt zip file, redownload
                        logger.warning("Corrupt zip file for mod " + mod.getDependancyString());
                        local.delete();
                        progress.update("Corrupt zip file, redownloading");
                        ReportingFuture<InputStream> download = (ReportingFuture<InputStream>) getMod(mod);
                        progress.match(download.getProgress());
                        download.thenAccept(output::complete);
                    }finally{
                        fis.close();
                    }
                    output.complete(new FileInputStream(local));
                }catch(IOException e){
                    output.completeExceptionally(e);
                }
            });
        } else {
            try{
                URL url = new URL("https://gcdn.thunderstore.io/live/repository/packages/" + mod.getDependancyString() + ".zip");
                logger.info("Fetching mod at: " + url.toString());
                Download download = new Download(url);
                ReportingFuture<File> future = download.reportingDownload(local);
                progress.match(future.getProgress());
                future.thenAccept(file -> {
                    try{
                        output.complete(new FileInputStream(file));
                    }catch(IOException e){
                        output.completeExceptionally(e);
                    }
                });
            }catch(Exception e){
                output.completeExceptionally(e);
            }
        }
        return output;
    }

    /*public static InputStream getMod(Mod mod) throws IOException{
        //https://gcdn.thunderstore.io/live/repository/packages/BepInEx-BepInExPack-5.4.2100.zip
        File local = new File("mods/" + mod.getDependancyString() + ".zip");
        if (local.exists()) {
            FileInputStream fis = new FileInputStream(local);
            try(ZipInputStream zis = new ZipInputStream(fis)){
                for(ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
                }
            }catch(IOException e){
                //Corrupt zip file, redownload
                logger.warning("Corrupt zip file for mod " + mod.getDependancyString());
                local.delete();
                return getMod(mod);
            }finally{
                fis.close();
            }
            return new FileInputStream(local);
        }else{
            URL url = new URL("https://gcdn.thunderstore.io/live/repository/packages/" + mod.getDependancyString() + ".zip");
            logger.info("Fetching mod at: " + url.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                logger.info("Mod found!");
                //Thankfully, this endpoint just gives us the zip file directly
                InputStream zip = connection.getInputStream();
                //Save zip file to cache
                logger.info("Saving zip file to cache");
                local.getParentFile().mkdirs();
                local.createNewFile();
                Files.copy(zip, local.toPath(), StandardCopyOption.REPLACE_EXISTING);
                zip.close();
                return new FileInputStream(local);
            } else {
                logger.info("Mod not found!");
                return null;
            }
        }
    }*/

    private static Installer modInstaller = new Installer(Logger.getLogger("mod installer"), Optional.of(new File("plugins")))
        .addLocationForDirectory("BepInEx","")
        .addLocationForDirectory("plugins", "BepInEx")
        .addLocationForDirectory("config", "BepInEx")
        .addLocationForDirectory("patchers", "BepInEx")
        .addLocationForLooseFiletype("dll", "BepInEx/plugins")
        .addLocationForLooseFiletype("cosmetics", "BepInEx/plugins/MoreCompanyCosmetics")
        .ignore(entry -> !entry.contains("/"));

    public static void installMod(Mod mod, File lcRoot) throws IOException{
        logger.info("Installing mod: " + mod.getName());
        try(ZipInputStream zis = new ZipInputStream(getMod(mod))){
            modInstaller.install(zis, lcRoot);
        }
    }

    public static void installProfile(String uuid, File lcRoot) throws IOException{
        Profile profile = Profile.getProfile(uuid);
        for (Mod mod : profile.getMods()) {
            installMod(mod, lcRoot);
        }
        /*ZipInputStream zis = new ZipInputStream(getProfile(uuid));
        File bepInExRoot = new File(lcRoot, "BepInEx");
        bepInExRoot.mkdirs();
        for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
            if(entry.isDirectory()){
                //Ignore directories
                continue;
            }
            if(entry.getName().startsWith("BepInEx/")){
                //File is relative to lcroot
                File file = new File(lcRoot, entry.getName());
                file.getParentFile().mkdirs();
                Files.copy(zis, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }else if(entry.getName().startsWith("config/")){
                //File is relative to bepinex root
                File file = new File(bepInExRoot, entry.getName());
                file.getParentFile().mkdirs();
                Files.copy(zis, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }else if(entry.getName().equals("doorstop_config.ini")){
                //File is relative to lcroot
                File file = new File(lcRoot, entry.getName());
                file.getParentFile().mkdirs();
                Files.copy(zis, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }else if(!entry.getName().contains("/")){
                //Random raw file in root directory, ignore
                logger.info("Ignoring loose file: " + entry.getName());
            }else{
                //File is unrecognized, but probably important!
                //I don't even know where that would go, so lets ignore it I guess
                logger.warning("Unrecognized file: " + entry.getName() + ", ignoring");
            }
        }*/
    }
}
