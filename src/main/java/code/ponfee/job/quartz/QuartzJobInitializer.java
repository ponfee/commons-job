package code.ponfee.job.quartz;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.model.PageHandler;
import code.ponfee.commons.util.SpringContextHolder;
import code.ponfee.job.dao.ISchedJobDao;
import code.ponfee.job.model.SchedJob;

/**
 * Load job to quartz
 * 
 * @author Ponfee
 */
public class QuartzJobInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private static Logger logger = LoggerFactory.getLogger(QuartzJobInitializer.class);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // check the container is root container
        if (event.getApplicationContext().getParent() == null) {
            Map<String, Object> params = new HashMap<>(PageHandler.QUERY_ALL_PARAMS);
            params.put("status", SchedJob.STATUS_START);
            QuartzJobManager jobManager = SpringContextHolder.getBean(QuartzJobManager.class);
            ISchedJobDao schedJobDao = SpringContextHolder.getBean(ISchedJobDao.class);
            // TODO: scroll page query all records
            schedJobDao.queryJobsForPage(params).forEach(job -> {
                try {
                    jobManager.addOrUpdateJob(job);
                } catch (Exception e) {
                    logger.error("QuartzJobManager add job occur error: {}.", Jsons.toJson(job), e);
                }
            });
        }
    }

}
