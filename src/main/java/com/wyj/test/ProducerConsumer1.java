package com.wyj.test;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @ClassName ProducerConsumer1
 * @Description: TODO
 * @Author yjwu
 * @Date 2020/1/3 13:29
 **/
public class ProducerConsumer1 {
    public static void main(String[] args) {
        LinkedList<Integer> linkedList=new LinkedList<>();
        ExecutorService executorService= Executors.newFixedThreadPool(8);
        for (int i=0;i<4;i++){
            executorService.submit(new Producer(linkedList,8));
        }
//        for (int i=0;i<4;i++){
//            executorService.submit(new Consumer(linkedList));
//        }
    }


    static class Producer implements Runnable {
        private List<Integer> list;
        private int maxLength;


        public Producer(List<Integer> list, int maxLength) {
            this.list = list;
            this.maxLength = maxLength;
        }

        @Override
        public void run() {
            while (true) {
                synchronized (list) {
                    try {
                        while (list.size() == maxLength) {
                            System.out.println("生产者：" + Thread.currentThread().getName() + " list 已经达到最大容量，开始wait");
                            list.wait();
                            System.out.println("生产者：" + Thread.currentThread().getName() + " 退出 wait");
                        }
                        Random random=new Random();
                        int i=random.nextInt();
                        list.add(i);
                        list.notifyAll();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }


    static class Consumer implements Runnable{
        private List<Integer> list;

        public Consumer(List<Integer> list) {
            this.list = list;
        }

        @Override
        public void run() {
            while (true){
                synchronized (list){
                    try {
                        while (list.isEmpty()){
                            System.out.println("消费者："+Thread.currentThread().getName()+" list为空，进行wait");
                            list.wait();
                            System.out.println("消费者："+Thread.currentThread().getName()+" 退出 wait");
                        }
                        Integer remove = list.remove(0);
                        System.out.println("消费者："+Thread.currentThread().getName()+"消费了数据"+remove);
                        list.notifyAll();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}

