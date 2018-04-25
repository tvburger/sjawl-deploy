package net.tvburger.sjawl.deploy.local;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.WorkerDeploymentStrategy;
import net.tvburger.sjawl.deploy.admin.WorkerDeployment;

import java.util.List;
import java.util.Map;

public interface LocalWorkersStore {

    Object getLock();

    Map<Class<?>, WorkerDeploymentStrategy<?>> getWorkerTypes();

    Map<Class<?>, List<WorkerDeployment<?>>> getWorkers();

    default <T> void assertRegistered(Class<T> workerTypeClass) throws DeployException {
        if (!getWorkerTypes().containsKey(workerTypeClass)) {
            throw new DeployException("Not registered workerType: " + workerTypeClass.getName());
        }
    }

}
