package ca.awoo.lcmm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.nio.file.Path;
import ca.awoo.lcmm.sync.Paths;

import org.junit.Test;

import ca.awoo.lcmm.sync.Installer;

public class InstallerTest {
    Installer installer = Installer.lethalCompanyInstaller();

    @Test
    public void ignoreTest(){
        Path[] paths = new Path[]{
            Paths.get("/CHANGELOG.md"),
            Paths.get("/icon.png"),
            Paths.get("/LICENSE"),
            Paths.get("/License.txt"),
            Paths.get("/manifest.json"),
            Paths.get("/README.md"),
            Paths.get("/test.pdb"),
            Paths.get("/sub/path/test.pdb"),
        };
        for(Path path : paths){
            assertNull(path.toString() + " -> null", installer.resolve(path));
        }
    }

    @Test
    public void bepInExTest(){
        Path[] paths = new Path[]{
            Paths.get("/BepInEx/plugins/foo.dll"),
            Paths.get("/BepInEx/config/bar.json"),
            Paths.get("/BepInEx/baz"),
        };
        for(Path path : paths){
            assertEquals(path.toString() + "->" + path.toString(), path, installer.resolve(path));
        }
    }

    @Test
    public void pluginsTest(){
        Path[] paths = new Path[]{
            Paths.get("/plugins/foo.dll"),
            Paths.get("/plugins/bundle"),
        };
        Path[] targets = new Path[]{
            Paths.get("/BepInEx/plugins/foo.dll"),
            Paths.get("/BepInEx/plugins/bundle"),
        };
        for(int i = 0; i < paths.length; i++){
            Path path = paths[i];
            Path target = targets[i];
            assertEquals(path.toString() + " -> " + target.toString(), target, installer.resolve(path));
        }
    }

    @Test
    public void cosmeticsTest(){
        Path[] paths = new Path[]{
            Paths.get("/foo.cosmetics"),
        };
        Path[] targets = new Path[]{
            Paths.get("/BepInEx/plugins/MoreCompanyCosmetics/foo.cosmetics"),
        };
        for(int i = 0; i < paths.length; i++){
            Path path = paths[i];
            Path target = targets[i];
            assertEquals(path.toString() + " -> " + target.toString(), target, installer.resolve(path));
        }
    }

    @Test
    public void bepInExPackTest(){
        Path[] paths = new Path[]{
            Paths.get("/BepInExPack/BepInEx/core/0Harmony.dll"),
            Paths.get("/BepInExPack/BepInEx/config/BepInEx.cfg"),
            Paths.get("/BepInExPack/doorstop_config.ini"),
            Paths.get("/BepInExPack/winhttp.dll"),
        };
        Path[] targets = new Path[]{
            Paths.get("/BepInEx/core/0Harmony.dll"),
            Paths.get("/BepInEx/config/BepInEx.cfg"),
            Paths.get("/doorstop_config.ini"),
            Paths.get("/winhttp.dll"),
        };
        for(int i = 0; i < paths.length; i++){
            Path path = paths[i];
            Path target = targets[i];
            assertEquals(path.toString() + " -> " + target.toString(), target, installer.resolve(path));
        }
    }

    @Test
    public void blorbsTypo(){
        Path[] paths = new Path[]{
            Paths.get("/BepinEx/plugins/foo.dll"),
            Paths.get("/BepinEx/config/bar.json"),
            Paths.get("/BepinEx/baz"),
        };
        Path[] targets = new Path[]{
            Paths.get("/BepInEx/plugins/foo.dll"),
            Paths.get("/BepInEx/config/bar.json"),
            Paths.get("/BepInEx/baz"),
        };
        for(int i = 0; i < paths.length; i++){
            Path path = paths[i];
            Path target = targets[i];
            assertEquals(path.toString() + " -> " + target.toString(), target, installer.resolve(path));
        }
    }
}
