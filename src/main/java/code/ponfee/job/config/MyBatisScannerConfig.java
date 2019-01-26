package code.ponfee.job.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis扫描配置
 * 
 * @author Ponfee
 */
@Configuration
@AutoConfigureAfter(MyBatisConfig.class)
public class MyBatisScannerConfig {

    @Bean
    public static tk.mybatis.spring.mapper.MapperScannerConfigurer tkMapperScannerConfigurer() {
        tk.mybatis.spring.mapper.MapperScannerConfigurer mapperScannerConfigurer =
            new tk.mybatis.spring.mapper.MapperScannerConfigurer();
        mapperScannerConfigurer.setBasePackage("code.ponfee.job.dao.mapper");
        mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
        return mapperScannerConfigurer;
    }

    /*@Bean
    public static org.mybatis.spring.mapper.MapperScannerConfigurer orgMapperScannerConfigurer() {
        org.mybatis.spring.mapper.MapperScannerConfigurer mapperScannerConfigurer =
            new org.mybatis.spring.mapper.MapperScannerConfigurer();
        mapperScannerConfigurer.setBasePackage("code.ponfee.job.dao.mapper");
        mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
        return mapperScannerConfigurer;
    }*/
}
