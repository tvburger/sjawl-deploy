package net.tvburger.sjawl.deploy.local.worker;

import net.tvburger.sjawl.common.AssertUtil;
import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.worker.WorkerActivator;
import net.tvburger.sjawl.deploy.worker.WorkerDeployer;
import net.tvburger.sjawl.deploy.worker.WorkerDeployment;
import net.tvburger.sjawl.deploy.worker.WorkerDeploymentStrategy;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unchecked")
public final class DefaultWorkerDeployer implements WorkerDeployer {

    private class Callback<T> implements WorkerFinishedCallback {

        private final Class<T> workerTypeClass;
        private final T workerInstance;

        public Callback(Class<T> workerTypeClass, T workerInstance) {
            this.workerTypeClass = workerTypeClass;
            this.workerInstance = workerInstance;
        }

        @Override
        public void workerFinished() throws DeployException {
            synchronized (state.getLock()) {
                undeployWorker(workerTypeClass, workerInstance, false);
            }
        }

    }

    private final WorkersState state;

    public DefaultWorkerDeployer(WorkersState state) {
        this.state = state;
    }

    @Override
    public <T> boolean isDeployed(Class<T> workerTypeClass, T workerInstance) throws DeployException {
        AssertUtil.assertNotNull(workerTypeClass);
        AssertUtil.assertNotNull(workerInstance);
        synchronized (state.getLock()) {
            state.assertRegistered(workerTypeClass);
            for (WorkerDeployment<?> workerDeployment : state.getWorkers().get(workerTypeClass)) {
                if (workerDeployment.getWorkerInstance().equals(workerInstance)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public <T> void deployWorker(Class<T> workerTypeClass, T workerInstance, WorkerActivator<T> workerActivator) throws DeployException {
        AssertUtil.assertNotNull(workerTypeClass);
        AssertUtil.assertNotNull(workerInstance);
        AssertUtil.assertNotNull(workerActivator);
        synchronized (state.getLock()) {
            state.assertRegistered(workerTypeClass);
            List<WorkerDeployment> workerDeployments = (List) state.getWorkers().get(workerTypeClass);
            workerDeployments.add(new WorkerDeployment<>(workerInstance, workerActivator));
            activateWorkers(workerTypeClass);
        }
    }

    @Override
    public <T> void undeployWorker(Class<T> workerTypeClass, T workerInstance) throws DeployException {
        AssertUtil.assertNotNull(workerTypeClass);
        AssertUtil.assertNotNull(workerInstance);
        synchronized (state.getLock()) {
            state.assertRegistered(workerTypeClass);
            undeployWorker(workerTypeClass, workerInstance, true);
        }
    }

    private <T> void undeployWorker(Class<T> workerTypeClass, T workerInstance, boolean mustBePresent) throws DeployException {
        if (!state.getWorkerTypes().containsKey(workerTypeClass)) {
            return;
        }
        Iterator<WorkerDeployment> iterator = (Iterator) state.getWorkers().get(workerTypeClass).iterator();
        while (iterator.hasNext()) {
            WorkerDeployment<T> workerDeployment = iterator.next();
            T worker = workerDeployment.getWorkerInstance();
            if (worker == workerInstance) {
                if (workerDeployment.getWorkerActivator().isActive(worker)) {
                    workerDeployment.getWorkerActivator().deactivateWorker(worker);
                }
                iterator.remove();
                activateWorkers(workerTypeClass);
                return;
            }
        }
        if (mustBePresent) {
            throw new DeployException("No such worker is deployed!");
        }
    }

    private <T> void activateWorkers(Class<T> workerTypeClass) throws DeployException {
        Collection<WorkerDeployment<T>> allWorkerDeployments = (List) state.getWorkers().get(workerTypeClass);
        WorkerDeploymentStrategy<T> deploymentStrategy = (WorkerDeploymentStrategy) state.getWorkerTypes().get(workerTypeClass);
        Collection<WorkerDeployment<T>> activeDeployments = deploymentStrategy.defineActiveWorkers(workerTypeClass, allWorkerDeployments);
        for (WorkerDeployment<T> workerDeployment : allWorkerDeployments) {
            ensureActiveState(workerTypeClass, workerDeployment, activeDeployments.contains(workerDeployment));
        }
    }

    private <T> void ensureActiveState(Class<T> workerTypeClass, WorkerDeployment<T> workerDeployment, boolean mustBeActive) throws DeployException {
        T workerInstance = workerDeployment.getWorkerInstance();
        WorkerActivator<T> activator = workerDeployment.getWorkerActivator();
        if (activator.isActive(workerInstance) != mustBeActive) {
            if (mustBeActive) {
                activator.activateWorker(workerInstance, new Callback<>(workerTypeClass, workerInstance));
            } else {
                activator.deactivateWorker(workerInstance);
            }
        }
    }

}
