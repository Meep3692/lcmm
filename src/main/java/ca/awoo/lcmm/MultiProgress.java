package ca.awoo.lcmm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

public class MultiProgress extends Progress implements Subscriber<Progress> {

    private final List<Progress> progresses = new ArrayList<>();

    public MultiProgress(String name, Progress... progresses){
        super(name, 0, "Starting");
        for (Progress progress : progresses) {
            this.progresses.add(progress);
            progress.subscribe(this);
        }
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(Progress item) {
        double total = 0;
        for (Progress progress : progresses) {
            total += progress.getProgress();
        }
        total /= progresses.size();
        setProgress(total);
        if(total == 1){
            setStatus(Status.FINISHED);
        }
        setTask(item.getName() + ": " + item.getTask());
        publish();
    }

    @Override
    public void onError(Throwable throwable) {
        
    }

    @Override
    public void onComplete() {
        
    }
    
}
