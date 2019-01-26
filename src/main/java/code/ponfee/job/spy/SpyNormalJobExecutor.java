package code.ponfee.job.spy;

import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import code.ponfee.commons.compile.exception.CompileExprException;
import code.ponfee.commons.reflect.CglibUtils;
import code.ponfee.job.common.Constants;
import code.ponfee.job.dao.ISchedJobDao;
import code.ponfee.job.dao.cache.SchedJobCached;
import code.ponfee.job.exception.JobExecuteException;
import code.ponfee.job.handler.JobHandlerLoader;
import code.ponfee.job.model.SchedJob;
import code.ponfee.job.model.SchedLog;

/**
 * Simply Normal job executor
 * 
 * @author Ponfee
 */
public class SpyNormalJobExecutor implements SpyJobExecutor {

    private static Logger logger = LoggerFactory.getLogger(SpyNormalJobExecutor.class);

    private final ISchedJobDao schedJobDao;
    private final SchedJobCached schedJobCached;

    public SpyNormalJobExecutor(ISchedJobDao schedJobDao, 
                                SchedJobCached schedJobCached) {
        this.schedJobDao = schedJobDao;
        this.schedJobCached = schedJobCached;
    }

    @Override
    public void execute(SchedJob job) {
        Date start = new Date(); Exception ex = null; boolean result = false;
        try {
            if (logger.isInfoEnabled()) {
                logger.info("schedule job execute begin [{}-{}]", job.getId(), job.getName());
            }
            SchedJob duplicate = new SchedJob();
            CglibUtils.copyProperties(job, duplicate);
            result = JobHandlerLoader.loadHandler(job.getHandler()).handle(duplicate);
            if (logger.isInfoEnabled()) {
                logger.info("schedule job execute end [{}-{}]", job.getId(), job.getName());
            }
        } catch (ReflectiveOperationException | CompileExprException e) {
            ex = e;
            logger.error("job handler invalid [{}-{}-{}]", job.getId(), job.getName(), job.getHandler(), e);
        } catch (JobExecuteException e) {
            ex = e;
            logger.error("job execute exception [{}-{}]", job.getId(), job.getName(), e);
        } catch (Exception e) {
            ex = e;
            logger.error("job execute error [{}-{}]", job.getId(), job.getName(), e);
        } finally {
            boolean isManual = Optional.ofNullable(job.getManualTrigger()).orElse(false)
                            || job.getLastSchedTime() == null;
            if (isManual) {
                try {
                    // 手动触发执行完成后清空缓存
                    this.schedJobCached.doneTrigger(job.getId());
                } catch (Exception err) {
                    logger.error("job done trigger error [{}-{}]", job.getId(), job.getName(), err);
                }
            } else {
                try {
                    schedJobDao.doneExecution(job); // 执行完成
                } catch (Exception err) {
                    logger.error("job done execution error [{}-{}]", job.getId(), job.getName(), err);
                }
                try {
                    schedJobDao.incrAndRank(Constants.SERVER_INSTANCE, job.getScore() * -1);
                } catch (Exception err) {
                    logger.error("job incr and rank error [{}-{}]", job.getId(), job.getName(), err);
                }
            }

            // 日志记录
            String exception = (ex == null) ? null : Throwables.getStackTraceAsString(ex);
            if (exception != null && exception.length() > Constants.MAX_ERROR_LENGTH) {
                exception = exception.substring(0, Constants.MAX_ERROR_LENGTH);
            }
            try {
                schedJobDao.recordLog(new SchedLog(
                    Constants.SERVER_INSTANCE, result, isManual, job.getId(), job.getName(), 
                    job.getExecParams(), job.getLastSchedTime(), start, new Date(), exception
                ));
            } catch (Exception err) {
                logger.error("job record log error [{}-{}]", job.getId(), job.getName(), err);
            }
        }
    }

}
