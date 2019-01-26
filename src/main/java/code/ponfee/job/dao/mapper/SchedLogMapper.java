package code.ponfee.job.dao.mapper;

import java.util.List;
import java.util.Map;

import code.ponfee.job.model.SchedLog;
import tk.mybatis.mapper.common.Mapper;

/**
 * Sched log mapper
 * 
 * @author Ponfee
 */
public interface SchedLogMapper extends Mapper<SchedLog> {

    int batchInsert(List<SchedLog> logs);

    List<SchedLog> query4list(Map<String, ?> params);

}
