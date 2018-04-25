package net.tvburger.sjawl.deploy;

import net.tvburger.sjawl.deploy.admin.WorkerDeployment;

import java.util.Collection;

public interface WorkerDeploymentStrategy<T> {

    Collection<WorkerDeployment<T>> defineActiveWorkers(Class<T> workerTypeClass, Collection<WorkerDeployment<T>> workerDeployments) throws DeployException;

}
