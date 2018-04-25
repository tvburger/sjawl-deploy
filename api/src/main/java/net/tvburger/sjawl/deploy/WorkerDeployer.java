package net.tvburger.sjawl.deploy;

public interface WorkerDeployer {

    interface WorkerFinishedCallback {

        void workerFinished() throws DeployException;

    }

    <T> boolean isDeployed(Class<T> workerTypeClass, T workerInstance) throws DeployException;

    <T> void deployWorker(Class<T> workerTypeClass, T workerInstance, WorkerActivator<T> workerActivator) throws DeployException;

    <T> void undeployWorker(Class<T> workerTypeClass, T workerInstance) throws DeployException;

}
