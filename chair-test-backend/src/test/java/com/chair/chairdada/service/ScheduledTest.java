package com.chair.chairdada.service;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;

@SpringBootTest
public class ScheduledTest {

    @Resource
    Scheduler vipScheduler;

    @Resource
    Scheduler userScheduler;

    // 测试并发任务执行时间
    @Test
    public void testConcurrency() throws InterruptedException {
        int taskCount = 100;
        CountDownLatch latch = new CountDownLatch(taskCount);

        // 提交任务到 VIP 线程池
        long startVip = System.currentTimeMillis();
        for (int i = 0; i < taskCount; i++) {
            vipScheduler.scheduleDirect(() -> {
                try {
                    Thread.sleep(100);
                } catch (Exception ignored) {
                }
                System.out.println("VIP Task on: " + Thread.currentThread().getName());
                latch.countDown();
            });
        }
//        latch.await();
        long vipTime = System.currentTimeMillis() - startVip;

        // 同样逻辑测试 USER 线程池（重置 latch）
        // ... 代码类似，替换为 userScheduler
        // 提交任务到 VIP 线程池
        long startUser = System.currentTimeMillis();
        for (int i = 0; i < taskCount; i++) {
            userScheduler.scheduleDirect(() -> {
                try {
                    Thread.sleep(100);
                } catch (Exception ignored) {
                }
                System.out.println("USER Task on: " + Thread.currentThread().getName());
                latch.countDown();
            });
        }
//        latch.await();
        long userTime = System.currentTimeMillis() - startUser;

        System.out.println("VIP Time: " + vipTime + "ms | USER Time: " + userTime + "ms");
    }


    @Test
    public void testSchedule() {
        Scheduler io = Schedulers.io();
        while (true) {
            io.scheduleDirect(() -> {
                System.out.println(Thread.currentThread().getName() + " print hello");
                try {
                    Thread.sleep(50000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}