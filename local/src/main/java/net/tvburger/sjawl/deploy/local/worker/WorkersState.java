package net.tvburger.sjawl.deploy.local.worker;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.worker.WorkerDeployment;
import net.tvburger.sjawl.deploy.worker.WorkerDeploymentStrategy;

import java.util.List;
import java.util.Map;

public interface WorkersState {

    Object getLock();

    Map<Class<?>, WorkerDeploymentStrategy<?>> getWorkerTypes();

    Map<Class<?>, List<WorkerDeployment<?>>> getWorkers();

    default <T> void assertRegistered(Class<T> workerTypeClass) throws DeployException {
        if (!getWorkerTypes().containsKey(workerTypeClass)) {
            throw new DeployException("Not registered workerType: " + workerTypeClass.getName());
        }
    }

}
