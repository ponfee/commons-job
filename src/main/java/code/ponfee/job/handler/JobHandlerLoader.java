package code.ponfee.job.handler;

import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.compile.exception.CompileExprException;
import code.ponfee.commons.compile.impl.JdkCompiler;
import code.ponfee.commons.compile.model.JavaSource;
import code.ponfee.commons.compile.model.RegexJavaSource;

/**
 * Job handler loader class
 * 
 * @author Ponfee
 */
public final class JobHandlerLoader {

    private static final JobHandler DEFAULT_HANDLER = new DefaultJobHandler();

    /**
     * 加载类：类全限定名或源代码
     * 
     * @param handler the source code or class full qualifier name of JobHandler
     * @return a JobHandler instance
     * @throws ReflectiveOperationException
     * @throws CompileExprException
     */
    public static JobHandler loadHandler(String handler)
        throws ReflectiveOperationException, CompileExprException {
        if (StringUtils.isBlank(handler)) {
            return DEFAULT_HANDLER;
        }
        try {
            JavaSource source = new RegexJavaSource(handler);
            // source code, compileForce
            return (JobHandler) new JdkCompiler().compile(source).newInstance();
        } catch (Exception e) {
            // class full qualifier name
            return (JobHandler) Class.forName(handler).newInstance();
        }
    }
}
