package net.tvburger.sjawl.deploy.worker;

public final class WorkerDeployment<T> {

    private final T workerInstance;
    private final WorkerActivator<T> workerActivator;

    public WorkerDeployment(T workerInstance, WorkerActivator<T> workerActivator) {
        this.workerInstance = workerInstance;
        this.workerActivator = workerActivator;
    }

    public T getWorkerInstance() {
        return workerInstance;
    }

    public WorkerActivator<T> getWorkerActivator() {
        return workerActivator;
    }

}
