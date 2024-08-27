package ca.awoo.lcmm.sync;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class App extends JFrame {
    private final Cache cache;
    private final ProfilePane profilesPanel = new ProfilePane();

    public App(){
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setTitle("Lethal Company mod manager");
        this.cache = new Cache(new File("."));
        setLayout(new BorderLayout());
        JMenuBar menuBar = new JMenuBar();
        JMenu profileMenu = new JMenu("Profiles");
        profileMenu.setMnemonic('p');
        profileMenu.getAccessibleContext().setAccessibleDescription("Manage profiles");

        JMenuItem newProfile = new JMenuItem("New Profile");
        newProfile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));

        JMenuItem importProfile = new JMenuItem("Import Profile");
        importProfile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));

        importProfile.addActionListener((e) -> {
            String profileId = JOptionPane.showInputDialog(this, "Profile id:", "Import profile", JOptionPane.QUESTION_MESSAGE);
            try {
                profileId = profileId.trim();
                Profile profile = Profile.getProfile(profileId, cache);
                profilesPanel.addProfile(new ProfileWidget(profile));
            } catch (IOException e1) {
                JOptionPane.showMessageDialog(this, e1, "Failed to import profile", JOptionPane.ERROR_MESSAGE);
            }
        });

        profileMenu.add(newProfile);
        profileMenu.add(importProfile);

        menuBar.add(profileMenu);
        add(menuBar, BorderLayout.NORTH);

        add(profilesPanel, BorderLayout.CENTER);

        try {
            Profile[] profiles = Profile.getProfiles(cache);
            for(Profile profile : profiles){
                profilesPanel.addProfile(new ProfileWidget(profile));
            }
        } catch (IOException e1) {
            JOptionPane.showMessageDialog(this, e1, "Failed to import profile", JOptionPane.ERROR_MESSAGE);
        }

        ProfileActions actions = new ProfileActions();
        add(actions, BorderLayout.EAST);

        profilesPanel.listenProfileChange(actions);

        //pack();
        setSize(600, 400);
    }
    public static void main(String[] args){
        new App().setVisible(true);
    }
}
