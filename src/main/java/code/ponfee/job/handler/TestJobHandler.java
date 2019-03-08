package code.ponfee.job.handler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import code.ponfee.commons.model.Result;
import code.ponfee.commons.util.Dates;
import code.ponfee.commons.util.SpringContextHolder;
import code.ponfee.job.model.SchedJob;

/**
 * The job handler test class
 * 
 * @author Ponfee
 */
@JobHandlerMeta("用于测试的调度器")
public class TestJobHandler implements JobHandler<Void> {

    @Override
    public Result<Void> handle(SchedJob job) {
        ThreadPoolTaskExecutor task = SpringContextHolder.getBean(ThreadPoolTaskExecutor.class);
        int size = task.getCorePoolSize();
        List<Future<Boolean>> calls = new ArrayList<>();
        System.out.println("=======job start " + Dates.format(new Date()) + "  " + job.getName());
        for (int i = 0; i < size; i++) {
            calls.add(task.submit(new TestExecutor(job)));
        }
        for (Future<Boolean> future : calls) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("=======job end " + Dates.format(new Date()) + "  " + job.getName());
        return Result.SUCCESS;
    }

    private static final class TestExecutor implements Callable<Boolean> {
        private SchedJob job;

        private TestExecutor(SchedJob job) {
            this.job = job;
        }

        @Override
        public Boolean call() {
            Thread t = Thread.currentThread();
            String s = job.getName() + "[" + t.getThreadGroup().getName() + "-" + t.getId() + "-" + t.getName() + "]";
            System.out.println(s + " start " + Dates.format(new Date()));
            try {
                Thread.sleep(30000 + ThreadLocalRandom.current().nextInt(60000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(s + " end " + Dates.format(new Date()));

            return true;
        }
    }

}
