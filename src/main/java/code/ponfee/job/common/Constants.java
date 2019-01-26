package code.ponfee.job.common;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.resource.ResourceScanner;
import code.ponfee.commons.util.Networks;
import code.ponfee.commons.util.ObjectUtils;
import code.ponfee.job.handler.JobHandlerMeta;

/**
 * Job constants
 * 
 * @author Ponfee
 */
@SuppressWarnings("unchecked")
public final class Constants {

    public static final String CACHE_KEY_PREFIX = "sched:"; // cache key prefix of sched job

    public static final Long NONE_JOB_PLACEHOLDER = 0L;

    public static final int MAX_ERROR_LENGTH = 4000;

    public static final String SERVER_INSTANCE = Networks.HOST_IP + ":" + ObjectUtils.uuid22();

    public static final Map<String, String> JOB_HANDLERS;
    static {
        Set<Class<?>> classes = new ResourceScanner("code.ponfee.job")
                                     .scan4class(JobHandlerMeta.class);
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        for (Class<?> clazz : classes) {
            builder.put(
                ClassUtils.getClassName(clazz), 
                clazz.getAnnotation(JobHandlerMeta.class).value()
            );
        }
        JOB_HANDLERS = builder.build();
    }

}
