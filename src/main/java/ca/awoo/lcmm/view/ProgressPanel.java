package ca.awoo.lcmm.view;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import ca.awoo.lcmm.Progress;

public class ProgressPanel extends JPanel implements Subscriber<Progress> {
    private final Progress progress;

    private final JProgressBar progressBar = new JProgressBar();
    private final JLabel label = new JLabel();
    private final Color errorColor = Color.red;
    private final Color normalColor = UIManager.getColor("ProgressBar.foreground");
    private final Color completedColor = Color.green;

    public ProgressPanel(Progress progress) {
        this.progress = progress;
        progressBar.setStringPainted(true);
        progressBar.setMinimum(0);
        progressBar.setMaximum(1000);
        progressBar.setValue((int)(progress.getProgress() * 1000.0));
        label.setText(progress.getTask());
        this.setBorder(BorderFactory.createTitledBorder(progress.getName()));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
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
                progressBar.setIndeterminate(false);
                progressBar.setForeground(normalColor);
                break;
            case FINISHED:
                progressBar.setIndeterminate(false);
                progressBar.setForeground(completedColor);
                break;
            case FAILED:
                progressBar.setIndeterminate(false);
                progressBar.setForeground(errorColor);
                break;
        }
    }

    private Subscription subscription;

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(Progress item) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                update();
                subscription.request(1);
            });
        } catch (InvocationTargetException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onError(Throwable throwable) {
        
    }

    @Override
    public void onComplete() {
        
    }
}
