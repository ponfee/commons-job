package code.ponfee.job.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Transient;

import code.ponfee.commons.util.Dates;
import code.ponfee.job.enums.TriggerType;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * The schedule job entity, mapped database table t_sched_job
 * 
 * Cron Expression：*(秒0-59) *(分钟0-59) *(小时0-23) *(日期1-31) *(月份1-12或是JAN-DEC) *(星期1-7或是SUN-SAT)
 * https://www.cnblogs.com/lazyInsects/p/8075487.html
 * https://www.cnblogs.com/hongwz/p/5831036.html
 * 
 * @author Ponfee
 */
public class SchedJob implements Serializable {

    private static final long serialVersionUID = -8101980833627039097L;

    public static final int STATUS_STOP = 0;
    public static final int STATUS_START = 1;

    /** 配置信息 */
    private Long id; // 编号
    private String name; // 名称
    private Integer triggerType; // 触发类型
    private String triggerSched; // 触发计划
    private String handler; // 任务处理器
    private Integer status; // 状态：0停止；1启动；
    private Boolean concurrentSupport; // 是否支持并发执行：0不支持；1支持；
    private Boolean recoverySupport; // 是否支持恢复执行：0不支持；1支持；
    private Integer score; // 权重分数
    private String execParams; // 执行参数

    @JsonFormat(pattern = Dates.DEFAULT_DATE_FORMAT)
    @DateTimeFormat(pattern = Dates.DEFAULT_DATE_FORMAT)
    private Date startTime; // 任务开始时间（为空不限制）

    @JsonFormat(pattern = Dates.DEFAULT_DATE_FORMAT)
    @DateTimeFormat(pattern = Dates.DEFAULT_DATE_FORMAT)
    private Date endTime; // 任务结束时间（为空不限制）

    private String remark; // 备注

    /** 调度信息：手动触发不作记录 */
    private Boolean isExecuting; // 是否正在执行：0否；1是；
    private Long execTimeMillis; // 本次执行时间（毫秒）
    private Date lastSchedTime; // 上一次的调度时间
    private String lastSchedServer; // 上一次的调度服务器IP
    private Date nextSchedTime; // 下一次的调度时间

    /** 后台管理信息 */
    private String createBy; // 创建人
    private Date   createTm; // 创建时间
    private String updateBy; // 最近修改人
    private Date   updateTm; // 最近修时间
    private int version; // 版本号

    // ------------------------------------------------非表字段
    @Transient
    private transient Boolean manualTrigger; // 是否手动触发：true是；false否；

    public SchedJob() {}

    public SchedJob(long id, int version) {
        this.id = id;
        this.version = version;
    }

    public String jobName() {
        return String.valueOf(id);
    }

    // ------------------------------------------------getter/setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(Integer triggerType) {
        this.triggerType = triggerType;
    }

    public String getTriggerSched() {
        return triggerSched;
    }

    public void setTriggerSched(String triggerSched) {
        this.triggerSched = triggerSched;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Boolean getConcurrentSupport() {
        return concurrentSupport;
    }

    public void setConcurrentSupport(Boolean concurrentSupport) {
        this.concurrentSupport = concurrentSupport;
    }

    public Boolean getRecoverySupport() {
        return recoverySupport;
    }

    public void setRecoverySupport(Boolean recoverySupport) {
        this.recoverySupport = recoverySupport;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getExecParams() {
        return execParams;
    }

    public void setExecParams(String execParams) {
        this.execParams = execParams;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Boolean getIsExecuting() {
        return isExecuting;
    }

    public void setIsExecuting(Boolean isExecuting) {
        this.isExecuting = isExecuting;
    }

    public Long getExecTimeMillis() {
        return execTimeMillis;
    }

    public void setExecTimeMillis(Long execTimeMillis) {
        this.execTimeMillis = execTimeMillis;
    }

    public Date getLastSchedTime() {
        return lastSchedTime;
    }

    public void setLastSchedTime(Date lastSchedTime) {
        this.lastSchedTime = lastSchedTime;
    }

    public String getLastSchedServer() {
        return lastSchedServer;
    }

    public void setLastSchedServer(String lastSchedServer) {
        this.lastSchedServer = lastSchedServer;
    }

    public Date getNextSchedTime() {
        return nextSchedTime;
    }

    public void setNextSchedTime(Date nextSchedTime) {
        this.nextSchedTime = nextSchedTime;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTm() {
        return createTm;
    }

    public void setCreateTm(Date createTm) {
        this.createTm = createTm;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTm() {
        return updateTm;
    }

    public void setUpdateTm(Date updateTm) {
        this.updateTm = updateTm;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    // ---------------------------------------------------------non table column
    public void triggerType(TriggerType trigger) {
        this.triggerType = trigger.type();
    }

    @Transient
    public Boolean getManualTrigger() {
        return manualTrigger;
    }

    public void setManualTrigger(Boolean manualTrigger) {
        this.manualTrigger = manualTrigger;
    }

}
