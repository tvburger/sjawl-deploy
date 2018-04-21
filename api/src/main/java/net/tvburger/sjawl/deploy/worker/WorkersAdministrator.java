package net.tvburger.sjawl.deploy.worker;

import net.tvburger.sjawl.deploy.DeployException;

import java.util.Collection;

public interface WorkersAdministrator {

    Collection<Class<?>> getRegisteredWorkerTypes() throws DeployException;

    <T> WorkerDeploymentStrategy<T> getWorkerDeploymentStrategy(Class<T> workerTypeClass) throws DeployException;

    <T> Collection<WorkerDeployment<T>> getWorkerDeployments(Class<T> workerTypeClass) throws DeployException;

    <T> boolean isRegisteredWorkerType(Class<T> workerTypeClass) throws DeployException;

    <T> void registerWorkerType(Class<T> workerTypeClass, WorkerDeploymentStrategy<T> deploymentStrategy) throws DeployException;

    <T> void unregisterWorkerType(Class<T> workerTypeClass) throws DeployException;

}
