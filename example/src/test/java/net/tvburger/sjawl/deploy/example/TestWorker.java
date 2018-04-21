package net.tvburger.sjawl.deploy.example;

import net.tvburger.sjawl.deploy.worker.WorkerActivator;

public class TestWorker implements Runnable, WorkerActivator<TestWorker> {

    private final Object lock = new Object();
    private volatile boolean isActive;

    private final String message;

    public TestWorker(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        System.out.println("Worker begins: " + this);
        while (isActive) {
            try {
                System.out.println(message);
                synchronized (lock) {
                    lock.wait(100);
                }
            } catch (InterruptedException cause) {
            }
        }
        System.out.println("Worker ends: " + this);
    }

    @Override
    public boolean isActive(TestWorker service) {
        return isActive;
    }

    @Override
    public void activateWorker(TestWorker service) {
        isActive = true;
        new Thread(this).start();
    }

    @Override
    public void deactivateWorker(TestWorker service) {
        isActive = false;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

}
