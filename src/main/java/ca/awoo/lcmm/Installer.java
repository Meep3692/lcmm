package ca.awoo.lcmm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Installer {
    /**
     * Represents a location that files can be installed to
     */
    public static class Location{
        private final Predicate<String> predicate;
        private final Locator locator;

        /**
         * Create a new location
         * @param predicate A predicate that takes a path to a file in the mod zip and returns true if the file should be installed here
         * @param locator A Locator which will return the File location to copy a file to from the zip
         */
        public Location(Predicate<String> predicate, Locator locator) {
            this.predicate = predicate;
            this.locator = locator;
        }

        public boolean test(String s){
            return predicate.test(s);
        }

        public Optional<File> locate(String entry, File root){
            return locator.locate(entry, root);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((predicate == null) ? 0 : predicate.hashCode());
            result = prime * result + ((locator == null) ? 0 : locator.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Location other = (Location) obj;
            if (predicate == null) {
                if (other.predicate != null)
                    return false;
            } else if (!predicate.equals(other.predicate))
                return false;
            if (locator == null) {
                if (other.locator != null)
                    return false;
            } else if (!locator.equals(other.locator))
                return false;
            return true;
        }

        
    }

    public static interface Locator{
        public Optional<File> locate(String entry, File root);

        public static Locator fromPrefix(String prefix){
            return (entry, root) -> Optional.of(new File(root, prefix + "/" + entry));
        }

        public static Locator ignore(){
            return (entry, root) -> Optional.empty();
        }
    }
    private final Logger logger;
    private final Set<Location> locations = new HashSet<>();
    private final Optional<File> defaultInstallDir;

    /**
     * Create a new installer
     * @param logger The logger to log to
     * @param defaultInstallDir The default directory to install files to if none of the locations are appropriate. If set to Optional.empty(), these files will be skipped
     */
    public Installer(Logger logger, Optional<File> defaultInstallDir) {
        this.logger = logger;
        this.defaultInstallDir = defaultInstallDir;
    }

    public Installer addLocation(Location location){
        locations.add(location);
        return this;
    }

    public Installer addLocation(Predicate<String> predicate, String prefix){
        addLocation(new Location(predicate, Locator.fromPrefix(prefix)));
        return this;
    }

    public Installer addLocationForFilename(String filename, String prefix){
        addLocation(s -> s.equals(filename), prefix);
        return this;
    }

    public Installer addLocationForLooseFiletype(String filetype, String prefix){
        addLocation(s -> s.endsWith("." + filetype) && !s.contains("/"), prefix);
        return this;
    }

    public Installer addLocationForDirectory(String directory, String prefix){
        addLocation(s -> s.startsWith(directory + "/"), prefix);
        return this;
    }

    public Installer ignore(Predicate<String> predicate){
        addLocation(new Location(predicate, Locator.ignore()));
        return this;
    }

    private boolean installFile(String name, InputStream is, File location){
        logger.info("Installing file: " + name + " to " + location);
        try {
            location.getParentFile().mkdirs();
            Files.copy(is, location.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            logger.warning("Failed to install file: " + name + " to " + location + " due to: " + e);
            return false;
        }
    }

    public void install(ZipInputStream zip, File root) throws IOException{
        int installedFiles = 0;
        for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
            if(entry.isDirectory()){
                //Ignore directories
                continue;
            }
            String name = entry.getName().replace("\\", "/");
            boolean installed = false;
            for(Location location : locations){
                if(location.test(name)){
                    Optional<File> installDir = location.locate(name, root);
                    if(!installDir.isPresent()){
                        //Skip this file
                        //Don't increment installedFiles though, because that's to check if no files get installed from a pack
                        installed = true;
                        break;
                    }
                    File installFile = installDir.get();
                    if(installFile(name, zip, installFile)){
                        installedFiles++;
                    }
                    installed = true;
                    break;
                }
            }
            if(!installed && defaultInstallDir.isPresent()){
                File installFile = new File(root, defaultInstallDir.get() + "/" + name);
                installFile.getParentFile().mkdirs();
                logger.warning("Installing file: " + name + " to " + installFile + " because no location was found");
                if(installFile(name, zip, installFile)){
                    installedFiles++;
                }
            }
        }
        if(installedFiles == 0){
            logger.warning("No files installed");
        }
    }
}
