package ca.awoo.lcmm;

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
public class Progress {
    private final String name;
    private final double progress;
    private final String task;

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
    }

    public String getName() {
        return name;
    }

    public double getProgress() {
        return progress;
    }

    public String getTask() {
        return task;
    }
}
