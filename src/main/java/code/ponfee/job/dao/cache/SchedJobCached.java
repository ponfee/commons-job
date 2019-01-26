package code.ponfee.job.dao.cache;

import static code.ponfee.job.common.Constants.CACHE_KEY_PREFIX;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

import code.ponfee.commons.util.MessageFormats;
import code.ponfee.job.model.SchedJob;

/**
 * Schedule job cached
 * 
 * @author Ponfee
 */
@Repository("schedJobCached")
public class SchedJobCached {

    private static final String SCHED_IDS_KEY = CACHE_KEY_PREFIX + "runnable:job:ids";
    private static final String SCHED_JOB_KEY = CACHE_KEY_PREFIX + "job:#{jobId}";
    // zrange "sched:executing:server:scores" 0 -1
    public static final String SCHED_SCORES_KEY = CACHE_KEY_PREFIX + "executing:server:scores";
    private static final String TRIGGER_JOB_KEY = CACHE_KEY_PREFIX + "trigger:#{jobId}";

    private static final int IDS_CACHE_TIME = 900; // 15分钟
    private static final int JOB_CACHE_TIME = 3600; // 1小时

    private @Resource RedisTemplate<String, String> strRedis;
    private @Resource RedisTemplate<String, Object> objRedis;

    // ---------------------------------------------------------job ids
    public void setJobIds(Long... ids) {
        BoundSetOperations<String, String> setOps = strRedis.boundSetOps(SCHED_IDS_KEY);
        setOps.add(Arrays.stream(ids).map(String::valueOf).toArray(String[]::new));
        setOps.expire(IDS_CACHE_TIME, SECONDS);
    }

    public void delJobIds() {
        strRedis.delete(SCHED_IDS_KEY);
    }

    public void remJobId(long id) {
        strRedis.boundSetOps(SCHED_IDS_KEY).remove(String.valueOf(id));
    }

    public Set<Long> getJobIds() {
        Set<String> result = strRedis.boundSetOps(SCHED_IDS_KEY).members();
        if (result == null) {
            return null;
        }
        return result.stream().map(Long::parseLong).collect(Collectors.toSet());
    }

    // ---------------------------------------------------------schedule job
    public void setSchedJob(SchedJob job) {
        String key = MessageFormats.format(SCHED_JOB_KEY, job.getId());
        objRedis.boundValueOps(key).set(job, JOB_CACHE_TIME, SECONDS);
    }

    public void delSchedJob(long jobId) {
        objRedis.delete(MessageFormats.format(SCHED_JOB_KEY, jobId));
    }

    public SchedJob getSchedJob(long jobId) {
        String key = MessageFormats.format(SCHED_JOB_KEY, jobId);
        return (SchedJob) objRedis.boundValueOps(key).get();
    }

    // ---------------------------------------------------------load balance
    public void setScoreServers(Map<String, Double> members) {
        Set<TypedTuple<String>> tuples = members.entrySet().stream().map(
            e -> new DefaultTypedTuple<>(e.getKey(), e.getValue())
        ).collect(Collectors.toSet());

        BoundZSetOperations<String, String> zsetOps = strRedis.boundZSetOps(SCHED_SCORES_KEY);
        zsetOps.add(tuples);
        zsetOps.expire(IDS_CACHE_TIME * 2, SECONDS);
    }

    public long getServerRank(String server) {
        Long rank = strRedis.boundZSetOps(SCHED_SCORES_KEY).rank(server);
        return rank == null ? 0 : rank;
    }

    public boolean incrServerScore(String server, int score) {
        // 正分：服务器准备执行调度；负分：服务器已完成调度
        if (strRedis.getExpire(SCHED_SCORES_KEY, SECONDS) < IDS_CACHE_TIME) {
            return false;
        } else {
            strRedis.boundZSetOps(SCHED_SCORES_KEY).incrementScore(server, score);
            return true;
        }
    }

    // ---------------------------------------------------------manual trigger
    public boolean manualTrigger(long jobId) {
        String key = MessageFormats.format(TRIGGER_JOB_KEY, jobId);
        BoundValueOperations<String, String> valOps = strRedis.boundValueOps(key);
        Boolean result = valOps.setIfAbsent("0");
        if (result != null && result) {
            valOps.expire(30, TimeUnit.MINUTES); // default 30 minutes
            return true;
        } else {
            return false;
        }
    }

    public void doneTrigger(long jobId) {
        strRedis.delete(MessageFormats.format(TRIGGER_JOB_KEY, jobId));
    }

}
