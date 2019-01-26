package code.ponfee.job.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import code.ponfee.commons.constrain.Constraints;
import code.ponfee.commons.constrain.MethodValidator;

/**
 * Service methods arguments validator
 * 
 * <aop:aspectj-autoproxy />
 * 
 * @author Ponfee
 */
@Component
@Aspect
public class JobCenterValidator extends MethodValidator {

    @Around(
        value = "execution(public * code.ponfee.job.service.impl..*Impl..*(..)) && @annotation(cst)", 
        argNames = "pjp,cst"
    )
    public @Override Object constrain(ProceedingJoinPoint pjp, Constraints cst) throws Throwable {
        return super.constrain(pjp, cst);
    }

}
