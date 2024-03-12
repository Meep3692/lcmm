package ca.awoo.lcmm;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.swing.JFrame;

import ca.awoo.lcmm.view.ProgressPanel;

public class App {

    public static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) throws IOException {
        /*FileHandler fileHandler = new FileHandler("output.txt");
        logger.addHandler(fileHandler);
        String uuid = "018dc360-ea80-9327-137c-989e716148aa";
        File lcRoot = new File("lcRoot");
        lcRoot.mkdirs();
        Web.installProfile(uuid, lcRoot).join();*/
        JFrame frame = new JFrame();
        Progress progress = new Progress("Test Progress", 0.25, "Test task");
        ProgressPanel pp = new ProgressPanel(progress);
        frame.add(pp);
        frame.setVisible(true);
    }
}
