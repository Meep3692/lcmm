package ca.awoo.lcmm.view;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import ca.awoo.lcmm.Progress;

public class ProgressPanel extends JPanel implements Subscriber<Progress> {
    private final Progress progress;

    private final JProgressBar progressBar = new JProgressBar();
    private final JLabel label = new JLabel();
    private final Color errorColor = Color.red;
    private final Color normalColor = UIManager.getColor("ProgressBar.foreground");

    public ProgressPanel(Progress progress) {
        this.progress = progress;
        progressBar.setStringPainted(true);
        progressBar.setMinimum(0);
        progressBar.setMaximum(1000);
        progressBar.setValue((int)(progress.getProgress() * 1000.0));
        label.setText(progress.getTask());
        this.setBorder(BorderFactory.createTitledBorder(progress.getName()));
        this.add(progressBar);
        this.add(label);
        progress.subscribe(this);
    }

    public void update() {
        progressBar.setValue((int)(progress.getProgress() * 1000.0));
        label.setText(progress.getTask());
        switch(progress.getStatus()){
            case STARTING:
                progressBar.setIndeterminate(true);
                break;
            case RUNNING:
            case FINISHED:
                progressBar.setIndeterminate(false);
                progressBar.setForeground(normalColor);
                break;
            case FAILED:
                progressBar.setIndeterminate(false);
                progressBar.setForeground(errorColor);
                break;
        }
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(Progress item) {
        SwingUtilities.invokeLater(() -> update());
    }

    @Override
    public void onError(Throwable throwable) {
        
    }

    @Override
    public void onComplete() {
        
    }
}
