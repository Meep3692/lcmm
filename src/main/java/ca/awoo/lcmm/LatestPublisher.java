package ca.awoo.lcmm;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

public class LatestPublisher<T> implements Publisher<T>{

    private class LatestSubscription implements Subscription{

        long requesting = 0;

        private final Subscriber<? super T> subscriber;
        private final ExecutorService executor;

        public LatestSubscription(Subscriber<? super T> subscriber, ExecutorService executor){
            this.subscriber = subscriber;
            this.executor = executor;
        }

        @Override
        public void request(long n) {
            requesting += n;
        }

        @Override
        public void cancel() {
            requesting = 0;
        }

        public void publish(T item){
            if (requesting > 0) {
                requesting--;
                executor.submit(() -> subscriber.onNext(item));
            }
        }

    }

    private final HashSet<LatestSubscription> subscriptions = new HashSet<>();

    private final ExecutorService executorService;

    public LatestPublisher(ExecutorService executorService){
        this.executorService = executorService;
    }

    public LatestPublisher(){
        this(ForkJoinPool.commonPool());
    }

    @Override
    public void subscribe(Subscriber<? super T> subscriber) {
        LatestSubscription subscription = new LatestSubscription(subscriber, executorService);
        subscriptions.add(subscription);
        subscriber.onSubscribe(subscription);
    }

    public void publish(T item){
        for (LatestSubscription subscription : subscriptions) {
            subscription.publish(item);
        }
    }
    
}
