package code.ponfee.job.exception;

/**
 * Job execute exception 
 * 
 * @author Ponfee
 */
public class JobExecuteException extends RuntimeException {
    private static final long serialVersionUID = 2625614802822753946L;

    public JobExecuteException() {
        super();
    }

    public JobExecuteException(String message) {
        super(message);
    }

    public JobExecuteException(Throwable cause) {
        super(cause);
    }

    public JobExecuteException(String message, Throwable cause) {
        super(message, cause);
    }

}
