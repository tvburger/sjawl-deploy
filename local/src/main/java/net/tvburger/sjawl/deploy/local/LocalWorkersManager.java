package net.tvburger.sjawl.deploy.local;

import net.tvburger.sjawl.common.AssertUtil;
import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.worker.*;

import java.util.*;

@SuppressWarnings("unchecked")
public final class LocalWorkersManager implements WorkerDeployer, WorkersAdministrator {

    private final Object lock = new Object();
    private final Map<Class, WorkerDeploymentStrategy> workerTypes = new LinkedHashMap<>();
    private final Map<Class, List<WorkerDeployment>> workers = new LinkedHashMap<>();

    @Override
    public <T> boolean isDeployed(Class<T> workerTypeClass, T workerInstance) throws DeployException {
        AssertUtil.assertNotNull(workerTypeClass);
        AssertUtil.assertNotNull(workerInstance);
        synchronized (lock) {
            assertRegistered(workerTypeClass);
            for (WorkerDeployment<T> workerDeployment : workers.get(workerTypeClass)) {
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
        synchronized (lock) {
            assertRegistered(workerTypeClass);
            List<WorkerDeployment> workerDeployments = workers.get(workerTypeClass);
            workerDeployments.add(new WorkerDeployment<>(workerInstance, workerActivator));
            activateWorkers(workerTypeClass);
        }
    }

    @Override
    public <T> void undeployWorker(Class<T> workerTypeClass, T workerInstance) throws DeployException {
        AssertUtil.assertNotNull(workerTypeClass);
        AssertUtil.assertNotNull(workerInstance);
        synchronized (lock) {
            assertRegistered(workerTypeClass);
            Iterator<WorkerDeployment> iterator = workers.get(workerTypeClass).iterator();
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
            throw new DeployException("No such worker is deployed!");
        }
    }

    @Override
    public Collection<Class<?>> getRegisteredWorkerTypes() {
        synchronized (lock) {
            return new LinkedHashSet(workerTypes.keySet());
        }
    }

    @Override
    public <T> WorkerDeploymentStrategy<T> getWorkerDeploymentStrategy(Class<T> workerTypeClass) throws DeployException {
        AssertUtil.assertNotNull(workerTypeClass);
        synchronized (lock) {
            assertRegistered(workerTypeClass);
            return workerTypes.get(workerTypeClass);
        }
    }

    @Override
    public <T> Collection<WorkerDeployment<T>> getWorkerDeployments(Class<T> workerTypeClass) throws DeployException {
        AssertUtil.assertNotNull(workerTypeClass);
        synchronized (lock) {
            assertRegistered(workerTypeClass);
            return new LinkedList(workers.get(workerTypeClass));
        }
    }

    @Override
    public <T> boolean isRegisteredWorkerType(Class<T> workerTypeClass) {
        AssertUtil.assertNotNull(workerTypeClass);
        synchronized (lock) {
            return workerTypes.containsKey(workerTypeClass);
        }
    }

    @Override
    public <T> void registerWorkerType(Class<T> workerTypeClass, WorkerDeploymentStrategy<T> deploymentStrategy) throws DeployException {
        AssertUtil.assertNotNull(workerTypeClass);
        AssertUtil.assertNotNull(deploymentStrategy);
        synchronized (lock) {
            if (workerTypes.containsKey(workerTypeClass)) {
                throw new DeployException("Already workerType defined for: " + workerTypeClass.getName());
            }
            workerTypes.put(workerTypeClass, deploymentStrategy);
            workers.put(workerTypeClass, new LinkedList<>());
        }
    }

    @Override
    public <T> void unregisterWorkerType(Class<T> workerTypeClass) throws DeployException {
        AssertUtil.assertNotNull(workerTypeClass);
        synchronized (lock) {
            assertRegistered(workerTypeClass);
            for (WorkerDeployment<T> workerDeployment : workers.get(workerTypeClass)) {
                if (workerDeployment.getWorkerActivator().isActive(workerDeployment.getWorkerInstance())) {
                    workerDeployment.getWorkerActivator().deactivateWorker(workerDeployment.getWorkerInstance());
                }
            }
            workers.remove(workerTypeClass);
            workerTypes.remove(workerTypeClass);
        }
    }

    private <T> void activateWorkers(Class<T> workerTypeClass) throws DeployException {
        Collection<WorkerDeployment<T>> allWorkerDeployments = (List) workers.get(workerTypeClass);
        Collection<WorkerDeployment<T>> activeDeployments = getWorkerDeploymentStrategy(workerTypeClass).defineActiveWorkers(workerTypeClass, allWorkerDeployments);
        for (WorkerDeployment<T> workerDeployment : allWorkerDeployments) {
            ensureActiveState(workerDeployment, activeDeployments.contains(workerDeployment));
        }
    }

    private <T> void ensureActiveState(WorkerDeployment<T> workerDeployment, boolean mustBeActive) throws DeployException {
        T workerInstance = workerDeployment.getWorkerInstance();
        WorkerActivator<T> activator = workerDeployment.getWorkerActivator();
        if (activator.isActive(workerInstance) != mustBeActive) {
            if (mustBeActive) {
                activator.activateWorker(workerInstance);
            } else {
                activator.deactivateWorker(workerInstance);
            }
        }
    }

    private <T> void assertRegistered(Class<T> workerTypeClass) throws DeployException {
        if (!isRegisteredWorkerType(workerTypeClass)) {
            throw new DeployException("Not registered workerType: " + workerTypeClass.getName());
        }
    }

}
