package net.tvburger.sjawl.deploy.worker;

import net.tvburger.sjawl.deploy.DeployException;

import java.util.Collection;

public interface WorkerDeploymentStrategy<T> {

    Collection<WorkerDeployment<T>> defineActiveWorkers(Class<T> workerTypeClass, Collection<WorkerDeployment<T>> workerDeployments) throws DeployException;

}
