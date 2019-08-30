package code.ponfee.job.queue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.*;

import static java.lang.Thread.*;

class StateChangeToOneListener implements TaskJobListener {

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");//设置日期格式
    Random ra = new Random();

    void exeTaskJob(TaskJob tj) {

        //ExecutorService executor = Executors.newSingleThreadExecutor();
        ExecutorService executor = Executors.newFixedThreadPool(5);
        FutureTask future =
            new FutureTask(new Callable() {//使用Callable接口作为构造参数
                public String call() throws InterruptedException {
                    //真正的任务在这里执行，这里的返回值类型为String，可以为任意类型
                    System.out.printf("%s---> client start exe timetask by async %s : %s", df.format(new Date()), tj.getExeTime(), tj.getName());
                    System.out.println();

                    Thread.sleep(ra.nextInt(10000) + 1);

                    System.out.printf("%s---> client end exe timetask by async %s : %s", df.format(new Date()), tj.getExeTime(), tj.getName());
                    System.out.println();
                    return "exeTaskJob OK";
                }
            });
        executor.execute(future);

    }

    @Override
    public void TaskEvent(TaskJobEvent tjs) {
        for (TaskJob tj : tjs.getTaskJob()) {
            exeTaskJob(tj);
            // System.out.printf("%s---> client start exe timetask by async %s : %s",df.format(new Date()), tj.getExeTime(), tj.getName());
            // System.out.println();
            ////// thread.sleep()
            // System.out.printf("%s---> client end exe timetask by async %s : %s",df.format(new Date()), tj.getExeTime(), tj.getName());
        }

    }
}

public class CycleOnTimeTaskTest {

    public static void main(String[] args) {

        CycleOnTimeTask ctt = new CycleOnTimeTask();

        for (int i = 0; i < 60; i++) {
            // Thread.Sleep(1);

            TaskJob tj = new TaskJob();
            tj.setName("job" + i);
            long currentTime = System.currentTimeMillis();
            currentTime += (15 + i) * 1000;
            Date date = new Date(currentTime);
            tj.setExeTime(date);

            ctt.PushTask(tj);
        }

        ctt.addTaskJobListener(new StateChangeToOneListener());
        ctt.Start();
    }
}