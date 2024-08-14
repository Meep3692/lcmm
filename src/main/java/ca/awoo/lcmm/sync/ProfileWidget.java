package ca.awoo.lcmm.sync;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ProfileWidget extends JPanel{
    private ProfilePane profilePane;
    private boolean selected = false;
    private Optional<Profile> profile;
    private Image icon;
    private JLabel label;

    public ProfileWidget(){
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
        this.profile = Optional.empty();
        try {
            icon = ImageIO.read(getClass().getResourceAsStream("/ca/awoo/lcmm/lcicon.png"));
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        this.setLayout(new GridBagLayout());
        label = new JLabel(profileName());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.CENTER;
        //add(label, constraints);
    }

    public ProfileWidget(Profile profile){
        this();
        this.profile = Optional.of(profile);
    }

    public void setProfilePane(ProfilePane profilePane){
        this.profilePane = profilePane;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
    }

    private final Dimension preferedSize = new Dimension(96, 96);

    @Override
    public Dimension getPreferredSize() {
        return preferedSize;
    }

    @Override
    public Dimension preferredSize() {
        return preferedSize;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.LIGHT_GRAY);
        String profileName = profileName();
        Graphics2D g2 = (Graphics2D)g;
        Rectangle2D nameSize = g.getFont().getStringBounds(profileName, g2.getFontRenderContext());
        if(((int)preferedSize.getHeight()) != 96+(int)nameSize.getHeight()){
            preferedSize.setSize(96, 96+nameSize.getHeight());
            validate();
        }
        g.fillRect(0, 96, 96, (int)nameSize.getHeight());
        g.drawImage(icon, 0, 0, 96, 96, null);
        g.setColor(Color.BLACK);
        g.drawString(profileName, 48-(int)nameSize.getCenterX(), 96);
    }

    private String profileName(){
        if(profile.isPresent()){
            return profile.get().getName();
        }else{
            return "Loading";
        }
    }
}
