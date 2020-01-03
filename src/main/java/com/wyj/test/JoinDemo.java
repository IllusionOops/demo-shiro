package com.wyj.test;

/**
 * @ClassName JoinDemo
 * @Description: TODO
 * @Author yjwu
 * @Date 2020/1/3 15:58
 **/
public class JoinDemo {

    public static void main(String[] args) {
        Thread previousThread=Thread.currentThread();

        for (int i=0;i<10;i++){
            JoinThread joinThread = new JoinThread(previousThread);
            joinThread.start();
            previousThread=joinThread;
        }
    }

    static class JoinThread extends Thread{
        private Thread thread;

        public JoinThread(Thread thread) {
            this.thread = thread;
        }

        @Override
        public void run() {
            try {
                thread.join();
                System.out.println(Thread.currentThread().getName()+"-----terminated ");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
