package ca.awoo.lcmm.view;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ca.awoo.lcmm.MultiProgress;
import ca.awoo.lcmm.Progress;

public class MultiProgressPanel extends ProgressPanel {

    private final JScrollPane scrollPane = new JScrollPane();
    private final JPanel contentPanel = new JPanel();

    public MultiProgressPanel(MultiProgress progress) {
        super(progress);
        scrollPane.setViewportView(contentPanel);
        for(Progress subProgress : progress.getProgresses()){
            if(subProgress instanceof MultiProgress multi){
                MultiProgressPanel panel = new MultiProgressPanel(multi);
                contentPanel.add(panel);
            }else{
                ProgressPanel panel = new ProgressPanel(subProgress);
                contentPanel.add(panel);
            }
        }
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        this.add(scrollPane);
    }
    
}
