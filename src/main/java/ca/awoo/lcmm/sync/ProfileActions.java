package ca.awoo.lcmm.sync;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JPanel;

public class ProfileActions extends JPanel implements Consumer<Profile>{
    private Profile profile;
    private final JButton installButton;

    public ProfileActions(){
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.LINE_START;
        installButton = new JButton("Install");
        installButton.setEnabled(false);
        add(installButton, constraints);
    }

    @Override
    public void accept(Profile profile) {
        this.profile = profile;
        if(profile != null){
            installButton.setEnabled(true);
        }else{
            installButton.setEnabled(false);
        }
    }
}
