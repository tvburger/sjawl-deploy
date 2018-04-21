package net.tvburger.sjawl.deploy.local;

import net.tvburger.sjawl.deploy.DeploymentContext;
import net.tvburger.sjawl.deploy.service.ServiceProvider;
import net.tvburger.sjawl.deploy.service.ServiceRegistry;
import net.tvburger.sjawl.deploy.service.ServicesAdministrator;
import net.tvburger.sjawl.deploy.worker.WorkerDeployer;
import net.tvburger.sjawl.deploy.worker.WorkersAdministrator;

import java.util.UUID;

public class LocalDeploymentContext implements DeploymentContext {

    public static final class Factory {

        public static LocalDeploymentContext create() {
            return new LocalDeploymentContext(
                    System.getProperty("deploymentId", UUID.randomUUID().toString()),
                    new LocalServicesManager(),
                    new LocalWorkersManager());
        }

        public Factory() {
        }

    }

    private final String deploymentId;
    private final LocalServicesManager localServicesManager;
    private final LocalWorkersManager workerManager;

    protected LocalDeploymentContext(String deploymentId, LocalServicesManager localServicesManager, LocalWorkersManager workerManager) {
        this.deploymentId = deploymentId;
        this.localServicesManager = localServicesManager;
        this.workerManager = workerManager;
    }

    @Override
    public String getDeploymentId() {
        return deploymentId;
    }

    @Override
    public ServiceProvider getServiceProvider() {
        return localServicesManager;
    }

    @Override
    public ServiceRegistry getServiceRegistry() {
        return localServicesManager;
    }

    @Override
    public ServicesAdministrator getServicesAdministrator() {
        return localServicesManager;
    }

    @Override
    public WorkerDeployer getWorkerDeployer() {
        return workerManager;
    }

    @Override
    public WorkersAdministrator getWorkerAdministrator() {
        return workerManager;
    }

}
