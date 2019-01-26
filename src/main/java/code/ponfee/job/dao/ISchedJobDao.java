package code.ponfee.job.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import code.ponfee.commons.model.Page;
import code.ponfee.job.model.SchedJob;
import code.ponfee.job.model.SchedLog;

/**
 * sched job dao interface
 * 
 * @author Ponfee
 */
public interface ISchedJobDao {

    boolean add(SchedJob job);

    boolean update(SchedJob job);

    boolean toggle(SchedJob job);

    boolean correctExec(SchedJob job);

    SchedJob get(long jobId);

    boolean delete(long jobId, int version);

    Page<SchedJob> queryJobsForPage(Map<String, ?> params);

    Collection<Long> listJobIds();

    boolean tryAcquire(SchedJob job);

    boolean doneExecution(SchedJob job);

    long incrAndRank(String server, int score);

    /** sched log */
    boolean recordLog(SchedLog log);

    boolean recordLog(List<SchedLog> logs);

    Page<SchedLog> queryLogsForPage(Map<String, ?> params);
}
