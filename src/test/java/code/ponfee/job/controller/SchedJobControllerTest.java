package code.ponfee.job.controller;

import java.util.Map;

import code.ponfee.commons.http.Http;
import code.ponfee.commons.json.Jsons;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class SchedJobControllerTest {

    public static final String URL = "http://localhost:8000/";
    //public static final String URL = "http://ddt-market-res-int.sit.sf-express.com/";
    public static final Map<String, String> COOKIE = ImmutableMap.of(
            "Cookie",
            "_TOKEN_KEY_=eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiIwMTM2NzgyNSIsImV4cCI6MTU0NTIxNjEwNH0.8pCYGNPy5CzgdGay0MYhVhlm4eAfQXYE_oHAu4QzRDM"
    );

    @Test(timeout = 999999999)
    public void jobpage() {
        Http http = Http.get(URL + "sched/job/page")
                .addHeader(COOKIE);
        print(http.request());
    }

    @Test(timeout = 999999999)
    public void logpage() {
        Http http = Http.get(URL + "sched/log/page")
                .addHeader(COOKIE);
        print(http.request());
    }

    @Test(timeout = 999999999)
    public void add() {
        Http http = Http.post(URL + "sched/job/add")
                .addParam("triggerType", 1)
                //.addParam("triggerSched", "{\"origin\":\"2000-01-01 00:00:00\", \"step\":1}")
                .addParam("triggerSched", "0/30 * * * * ?")
                .addParam("startTime", "2018-02-28 23:59:58")
                .addParam("name", "测试1")
                .addParam("concurrentSupport", "false")
                .addParam("recoverySupport", "true")
                //.addParam("handler", "code.ponfee.job.handler.TestJobHandler")
                .addParam("handler", "code.ponfee.job.handler.HttpJobHandler")
                .addParam("execParams", "{\"url\":\"http://www.baidu.com\"}")
                .addParam("status", 1)
                //.addParam("endTime", "2018-04-30 00:00:00")
                .addHeader(COOKIE);
        try {

            print(http.request());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test(timeout = 999999999)
    public void get() {
        Http http = Http.get(URL + "sched/job/9")
                .addHeader(COOKIE);
        print(http.request());
    }

    @Test(timeout = 999999999)
    public void upd() {
        Http http = Http.put(URL + "sched/job/upd")
                .addParam("id", 1)
                //.addParam("name", "测试2")
                .addParam("triggerType", 1)
                .addParam("triggerSched", "0/30 * * * * ?")
                .addParam("handler", "code.ponfee.job.handler.JobHandlerTest")
                //.addParam("handler", "code.ponfee.job.handler.DefaultJobHandler")
                .addParam("recoverySupport", "false")
                .addParam("status", 1)
                .addParam("version", 1)
                .addHeader(COOKIE);
        print(http.request());
    }

    @Test(timeout = 999999999)
    public void del() {
        Http http = Http.delete(URL + "sched/job/34")
                .addParam("version", 21)
                .addHeader(COOKIE);
        print(http.request());
        print(Jsons.toJson(http.getRespHeaders()));
    }

    @Test(timeout = 999999999)
    public void start() {
        Http http = Http.post(URL + "sched/job/start")
                .addParam("jobId", 9)
                .addParam("version", 2)
                .addHeader(COOKIE);
        print(http.request());
        print(Jsons.toJson(http.getRespHeaders()));
    }

    @Test(timeout = 999999999)
    public void stop() {
        Http http = Http.post(URL + "sched/job/stop")
                .addParam("jobId", 9)
                .addParam("version", 1)
                .addHeader(COOKIE);
        print(http.request());
        print(Jsons.toJson(http.getRespHeaders()));
    }


    @Test(timeout = 999999999)
    public void triggerJob() {
        Http http = Http.post(URL + "sched/job/trigger")
                .addParam("jobId", 1)
                .addHeader(COOKIE);
        print(http.request());
        print(Jsons.toJson(http.getRespHeaders()));
    }


    @Test(timeout = 999999999)
    public void quartzall() {
        Http http = Http.get(URL + "sched/job/quartz/all")
                .addHeader(COOKIE);
        print(http.request());
        print(Jsons.toJson(http.getRespHeaders()));
    }

    @Test(timeout = 999999999)
    public void quartzrunning() {
        Http http = Http.get(URL + "sched/job/quartz/running")
                .addHeader(COOKIE);
        print(http.request());
        print(Jsons.toJson(http.getRespHeaders()));
    }

    private static void print(Object obj) {
        try {
            Thread.sleep(100);
            System.out.println(obj);
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
