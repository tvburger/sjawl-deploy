package net.tvburger.sjawl.deploy.worker;

import net.tvburger.sjawl.deploy.DeployException;

public interface WorkerActivator<T> {

    boolean isActive(T workerInstance) throws DeployException;

    void activateWorker(T workerInstance) throws DeployException;

    void deactivateWorker(T workerInstance) throws DeployException;

}
