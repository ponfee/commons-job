package code.ponfee.job.queue;

import java.util.*;
import java.text.SimpleDateFormat;

enum TaskState {
    None, Ready, Start, Exe, End
}

class TaskJob {
    private String Id;
    private Date ExeTime;
    private TaskState state;
    private String Name;

    public void setId(String id) {
        Id = id;
    }

    public void setExeTime(Date exeTime) {
        ExeTime = exeTime;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getId() {
        return Id;
    }

    public Date getExeTime() {
        return ExeTime;
    }

    public TaskState getState() {
        return state;
    }

    public String getName() {
        return Name;
    }
}

class TaskJobEvent extends EventObject {

    private static final long serialVersionUID = 6496098798146410884L;

    private List<TaskJob> taskJobs = new ArrayList<>();// 表示门的状态，有“开”和“关”两种

    public TaskJobEvent(Object source, List<TaskJob> taskJobs) {
        super(source);
        this.taskJobs = taskJobs;
    }

    public void setTaskJob(List<TaskJob> taskJobs) {
        this.taskJobs = taskJobs;
    }

    public List<TaskJob> getTaskJob() {
        return this.taskJobs;
    }

}

interface TaskJobListener extends EventListener {
    public void TaskEvent(TaskJobEvent event);
}

public class CycleOnTimeTask {

    //1~60的延时对垒，每秒轮询一次
    // SortedMap> cycleQuery = new SortedMap>();
    Map<Integer, List> cycleQuery = new HashMap<Integer, List>();
    Queue msgq = new LinkedList();

    Timer timer = new Timer();
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");//设置日期格式
    private Collection listeners;

    public CycleOnTimeTask() {
        //初始化队列
        for (int i = 0; i < 60; i++) {
            cycleQuery.put(i, new ArrayList());
        }

        if (listeners == null) {
            listeners = new HashSet();
        }

        // timer.schedule(new TimerTask() {
        // public void run() {
        // System.out.println("TimerTask");
        // Calendar now = Calendar.getInstance();
        // PopTask(now.get(Calendar.SECOND));
        // }
        // }, 1000 , 1000);

    }

    private void notifyListeners(TaskJobEvent event) {
        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            TaskJobListener listener = (TaskJobListener) iter.next();
            listener.TaskEvent(event);
        }
    }

    public void addTaskJobListener(TaskJobListener listener) {

        listeners.add(listener);
    }

    public void removeTaskJobListener(TaskJobListener listener) {
        if (listeners == null) return;
        listeners.remove(listener);
    }

    public void Start() {
        //Console.WriteLine("{0:yyyy-MM-dd HH:mm:ss.fff}---> Start ------------------------------------------------------------", DateTime.Now);
        System.out.printf("%s---> Start", df.format(new Date()));
        System.out.println();

        timer.schedule(new TimerTask() {
            public void run() {
                System.out.println("TimerTask");
                Calendar now = Calendar.getInstance();
                PopTask(now.get(Calendar.SECOND));
            }
        }, 1000, 1000);

        this.timer.purge();
    }

    public void Stop() {
        this.timer.cancel();
        //Console.WriteLine("{0:yyyy-MM-dd HH:mm:ss.fff}---> Stop ------------------------------------------------------------", DateTime.Now);
        System.out.printf("%s---> Stop", df.format(new Date()));
        System.out.println();
    }

    private void onTaskEvent(List<TaskJob> forExeTasks) {

        for (TaskJob tj : forExeTasks) {
            System.out.printf("%s---> onTaskEvent %s : %s", df.format(new Date()), tj.getExeTime(), tj.getName());
            System.out.println();
        }

        TaskJobEvent tje = new TaskJobEvent(this, forExeTasks);
        // tje.setTaskJob(forExeTasks);
        this.notifyListeners(tje);

    }

    //。假设当前时间为15:20:08，当前扫描位置是2，我的任务要在15:22:35这个时刻触发，也就是147秒后。那么我需要循环的圈数就是147/60=2圈，需要被扫描的位置就是(147+2)`=29的地方。计算好任务的坐标后塞到数组中属于它的位置，然后静静等待被消费就好啦。
    public void PushTask(TaskJob tjob) {
        Date dtNow = new Date();
        long delaytimes = (tjob.getExeTime().getTime() - dtNow.getTime()) / 1000;
        ;//延迟时间
        if (delaytimes > 0) {
            Calendar now = Calendar.getInstance();
            int key = ((int) delaytimes + now.get(Calendar.SECOND)) % 60;
            if (cycleQuery.containsKey(key)) {
                cycleQuery.get(key).add(tjob);
            }

            System.out.printf("%s---> push task %s to slot %d", df.format(new Date()), tjob.getName(), key);
            System.out.println();
        }
    }

    private void PopTask(int key) {

        List<TaskJob> forRemove = new ArrayList();
        List<TaskJob> tasks = cycleQuery.get(key);
        for (TaskJob v : tasks) {
            Date dtNow = new Date();
            if (v.getExeTime().getTime() - dtNow.getTime() <= 0) {
                v.setState(TaskState.Ready);
                msgq.add(v);
                forRemove.add(v);
            }
        }

        for (TaskJob v : forRemove) {
            tasks.remove(v);
            // Console.WriteLine("{0:yyyy-MM-dd HH:mm:ss.fff}---> remove to slot {1}-{2} tasks", DateTime.Now, key, v.Name);
            System.out.printf("%s---> remove to slot %d -> %s task", df.format(new Date()), key, v.getName());
            System.out.println();
        }

        //Console.WriteLine("{0:yyyy-MM-dd HH:mm:ss.fff}---> PopTask slot {1} has {2} tasks", DateTime.Now, key, forRemove.Count);
        System.out.printf("%s---> PopTask slot %d has %s tasks", df.format(new Date()), key, forRemove.size());
        System.out.println();
        this.onTaskEvent(forRemove);
    }

    public ArrayList GetTask() {

        ArrayList forExe = new ArrayList();

        while (msgq.size() > 0) {
            forExe.add(msgq.peek());
        }

        return forExe;
    }
}
