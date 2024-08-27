package ca.awoo.lcmm.sync;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ProfileWidget extends JPanel{
    private ProfilePane profilePane;
    private boolean selected = false;
    private Profile profile;
    private Image icon;
    private JLabel label;

    public ProfileWidget(Profile profile){
        this.profile = profile;
        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                if(profilePane != null){
                    profilePane.select(ProfileWidget.this);
                } else {
                    //profilePane should never be null in a scenario where we can be clicked
                    JOptionPane.showMessageDialog(ProfileWidget.this, "ProfileWidget " + ProfileWidget.this + " was clicked but somehow isn't part of a ProfilePane", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        try {
            icon = ImageIO.read(getClass().getResourceAsStream("/ca/awoo/lcmm/lcicon.png"));
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        this.setLayout(new GridBagLayout());
        label = new JLabel(profile.getName());
        label.setBackground(Color.lightGray);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx = 0;
        constraints.gridy = 0;
        add(new FixedImage(icon, new Dimension(96, 96)), constraints);
        constraints.gridy += 1;
        add(label, constraints);
    }

    public void setProfilePane(ProfilePane profilePane){
        this.profilePane = profilePane;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
        label.setOpaque(selected);
        repaint();
    }

    public Profile getProfile(){
        return profile;
    }
}
