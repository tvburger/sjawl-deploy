package net.tvburger.sjawl.deploy.local;

import net.tvburger.sjawl.common.AssertUtil;
import net.tvburger.sjawl.deploy.DeploymentContext;
import net.tvburger.sjawl.deploy.local.service.*;
import net.tvburger.sjawl.deploy.local.worker.DefaultWorkerDeployer;
import net.tvburger.sjawl.deploy.local.worker.DefaultWorkersAdministrator;
import net.tvburger.sjawl.deploy.local.worker.LocalWorkersState;
import net.tvburger.sjawl.deploy.local.worker.WorkersState;
import net.tvburger.sjawl.deploy.service.ServiceProvider;
import net.tvburger.sjawl.deploy.service.ServiceRegistry;
import net.tvburger.sjawl.deploy.service.ServicesAdministrator;
import net.tvburger.sjawl.deploy.worker.WorkerDeployer;
import net.tvburger.sjawl.deploy.worker.WorkersAdministrator;

import java.util.UUID;

public final class DefaultDeploymentContext implements DeploymentContext {

    public static final class Factory {

        public static DefaultDeploymentContext create() {
            return create(new DefaultLocalServicesStateManager(), new LocalWorkersState());
        }

        public static DefaultDeploymentContext create(LocalServicesStateManager localServicesStateManager, WorkersState workersState) {
            AssertUtil.assertNotNull(localServicesStateManager);
            AssertUtil.assertNotNull(workersState);
            return new DefaultDeploymentContext(
                    System.getProperty("deploymentId", UUID.randomUUID().toString()),
                    new DefaultServiceProvider(localServicesStateManager),
                    new DefaultServiceRegistry(localServicesStateManager),
                    new DefaultServicesAdministrator(localServicesStateManager),
                    new DefaultWorkerDeployer(workersState),
                    new DefaultWorkersAdministrator(workersState));
        }

        public Factory() {
        }

    }

    private final String deploymentId;
    private final ServiceProvider serviceProvider;
    private final ServiceRegistry serviceRegistry;
    private final ServicesAdministrator servicesAdministrator;
    private final WorkerDeployer workerDeployer;
    private final WorkersAdministrator workersAdministrator;

    protected DefaultDeploymentContext(String deploymentId, ServiceProvider serviceProvider, ServiceRegistry serviceRegistry, ServicesAdministrator servicesAdministrator, WorkerDeployer workerDeployer, WorkersAdministrator workersAdministrator) {
        this.deploymentId = deploymentId;
        this.serviceProvider = serviceProvider;
        this.serviceRegistry = serviceRegistry;
        this.servicesAdministrator = servicesAdministrator;
        this.workerDeployer = workerDeployer;
        this.workersAdministrator = workersAdministrator;
    }

    @Override
    public String getDeploymentId() {
        return deploymentId;
    }

    @Override
    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    @Override
    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    @Override
    public ServicesAdministrator getServicesAdministrator() {
        return servicesAdministrator;
    }

    @Override
    public WorkerDeployer getWorkerDeployer() {
        return workerDeployer;
    }

    @Override
    public WorkersAdministrator getWorkersAdministrator() {
        return workersAdministrator;
    }

}
