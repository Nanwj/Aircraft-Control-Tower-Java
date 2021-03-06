package towersim.tasks;

import towersim.util.Encodable;

import java.util.List;
import java.util.StringJoiner;

/**
 * Represents a circular list of tasks for an aircraft to cycle through.
 * @ass1
 */
public class TaskList implements Encodable {
    /** List of tasks to cycle through. */
    private final List<Task> tasks;
    /** Index of current task in tasks list. */
    private int currentTaskIndex;

    /**
     * Creates a new TaskList with the given list of tasks.
     * <p>
     * Initially, the current task (as returned by {@link #getCurrentTask()}) should be the first
     * task in the given list.
     *
     * @param tasks list of tasks
     * @ass1
     */
    public TaskList(List<Task> tasks) {
        if (tasks.size() == 0) {
            throw new IllegalArgumentException();
        }

        for (int indexOfTask = 0; indexOfTask < tasks.size(); indexOfTask++) {
            Task currentTask = tasks.get(indexOfTask);
            Task nextTask = getNextTaskInArraylist(tasks, indexOfTask);
            if (isInvalidTasks(currentTask, nextTask)) {
                // the order of tasks is invalid.
                throw new IllegalArgumentException();
            }
        }

        this.tasks = tasks;
        this.currentTaskIndex = 0;
    }

    /* return the next task inside the arraylist with given index of current task. */
    private static Task getNextTaskInArraylist(List<Task> tasks, int currentTaskIndex) {
        if (currentTaskIndex == tasks.size() - 1) {
            return tasks.get(0);
        } else {
            return tasks.get(currentTaskIndex + 1);
        }
    }

    /* Return true if the given tasks is not valid. */
    private static Boolean isInvalidTasks(Task currentTask, Task nextTask) {
        switch (currentTask.getType()) {
            case TAKEOFF :
                return !nextTask.getType().equals(TaskType.AWAY);
            case LOAD :
                return !nextTask.getType().equals(TaskType.TAKEOFF);
            case WAIT :
            case LAND :
                return !(nextTask.getType().equals(TaskType.LOAD)
                        || nextTask.getType().equals(TaskType.WAIT));
            case AWAY :
                return !(nextTask.getType().equals(TaskType.AWAY)
                        || nextTask.getType().equals(TaskType.LAND));
        }
        return false;
    }

    /**
     * Returns the current task in the list.
     *
     * @return current task
     * @ass1
     */
    public Task getCurrentTask() {
        return this.tasks.get(this.currentTaskIndex);
    }

    /**
     * Returns the task in the list that comes after the current task.
     * <p>
     * After calling this method, the current task should still be the same as it was before calling
     * the method.
     * <p>
     * Note that the list is treated as circular, so if the current task is the last in the list,
     * this method should return the first element of the list.
     *
     * @return next task
     * @ass1
     */
    public Task getNextTask() {
        int nextTaskIndex = (this.currentTaskIndex + 1) % this.tasks.size();
        return this.tasks.get(nextTaskIndex);
    }

    /**
     * Moves the reference to the current task forward by one in the circular task list.
     * <p>
     * After calling this method, the current task should be the next task in the circular list
     * after the "old" current task.
     * <p>
     * Note that the list is treated as circular, so if the current task is the last in the list,
     * the new current task should be the first element of the list.
     * @ass1
     */
    public void moveToNextTask() {
        this.currentTaskIndex = (this.currentTaskIndex + 1) % this.tasks.size();
    }

    /**
     * Returns the human-readable string representation of this task list.
     * <p>
     * The format of the string to return is
     * <pre>TaskList currently on currentTask [taskNum/totalNumTasks]</pre>
     * where {@code currentTask} is the {@code toString()} representation of the current task as
     * returned by {@link Task#toString()},
     * {@code taskNum} is the place the current task occurs in the task list, and
     * {@code totalNumTasks} is the number of tasks in the task list.
     * <p>
     * For example, a task list with the list of tasks {@code [AWAY, LAND, WAIT, LOAD, TAKEOFF]}
     * which is currently on the {@code WAIT} task would have a string representation of
     * {@code "TaskList currently on WAIT [3/5]"}.
     *
     * @return string representation of this task list
     * @ass1
     */
    @Override
    public String toString() {
        return String.format("TaskList currently on %s [%d/%d]",
                this.getCurrentTask(),
                this.currentTaskIndex + 1,
                this.tasks.size());
    }

    /**
     * Returns the machine-readable string representation of this task list.
     *
     * @return encoded string representation of this task list
     */
    @Override
    public String encode() {
        // a StringBuilder to store encodes of task
        StringJoiner result = new StringJoiner(",");
        for (int indexOfTask = 0; indexOfTask < tasks.size(); indexOfTask++) {
            result.add(getCurrentTask().encode());
            moveToNextTask();
        }
        return result.toString();
    }
}
