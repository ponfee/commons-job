package code.ponfee.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.ImportResource;

/**
 * SpringBoot启动类
 *
 * @author Ponfee
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    RedisAutoConfiguration.class
})
@ComponentScans({ // If not set, default current package and posterity
    @ComponentScan("code.ponfee.job")
})
@ImportResource("classpath:spring/spring-bean.xml")
@EnableCaching
public class SimplyJobStartup {

    public static void main(String[] args) {
        SpringApplication.run(SimplyJobStartup.class, args);
    }

}
