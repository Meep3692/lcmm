package ca.awoo.lcmm;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class App {

    public static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) throws IOException {
        String uuid = "018dc360-ea80-9327-137c-989e716148aa";
        File lcRoot = new File("lcRoot");
        lcRoot.mkdirs();
        Web.installProfile(uuid, lcRoot);
    }
}
