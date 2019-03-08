package code.ponfee.job.handler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.compile.exception.CompileExprException;
import code.ponfee.commons.compile.impl.JdkCompiler;
import code.ponfee.commons.compile.model.RegexJavaSource;
import code.ponfee.commons.jce.digest.DigestUtils;

/**
 * Job handler loader class
 * 
 * @author Ponfee
 */
public final class JobHandlerLoader {

    private static final JobHandler<Void> NOOP_HANDLER = new NoopJobHandler();
    private static final JdkCompiler COMPILER = new JdkCompiler();
    private static final ConcurrentMap<String, Class<? extends JobHandler<?>>> REGISTERED = new ConcurrentHashMap<>();

    /**
     * 加载类：类全限定名或源代码
     * 
     * @param handlerText the source code or class full qualifier name of JobHandler
     * @return a JobHandler instance
     * @throws ReflectiveOperationException
     * @throws CompileExprException
     */
    @SuppressWarnings("unchecked")
    public static JobHandler<?> loadHandler(String handlerText) throws Exception {
        if (StringUtils.isBlank(handlerText)) {
            return NOOP_HANDLER;
        }

        Class<? extends JobHandler<?>> handlerClass;
        if (RegexJavaSource.QUALIFIER_PATTERN.matcher(handlerText).matches()) {
            // class full qualifier name
            handlerClass = (Class<? extends JobHandler<?>>) Class.forName(handlerText);
        } else {
            String key = DigestUtils.md5Hex(handlerText);
            if ((handlerClass = REGISTERED.get(key)) == null) {
                // source code, compileForce
                REGISTERED.put(key, handlerClass = (Class<? extends JobHandler<?>>) COMPILER.compile(handlerText));
            }
        }

        return handlerClass.newInstance();
    }

}
