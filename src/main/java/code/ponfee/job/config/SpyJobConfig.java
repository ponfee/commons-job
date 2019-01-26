// package code.ponfee.job.config;
//
// import code.ponfee.commons.util.SpringContextHolder;
// import code.ponfee.job.dao.ISchedJobDao;
// import code.ponfee.job.dao.cache.SchedJobCached;
// import code.ponfee.job.service.ISchedJobService;
// import code.ponfee.job.service.impl.SchedJobServiceImpl;
// import code.ponfee.job.spy.SpyJobHeartbeat;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Qualifier;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.DependsOn;
// import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
// /**
//  * 事务配置
//  *
//  * @author Ponfee
//  */
// @Configuration
// public class SpyJobConfig {
//
//     @Autowired
//     private ISchedJobDao schedJobDao;
//
//     @Autowired
//     private SchedJobCached schedJobCached;
//
//     @Bean("schedJobService")
//     public ISchedJobService schedJobService() {
//         return new SchedJobServiceImpl();
//     }
//
//     @Bean("taskExecutor")
//     public ThreadPoolTaskExecutor taskExecutor() {
//         ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
//         taskExecutor.setCorePoolSize(5);//线程池大小
//         taskExecutor.setMaxPoolSize(10);//线程池最大线程数
//         taskExecutor.setQueueCapacity(25);//最大等待任务数
//         taskExecutor.initialize();
//         return taskExecutor;
//     }
//
//     @Bean(name = "spyJobHeartbeat", initMethod = "startup", destroyMethod = "shutdown")
//     @DependsOn("taskExecutor")
//     public SpyJobHeartbeat spyJobHeartbeat(
//             @Qualifier("taskExecutor") ThreadPoolTaskExecutor taskExecutor) {
//         return new SpyJobHeartbeat(schedJobDao, schedJobCached, taskExecutor);
//     }
//
//     @Bean
//     public SpringContextHolder springContextHolder() {
//         return new SpringContextHolder();
//     }
// }
