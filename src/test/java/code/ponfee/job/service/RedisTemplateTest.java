package code.ponfee.job.service;

import java.io.IOException;

import org.junit.Test;
import org.springframework.data.redis.core.RedisTemplate;

import com.google.common.base.Stopwatch;

import code.ponfee.commons.jedis.spring.RedisBloomFilter;
import code.ponfee.job.BaseTest;

public class RedisTemplateTest extends BaseTest<RedisTemplate<String, byte[]>> {

    @Test
    public void contextLoads() {
    }

    @Test
    public void deleteAll() throws IOException, InterruptedException {
        getBean().delete(getBean().keys("*"));
        System.out.println(getBean().keys("*"));
    }


    @Test
    public void test1() {
        System.out.println(getBean().getExpire("123456"));
        System.out.println(getBean().getExpire("CRAWLER_BLOOMFILTER"));
    }

    @Test
    public void test2() {
        String key = "key";
        RedisBloomFilter bloomFilter = new RedisBloomFilter(key, getBean(), 0.0001, 600000);
        System.out.println(bloomFilter.getSizeOfBloomFilter());
        System.out.println(bloomFilter.getNumberOfHashFunctions());

        byte[] element = "123".getBytes();

        Stopwatch watch = Stopwatch.createStarted();
        System.out.println(bloomFilter.mightContain(element));
        //System.out.println(watch.stop().toString());

        watch.reset().start();
        bloomFilter.put(element);
        //System.out.println(watch.stop().toString());

        watch.reset().start();
        System.out.println(bloomFilter.mightContain(element));
        //System.out.println(watch.stop().toString());
    }
}
