package com.atbm.locks.lockstest.service;

import org.springframework.validation.annotation.Validated;
import sun.nio.ch.ThreadPool;

import java.util.UUID;
import java.util.concurrent.*;

/*
* 异步线程
*
* */

//以后异步任务的编程模式
//ExecutorService threadPool = Executors.newFixedThreadPool(10);
//CompletableFuture.supplyAsync(()->{return 10;},threadPool).whenComplete((r,e)->{});


public class ThreadTest {

    /*public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("主线程....");
        //异步化
//        new Thread(new Thread1()).start();
//
//        new Thread(new Thread2()).start();

        FutureTask<String> task = new FutureTask<>(new Thread3());
        new Thread(task).start();
        //获取异步运行结果
        String s = task.get();
        System.out.println("结果是"+s);

        System.out.println("主线程结束....");
    }*/

/*    public static void main(String[] args) {
        //创建一个同时运行两个线程的线程池-->防止系统资源耗尽
        ExecutorService threadPool = Executors.newFixedThreadPool(2);

        System.out.println("线程池任务准备...");
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> {
                System.out.println("Thread1-当前线程开始" + Thread.currentThread());
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                }
                System.out.println("Thread1-当前线程结束" + Thread.currentThread());
            });
            //给线程池提交任务
            threadPool.submit(thread);
        }
        System.out.println("所有任务都提交...");
    }

}*/

    /*
    * 任务交给线程池 出现异常无法感知
    * */
/*    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(10);
        System.out.println("当前主线程====");
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程开始:" + Thread.currentThread());
            String s = UUID.randomUUID().toString();//int a=10/0;
            System.out.println("当前线程结束");
            return s;
        }, pool)
                .thenApply((r)->{//接收上次结果在加工
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("上一步结果是"+r);
                    return r.replace("-","");
                })
                .whenComplete((r,e)->{//接受结果和执行时的异常
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e1) {
                        e.printStackTrace();
                    }
                    System.out.println("方法完成后执行的结果是:"+r);
                    System.out.println("方法完成后执行的异常是:"+e);
                });
        System.out.println("当前主线程====" + future.get());
    }*/


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品基本数据...");
            return "小米";
        }, threadPool)
                .whenComplete((r,e)->{
                    System.out.println("方法完成后执行的结果是:"+r);
                    System.out.println("方法完成后执行的异常是:"+e);
                });
        CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品属性数据...");
            return "黑色";
        }, threadPool)
                .whenComplete((r,e)->{
                    System.out.println("方法完成后执行的结果是:"+r);
                    System.out.println("方法完成后执行的异常是:"+e);
                });
        CompletableFuture<Integer> f3 = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品营销数据...");
            return 20;
        }, threadPool)
                .whenComplete((r,e)->{
                    System.out.println("方法完成后执行的结果是:"+r);
                    System.out.println("方法完成后执行的异常是:"+e);
                });
        //所有人都执行完
        CompletableFuture<Void> allof = CompletableFuture.allOf(f1, f2, f3);
//        Void aVoid = allof.get();//阻塞作用等待完成
        allof.join();//等待所有人完成
        System.out.println("所有人都完成");

        //以后异步任务的编程模式
        //ExecutorService threadPool = Executors.newFixedThreadPool(10);
        //CompletableFuture.supplyAsync(()->{return 10;},threadPool).whenComplete((r,e)->{});

        //一个人执行完就行
        //CompletableFuture.anyOf(f1,f2,f3);
    }


    class Thread1 extends Thread {
        @Override
        public void run() {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Thread1-当前线程" + Thread.currentThread());
        }
    }

    class Thread2 implements Runnable {

        @Override
        public void run() {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Thread2-当前线程" + Thread.currentThread());
        }
    }

    class Thread3 implements Callable<String> {

        @Override
        public String call() throws Exception {
            try {
                System.out.println("Thread3-开始运行");
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Thread3-运行结束");
            return "ok";
        }
    }


}