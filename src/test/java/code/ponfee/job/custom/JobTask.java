package code.ponfee.job.custom;

import java.io.Serializable;

public class JobTask<T extends Comparable<? super T> & Serializable>
    implements Comparable<JobTask<T>>, Serializable {

    private static final long serialVersionUID = -4961860122713096934L;

    private final T id;
    private final long nextExecTimeMillis;

    public JobTask(T id, long nextExecTimeMillis) {
        this.id = id;
        this.nextExecTimeMillis = nextExecTimeMillis;
    }

    public T getId() {
        return id;
    }

    public long getNextExecTimeMillis() {
        return nextExecTimeMillis;
    }

    @Override
    public int compareTo(JobTask<T> o) {
        int a = (int) (this.nextExecTimeMillis - o.nextExecTimeMillis);
        return a != 0 ? a : this.id.compareTo(o.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode() + (int) (nextExecTimeMillis ^ (nextExecTimeMillis >>> 32));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JobTask)) {
            return false;
        }

        JobTask<T> other = (JobTask<T>) obj;
        return this.id.equals(other.id) 
            && this.nextExecTimeMillis == other.nextExecTimeMillis;
    }

    @Override
    public String toString() {
        return "TaskJob [id=" + id + ", nextExecTimeMillis=" + nextExecTimeMillis + "]";
    }

}
