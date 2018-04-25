package net.tvburger.sjawl.deploy.local.impl;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.WorkerDeploymentStrategy;
import net.tvburger.sjawl.deploy.admin.WorkerDeployment;
import net.tvburger.sjawl.deploy.local.LocalWorkersStore;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DefaultLocalWorkersStore implements LocalWorkersStore {

    private final Object lock = new Object();
    private final Map<Class<?>, WorkerDeploymentStrategy<?>> workerTypes = new LinkedHashMap<>();
    private final Map<Class<?>, List<WorkerDeployment<?>>> workers = new LinkedHashMap<>();

    public Object getLock() {
        return lock;
    }

    public Map<Class<?>, WorkerDeploymentStrategy<?>> getWorkerTypes() {
        return workerTypes;
    }

    public Map<Class<?>, List<WorkerDeployment<?>>> getWorkers() {
        return workers;
    }

    public <T> void assertRegistered(Class<T> workerTypeClass) throws DeployException {
        if (!workerTypes.containsKey(workerTypeClass)) {
            throw new DeployException("Not registered workerType: " + workerTypeClass.getName());
        }
    }

}
