package net.tvburger.sjawl.deploy.worker;

import net.tvburger.sjawl.deploy.DeployException;

public interface WorkerDeployer {

    <T> boolean isDeployed(Class<T> workerTypeClass, T workerInstance) throws DeployException;

    <T> void deployWorker(Class<T> workerTypeClass, T workerInstance, WorkerActivator<T> workerActivator) throws DeployException;

    <T> void undeployWorker(Class<T> workerTypeClass, T workerInstance) throws DeployException;

}
