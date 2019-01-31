package code.ponfee.job.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.data.redis.core.RedisTemplate;

import com.google.common.base.Stopwatch;
import com.google.common.io.Files;

import code.ponfee.commons.io.WrappedBufferedReader;
import code.ponfee.commons.jedis.spring.RedisBloomFilter;
import code.ponfee.commons.jedis.spring.SpringRedisLock;
import code.ponfee.commons.util.MavenProjects;
import code.ponfee.commons.util.ObjectUtils;
import code.ponfee.job.BaseTest;

public class RedisTemplateTest extends BaseTest<RedisTemplate<String, byte[]>> {

    private static final String NAME = ObjectUtils.uuid32().substring(0, 3);

    @Test
    public void contextLoads() {
    }

    @Test
    public void deleteAll() throws IOException, InterruptedException {
        getBean().delete(getBean().keys("*"));
        System.out.println(getBean().keys("*"));
    }

    @Test
    public void test1() throws IOException, InterruptedException {
        WrappedBufferedReader reader = new WrappedBufferedReader(MavenProjects.getTestJavaFile(this.getClass()));
        final Printer printer = new Printer(new SpringRedisLock(getBean(), "testLock1", 5));
        final AtomicInteger num = new AtomicInteger(0);
        String line = null;
        List<Thread> threads = new ArrayList<>();
        System.out.println("\n=========================START========================");
        while ((line = reader.readLine()) != null) {
            final String _line = line;
            threads.add(new Thread(() -> {
                printer.output(NAME + "-" + num.getAndIncrement() + "\t" + _line + "\n");
            }));
        }
        reader.close();
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("=========================END========================\n");
    }

    @Test
    public void test2() throws IOException, InterruptedException {
        WrappedBufferedReader reader = new WrappedBufferedReader(MavenProjects.getTestJavaFile(this.getClass()));
        final Lock lock = new SpringRedisLock(getBean(), "testLock2", 5);
        final AtomicInteger num = new AtomicInteger(0);
        String line = null;
        List<Thread> threads = new ArrayList<>();
        System.out.println("\n=========================START========================");
        while ((line = reader.readLine()) != null) {
            final String _line = line;
            threads.add(new Thread(() -> {
                new Printer(lock).output(NAME + "-" + num.getAndIncrement() + "\t" + _line + "\n");
            }));
        }
        reader.close();
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("=========================END========================\n");
    }

    @Test
    public void test3() throws IOException, InterruptedException {
        WrappedBufferedReader reader = new WrappedBufferedReader(MavenProjects.getTestJavaFile(this.getClass()));
        final AtomicInteger num = new AtomicInteger(0);
        String line = null;
        List<Thread> threads = new ArrayList<>();
        System.out.println("\n=========================START========================");
        while ((line = reader.readLine()) != null) {
            final String _line = line;
            threads.add(new Thread(() -> {
                new Printer(new SpringRedisLock(getBean(), "testLock3", 5)).output(NAME + "-" + num.getAndIncrement() + "\t" + _line + "\n");
            }));
        }
        reader.close();
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("=========================END========================\n");
    }

    @Test
    public void test4() throws IOException, InterruptedException {
        Printer printer = new Printer(new SpringRedisLock(getBean(), "testLock4", 5));
        AtomicInteger num = new AtomicInteger(0);
        System.out.println("\n=========================START========================");
        Files.readLines(
                MavenProjects.getTestJavaFile(this.getClass()), StandardCharsets.UTF_8
        ).stream().collect(
                Collectors.toMap(line -> (Integer) num.getAndIncrement(), Function.identity())
        ).entrySet().stream().map(
                line -> CompletableFuture.runAsync(
                        () -> printer.output(NAME + "-" + line.getKey() + "\t" + line.getValue() + "\n")
                )
        ).collect(Collectors.toList()).stream().forEach(CompletableFuture::join);
        System.out.println("=========================END========================\n");
    }

    private static class Printer {
        private final Lock lock;

        Printer(Lock lock) {
            this.lock = lock;
        }

        private void output(final String name) {
            lock.lock();
            try {
                for (int i = 0; i < name.length(); i++) {
                    System.out.print(name.charAt(i));
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

    @Test
    public void test10() {
        System.out.println(getBean().getExpire("123456"));
        System.out.println(getBean().getExpire("CRAWLER_BLOOMFILTER"));
    }

    @Test
    public void test11() {
        RedisBloomFilter bloomFilter = new RedisBloomFilter(getBean(), 0.0001, 600000);
        System.out.println(bloomFilter.getSizeOfBloomFilter());
        System.out.println(bloomFilter.getNumberOfHashFunctions());

        byte[] element = "123".getBytes();

        Stopwatch watch = Stopwatch.createStarted();
        System.out.println(bloomFilter.contains(element));
        //System.out.println(watch.stop().toString());

        watch.reset().start();
        bloomFilter.add(element);
        //System.out.println(watch.stop().toString());

        watch.reset().start();
        System.out.println(bloomFilter.contains(element));
        //System.out.println(watch.stop().toString());
    }
}
