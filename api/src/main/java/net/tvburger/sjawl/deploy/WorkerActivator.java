package net.tvburger.sjawl.deploy;

public interface WorkerActivator<T> {

    boolean isActive(T workerInstance) throws DeployException;

    void activateWorker(T workerInstance, WorkerDeployer.WorkerFinishedCallback callback) throws DeployException;

    void deactivateWorker(T workerInstance) throws DeployException;

}
