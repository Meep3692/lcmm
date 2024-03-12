package ca.awoo.lcmm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ca.awoo.lcmm.Installer.Location;
import ca.awoo.lcmm.Progress.Status;

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

    static Logger installLogger = Logger.getLogger("mod installer");
    static{
        installLogger.setParent(App.logger);
    }

    private static Installer modInstaller = new Installer(installLogger, Optional.of(new File("BepInEx/plugins")))
        .addLocationForDirectory("BepInEx","")
        .addLocationForDirectory("plugins", "BepInEx")
        .addLocationForDirectory("config", "BepInEx")
        .addLocationForDirectory("patchers", "BepInEx")
        .addLocationForLooseFiletype("dll", "BepInEx/plugins")
        .addLocationForLooseFiletype("cosmetics", "BepInEx/plugins/MoreCompanyCosmetics")
        .addLocation(new Location(s -> s.startsWith("BepInExPack/"), (entry, root) -> Optional.of(new File(root, entry.substring("BepInExPack/".length())))))
        .ignore(entry -> !entry.contains("/"));

    public static ReportingFuture<Void> installMod(Mod mod, File lcRoot) {
        logger.info("Installing mod: " + mod.getName());
        MultiProgress multiProgress = new MultiProgress("Installing mod " + mod.getDependancyString());
        Progress installProgress = new Progress("Install", 0, "Waiting for mod");
        ReportingFuture<Void> future = new ReportingFuture<>(multiProgress);
        ReportingFuture<InputStream> getModFuture = getMod(mod);
        multiProgress.addProgress(getModFuture.getProgress());
        multiProgress.addProgress(installProgress);
        getModFuture.thenAccept(is -> {
            //TODO: actual progress on installing mods
            installProgress.update("Copying files", Status.RUNNING);
            try(ZipInputStream zis = new ZipInputStream(is)){
                modInstaller.install(zis, lcRoot);
                installProgress.update("Files installed", 1.0, Status.FINISHED);
                future.complete(null);
            }catch(IOException e){
                logger.warning("Failed to install mod: " + mod.getName() + " due to: " + e);
                installProgress.update(e.getMessage(), Status.FAILED);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public static ReportingFuture<Void> installProfile(String uuid, File lcRoot) throws IOException{
        Profile profile = Profile.getProfile(uuid);
        return ReportingFuture.allOf("Install profile: " + profile.getName(), Arrays.stream(profile.getMods()).map(mod -> installMod(mod, lcRoot)).toArray(ReportingFuture[]::new));
    }
}
