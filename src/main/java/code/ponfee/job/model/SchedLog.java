package code.ponfee.job.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Id;
import javax.persistence.Table;

import tk.mybatis.mapper.annotation.KeySql;

/**
 * The schedule log entity, mapped database table t_sched_log
 * 
 * @author Ponfee
 */
@Table(name = "t_sched_log") // 集成Mybatis通用mapper
public class SchedLog implements Serializable {
    private static final long serialVersionUID = 2520801190208386818L;

    @Id // @Id注解的字段必须为包装类型（不能为原始类型，如：long、int）
    @KeySql(useGeneratedKeys = true)
    private Long id; // 主键id

    private String execServer; // 执行服务器IP
    private Boolean isSuccess; // 是否成功：0否；1是；
    private Boolean isManualTrigger; // 是否手动触发执行：0否；1是；
    private Long jobId; // 任务ID
    private String jobName; // 任务名称
    private String execParams; // 执行参数
    private Date schedTime; // 调度时间（为空表示手动触发执行）
    private Date execStartTime; // 执行开始时间
    private Date execEndTime; // 执行结束时间
    private String exception; // 异常信息
    private Date createTm; // 创建时间

    public SchedLog() {}

    public SchedLog(String execServer, boolean isSuccess, boolean isManualTrigger, 
                    long jobId, String jobName, String execParams, Date schedTime, 
                    Date execStartTime, Date execEndTime, String exception) {
        this.execServer = execServer;
        this.isSuccess = isSuccess;
        this.isManualTrigger = isManualTrigger;
        this.jobId = jobId;
        this.jobName = jobName;
        this.execParams = execParams;
        this.schedTime = schedTime;
        this.execStartTime = execStartTime;
        this.execEndTime = execEndTime;
        this.exception = exception;
        this.createTm = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExecServer() {
        return execServer;
    }

    public void setExecServer(String execServer) {
        this.execServer = execServer;
    }

    public Boolean getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(Boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public Boolean getIsManualTrigger() {
        return isManualTrigger;
    }

    public void setIsManualTrigger(Boolean isManualTrigger) {
        this.isManualTrigger = isManualTrigger;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getExecParams() {
        return execParams;
    }

    public void setExecParams(String execParams) {
        this.execParams = execParams;
    }

    public Date getSchedTime() {
        return schedTime;
    }

    public void setSchedTime(Date schedTime) {
        this.schedTime = schedTime;
    }

    public Date getExecStartTime() {
        return execStartTime;
    }

    public void setExecStartTime(Date execStartTime) {
        this.execStartTime = execStartTime;
    }

    public Date getExecEndTime() {
        return execEndTime;
    }

    public void setExecEndTime(Date execEndTime) {
        this.execEndTime = execEndTime;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public Date getCreateTm() {
        return createTm;
    }

    public void setCreateTm(Date createTm) {
        this.createTm = createTm;
    }

}
