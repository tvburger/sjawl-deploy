package net.tvburger.sjawl.deploy.strategies;

import net.tvburger.sjawl.deploy.worker.WorkerDeployment;
import net.tvburger.sjawl.deploy.worker.WorkerDeploymentStrategy;

import java.util.Collection;

public final class ActiveActiveStrategy<T> implements WorkerDeploymentStrategy<T> {

    @Override
    public Collection<WorkerDeployment<T>> defineActiveWorkers(Class<T> workerTypeClass, Collection<WorkerDeployment<T>> workerDeployments) {
        return workerDeployments;
    }

}
