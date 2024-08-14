package ca.awoo.lcmm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

public class MultiProgress extends Progress implements Subscriber<Progress> {

    private final List<Progress> progresses = new ArrayList<>();

    public MultiProgress(String name){
        super(name, 0, "Starting");
    }

    public MultiProgress(String name, Progress... progresses){
        this(name);
        for (Progress progress : progresses) {
            this.progresses.add(progress);
            progress.subscribe(this);
        }
    }

    public void addProgress(Progress progress){
        progresses.add(progress);
        progress.subscribe(this);
    }

    public List<Progress> getProgresses(){
        return progresses;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(Progress item) {
        if(item.getStatus() == Status.RUNNING){
            setStatus(Status.RUNNING);
        }
        double total = 0;
        boolean finished = true;
        for (Progress progress : progresses) {
            total += progress.getProgress();
            if(!(progress.getStatus() == Status.FINISHED)){
                finished = false;
            }
        }
        total /= progresses.size();
        setProgress(total);
        if(finished){
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
