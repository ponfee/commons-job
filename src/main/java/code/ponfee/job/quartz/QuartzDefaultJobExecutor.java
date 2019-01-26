package code.ponfee.job.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;

/**
 * The default quartz job executor
 * 
 * @author Ponfee
 */
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class QuartzDefaultJobExecutor extends QuartzJobExecutor {
}
