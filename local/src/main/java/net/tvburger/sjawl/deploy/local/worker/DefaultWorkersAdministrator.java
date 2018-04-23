package net.tvburger.sjawl.deploy.local.worker;

import net.tvburger.sjawl.common.AssertUtil;
import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.worker.WorkerDeployment;
import net.tvburger.sjawl.deploy.worker.WorkerDeploymentStrategy;
import net.tvburger.sjawl.deploy.worker.WorkersAdministrator;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;

@SuppressWarnings("unchecked")
public final class DefaultWorkersAdministrator implements WorkersAdministrator {

    private final WorkersState state;

    public DefaultWorkersAdministrator(WorkersState state) {
        this.state = state;
    }

    @Override
    public Collection<Class<?>> getRegisteredWorkerTypes() throws DeployException {
        synchronized (state.getLock()) {
            return new LinkedHashSet<>(state.getWorkerTypes().keySet());
        }
    }

    @Override
    public <T> WorkerDeploymentStrategy<T> getWorkerDeploymentStrategy(Class<T> workerTypeClass) throws DeployException {
        AssertUtil.assertNotNull(workerTypeClass);
        synchronized (state.getLock()) {
            state.assertRegistered(workerTypeClass);
            return (WorkerDeploymentStrategy) state.getWorkerTypes().get(workerTypeClass);
        }
    }

    @Override
    public <T> Collection<WorkerDeployment<T>> getWorkerDeployments(Class<T> workerTypeClass) throws DeployException {
        AssertUtil.assertNotNull(workerTypeClass);
        synchronized (state.getLock()) {
            state.assertRegistered(workerTypeClass);
            return new LinkedList(state.getWorkers().get(workerTypeClass));
        }
    }

    @Override
    public <T> boolean isRegisteredWorkerType(Class<T> workerTypeClass) throws DeployException {
        AssertUtil.assertNotNull(workerTypeClass);
        synchronized (state.getLock()) {
            return state.getWorkerTypes().containsKey(workerTypeClass);
        }
    }

    @Override
    public <T> void registerWorkerType(Class<T> workerTypeClass, WorkerDeploymentStrategy<T> deploymentStrategy) throws DeployException {
        AssertUtil.assertNotNull(workerTypeClass);
        AssertUtil.assertNotNull(deploymentStrategy);
        synchronized (state.getLock()) {
            if (state.getWorkerTypes().containsKey(workerTypeClass)) {
                throw new DeployException("Already workerType defined for: " + workerTypeClass.getName());
            }
            state.getWorkerTypes().put(workerTypeClass, deploymentStrategy);
            state.getWorkers().put(workerTypeClass, new LinkedList<>());
        }
    }

    @Override
    public <T> void unregisterWorkerType(Class<T> workerTypeClass) throws DeployException {
        AssertUtil.assertNotNull(workerTypeClass);
        synchronized (state.getLock()) {
            state.assertRegistered(workerTypeClass);
            for (WorkerDeployment workerDeployment : state.getWorkers().get(workerTypeClass)) {
                if (workerDeployment.getWorkerActivator().isActive(workerDeployment.getWorkerInstance())) {
                    workerDeployment.getWorkerActivator().deactivateWorker(workerDeployment.getWorkerInstance());
                }
            }
            state.getWorkers().remove(workerTypeClass);
            state.getWorkerTypes().remove(workerTypeClass);
        }
    }

}
