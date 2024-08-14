package ca.awoo.lcmm.sync;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JComponent;

public class FixedImage extends JComponent {
    private final Image image;
    private final Dimension dims;
    
    public FixedImage(Image image, Dimension dims) {
        this.image = image;
        this.dims = dims;
    }

    @Override
    public Dimension getPreferredSize(){
        return dims;
    }

    @Override
    public Dimension preferredSize(){
        return dims;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.drawImage(image, 0, 0, (int)dims.getWidth(), (int)dims.getHeight(), null);
    }

    
}
