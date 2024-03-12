package ca.awoo.lcmm;

import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;

/**
 * Represents a progress update
 * <p>
 * This class is used to provide feedback to the user about the progress of a long running task.
 * </p>
 * <p>
 * The name of the overall task is provided, as well as the progress of the overall task, and the current subtask.
 * The progress is a double between 0 and 1, where 0 is no progress and 1 is complete, which can be used to populate a progress bar.
 * The subtask describes a smaller step in the task to provide more feedback to the user.
 * </p>
 */
public class Progress implements Publisher<Progress> {

    public enum Status {
        STARTING,
        RUNNING,
        FINISHED,
        FAILED
    }

    private final String name;
    private double progress;
    private String task;
    private Status status;

    private final LatestPublisher<Progress> publisher = new LatestPublisher<>();

    /**
     * Create a new progress object
     * @param name The name of the overall task
     * @param progress The progress of the overall task
     * @param task The current subtask
     */
    public Progress(String name, double progress, String task) {
        this.name = name;
        this.progress = progress;
        this.task = task;
        this.status = Status.STARTING;
    }

    public String getName() {
        return name;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void update(String task){
        this.task = task;
        publisher.publish(this);
    }

    public void update(double progress){
        this.progress = progress;
        publisher.publish(this);
    }

    public void update(Status status){
        this.status = status;
        publisher.publish(this);
    }

    public void update(String task, double progress){
        this.task = task;
        this.progress = progress;
        publisher.publish(this);
    }

    public void update(String task, Status status){
        this.task = task;
        this.status = status;
        publisher.publish(this);
    }

    public void update(double progress, Status status){
        this.progress = progress;
        this.status = status;
        publisher.publish(this);
    }

    public void update(String task, double progress, Status status){
        this.task = task;
        this.progress = progress;
        this.status = status;
        publisher.publish(this);
    }

    public void publish(){
        publisher.publish(this);
    }

    @Override
    public void subscribe(Subscriber<? super Progress> subscriber) {
        publisher.subscribe(subscriber);
    }
}
