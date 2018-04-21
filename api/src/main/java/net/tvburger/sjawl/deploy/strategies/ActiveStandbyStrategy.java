package net.tvburger.sjawl.deploy.strategies;

import net.tvburger.sjawl.deploy.worker.WorkerDeployment;
import net.tvburger.sjawl.deploy.worker.WorkerDeploymentStrategy;

import java.util.Collection;
import java.util.Collections;

public final class ActiveStandbyStrategy<T> implements WorkerDeploymentStrategy<T> {

    @Override
    public Collection<WorkerDeployment<T>> defineActiveWorkers(Class<T> workerTypeClass, Collection<WorkerDeployment<T>> workerDeployments) {
        return workerDeployments.isEmpty() ? Collections.emptySet() : Collections.singleton(workerDeployments.iterator().next());
    }

}
