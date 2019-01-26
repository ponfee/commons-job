package code.ponfee.job.spy;
import static code.ponfee.commons.concurrent.ThreadPoolExecutors.CALLER_RUN_SCHEDULER;
import static code.ponfee.job.dao.cache.SchedJobCached.SCHED_SCORES_KEY;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import code.ponfee.commons.jedis.spring.SpringRedisLock;
import code.ponfee.commons.util.ObjectUtils;

/**
 * Checks the server nodes whether alive for cluster
 * 
 * @author Ponfee
 */
public class ClusterServerHeartBeat {
 
    private static Logger logger = LoggerFactory.getLogger(ClusterServerHeartBeat.class);

    private static final String HEARTBEAT_SERVER_KEY = "sched:clusterserver:heartbeat";
    private static final byte[] HEARTBEAT_SERVER_BYT = HEARTBEAT_SERVER_KEY.getBytes();
    private static final int    HEARTBEAT_SERVER_EXP = 86400;

    private final ZSetOperations<String, String> zset;
    private final long sentenceDeathMillis;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ClusterServerHeartBeat(final RedisTemplate redis, 
                                  int heartbeatRateSeconds, 
                                  int sentenceDeathSeconds) {
        this.zset = redis.opsForZSet();
        this.sentenceDeathMillis = sentenceDeathSeconds * 1000;

        final Lock lock = new SpringRedisLock(redis, HEARTBEAT_SERVER_KEY, heartbeatRateSeconds);

        // zrange "sched:clusterserver:heartbeat" 0 -1
        CALLER_RUN_SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                if (lock.tryLock()) {
                    redis.executePipelined((RedisCallback<Void>) redisOps -> {
                        redisOps.zRemRangeByScore(HEARTBEAT_SERVER_BYT, 0, minimumScore());
                        redisOps.expire(HEARTBEAT_SERVER_BYT, HEARTBEAT_SERVER_EXP);
                        return null;
                    });

                    Set<String> alive = Optional.ofNullable(zset.range(HEARTBEAT_SERVER_KEY, 0, -1))
                                                .orElse(Collections.emptySet());
                    Optional.ofNullable(
                        zset.range(SCHED_SCORES_KEY, 0, -1)
                    ).orElse(
                        Collections.emptySet()
                    ).stream().filter(
                        ObjectUtils.not(alive::contains)
                    ).forEach(
                        x -> zset.remove(SCHED_SCORES_KEY, x)
                    );
                }
            } catch (Throwable t) {
                logger.error("JedisLock tryLock occur error", t);
            }
        }, 1, heartbeatRateSeconds, TimeUnit.SECONDS);
    }

    public boolean isAlive(String server) {
        Double score = zset.score(HEARTBEAT_SERVER_KEY, server);
        if (score == null) {
            return false;
        }

        if (score < minimumScore()) {
            zset.remove(HEARTBEAT_SERVER_KEY, server); // sentence to death
            return false;
        } else {
            return true;
        }
    }

    public void heartbeat(String server) {
        try {
            zset.add(HEARTBEAT_SERVER_KEY, server, System.currentTimeMillis());
        } catch (Throwable t) {
            logger.error("Cluster server heartbeat occur error: {}", server, t);
        }
    }

    private long minimumScore() {
        return System.currentTimeMillis() - sentenceDeathMillis;
    }

}
