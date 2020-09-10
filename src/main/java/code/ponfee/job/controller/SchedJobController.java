package code.ponfee.job.controller;

import code.ponfee.commons.model.Page;
import code.ponfee.commons.model.PageRequestParams;
import code.ponfee.commons.model.Result;
import code.ponfee.job.model.SchedJob;
import code.ponfee.job.model.SchedLog;
import code.ponfee.job.service.ISchedJobService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * Sched Job Controller
 * 
 * @author Ponfee
 */
@RequestMapping("sched")
@RestController
public class SchedJobController {

    private @Resource ISchedJobService schedJobService;

    @PostMapping("job/add")
    public Result<Long> addJob(SchedJob job) {
        job.setCreateBy("username");
        return schedJobService.addJob(job);
    }

    @PutMapping("job/upd")
    public Result<Void> updJob(SchedJob job) {
        job.setUpdateBy("username");
        return schedJobService.updJob(job);
    }

    @GetMapping("job/{id}")
    public Result<SchedJob> getJob(@PathVariable("id") long jobId) {
        return schedJobService.getJob(jobId);
    }

    @DeleteMapping("job/{id}")
    public Result<Void> delJob(@PathVariable("id")      long jobId, 
                               @RequestParam("version") int version) {
        return schedJobService.delJob(jobId, version);
    }

    @PostMapping("job/start")
    public Result<Void> startJob(@RequestParam("jobId")   long jobId,
                                 @RequestParam("version") int version) {
        return schedJobService.startJob(jobId, version);
    }

    @PostMapping("job/stop")
    public Result<Void> stopJob(@RequestParam("jobId")   long jobId,
                                @RequestParam("version") int version) {
        return schedJobService.stopJob(jobId, version);
    }

    @PostMapping("job/trigger")
    public Result<Void> triggerJob(@RequestParam("jobId") long jobId) {
        return schedJobService.triggerJob(jobId);
    }

    @GetMapping("job/page")
    public Result<Page<SchedJob>> queryJobsForPage(PageRequestParams params) {
        return schedJobService.queryJobsForPage(params.params());
    }

    @GetMapping("log/page")
    public Result<Page<SchedLog>> queryLogsForPage(PageRequestParams params) {
        return schedJobService.queryLogsForPage(params.params());
    }

    // -------------------------------------------------------quartz sched job
    @GetMapping("job/quartz/all")
    public Result<List<SchedJob>> listQuartzAllJobs() {
        return schedJobService.listQuartzAllJobs();
    }

    @GetMapping("job/quartz/running")
    public Result<List<SchedJob>> listQuartzRunningJobs() {
        return schedJobService.listQuartzRunningJobs();
    }

}
