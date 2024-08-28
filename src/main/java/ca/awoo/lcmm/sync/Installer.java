package ca.awoo.lcmm.sync;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class Installer {
    public static class InstallRule{
        public final Predicate<Path> predicate;
        public final Function<Path, Path> transformer;

        public InstallRule(Predicate<Path> predicate, Function<Path, Path> transformer) {
            this.predicate = predicate;
            this.transformer = transformer;
        }
    }

    private final List<InstallRule> rules = new ArrayList<>();

    public Installer(){

    }

    public Installer addRule(Predicate<Path> predicate, Function<Path, Path> transformer){
        rules.add(new InstallRule(predicate, transformer));
        return this;
    }

    public Installer ignore(Predicate<Path> predicate){
        return addRule(predicate, path -> null);
    }

    public Installer ignore(Path file){
        return ignore(path -> path.equals(file));
    }

    public Installer ignore(String file){
        return ignore(Paths.get("/", file));
    }

    public Installer ignore(String... files){
        for(String file : files){
            ignore(file);
        }
        return this;
    }

    public Installer merge(Path source, Path dest){
        return addRule(path -> path.startsWith(source), path -> dest.resolve(source.relativize(path)));
    }

    public Installer merge(String source, String dest){
        return merge(Paths.get(source), Paths.get(dest));
    }

    public Path resolve(Path source){
        for(InstallRule rule : rules){
            if(rule.predicate.test(source)){
                return rule.transformer.apply(source);
            }
        }
        return Paths.get("/unknown").resolve(Paths.get("/").relativize(source));
    }

    public static Installer lethalCompanyInstaller(){
        return new Installer().ignore("CHANGELOG.md", "icon.png", "LICENSE", "License.txt", "manifest.json", "README.md")
                              .ignore(path -> path.getFileName().toString().endsWith(".pdb"))
                              .merge("/BepInEx", "/BepInEx")
                              .merge("/plugins", "/BepInEx/plugins")
                              .merge("/BepInExPack", "/")
                              .merge("/BepinEx", "/BepInEx") //Fix typo in Blorb-WeatherMultipliers-1.1.0
                              .merge("/patchers", "/BepInEx/patchers")
                              .merge("/BoomboxController", "/BoomboxController")
                              .addRule(path -> path.getNameCount() == 1 && path.getFileName().toString().endsWith(".cosmetics"), path -> Paths.get("/BepInEx/plugins/MoreCompanyCosmetics").resolve(path.getFileName()))
                              .addRule(path -> path.getNameCount() == 1, path -> Paths.get("/BepInEx/plugins").resolve(path.getFileName()));
    }
}
