package com.wyj.test;


/**
 * @ClassName InterruptDemo
 * @Description: TODO
 * @Author yjwu
 * @Date 2020/1/3 15:34
 **/
public class InterruptDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread sleepThread=new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    System.out.println("sleep结束");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread busyThred=new Thread(){
            @Override
            public void run() {
                    while(true){

                }
            }
        };

        sleepThread.start();
        busyThred.start();
        System.out.println(sleepThread.isInterrupted());
        System.out.println(busyThred.isInterrupted());
        sleepThread.interrupt();
        busyThred.interrupt();
        Thread.sleep(5000);
        System.out.println(sleepThread.isInterrupted());
        System.out.println(busyThred.isInterrupted());
    }


}
