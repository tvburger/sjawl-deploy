package net.tvburger.sjawl.deploy.example;

import net.tvburger.sjawl.deploy.utils.ManagedWorker;

public class TestWorker extends ManagedWorker {

    private final String message;

    public TestWorker(String message) {
        this.message = message;
    }

    @Override
    public void started() {
        System.out.println("Service started: " + message);
    }

    @Override
    public void stopped() {
        System.out.println("Service stopped: " + message);
    }

    @Override
    protected void performOneWorkUnit() throws InterruptedException {
        System.out.println(message);
        Thread.sleep(50);
    }

}
