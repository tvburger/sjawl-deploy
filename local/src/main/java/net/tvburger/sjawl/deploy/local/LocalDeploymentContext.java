package net.tvburger.sjawl.deploy.local;

import net.tvburger.sjawl.common.AssertUtil;
import net.tvburger.sjawl.deploy.*;
import net.tvburger.sjawl.deploy.admin.ServicesAdministrator;
import net.tvburger.sjawl.deploy.admin.WorkersAdministrator;
import net.tvburger.sjawl.deploy.local.impl.*;

import java.util.UUID;

public final class LocalDeploymentContext implements DeploymentContext {

    public static final class Factory {

        public static LocalDeploymentContext create() {
            return create(System.getProperty("deploymentId", UUID.randomUUID().toString()));
        }

        public static LocalDeploymentContext create(String deploymentId) {
            return create(deploymentId, new DefaultLocalServicesStore(), new DefaultLocalWorkersStore());
        }

        public static LocalDeploymentContext create(String deploymentId, LocalServicesStore localServicesStore, LocalWorkersStore localWorkersStore) {
            AssertUtil.assertNotNull(localServicesStore);
            AssertUtil.assertNotNull(localWorkersStore);
            return new LocalDeploymentContext(deploymentId,
                    new DefaultServiceProvider(localServicesStore),
                    DefaultServiceRegistry.Factory.create(localServicesStore),
                    new DefaultServicesAdministrator(localServicesStore),
                    new DefaultWorkerDeployer(localWorkersStore),
                    new DefaultWorkersAdministrator(localWorkersStore));
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

    protected LocalDeploymentContext(String deploymentId, ServiceProvider serviceProvider, ServiceRegistry serviceRegistry, ServicesAdministrator servicesAdministrator, WorkerDeployer workerDeployer, WorkersAdministrator workersAdministrator) {
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

    @Override
    public void close() throws DeployException {
        for (Class<?> workerType : workersAdministrator.getRegisteredWorkerTypes()) {
            workersAdministrator.unregisterWorkerType(workerType);
        }
        for (Class<?> serviceType : servicesAdministrator.getRegisteredServiceTypes()) {
            servicesAdministrator.unregisterServiceType(serviceType);
        }
    }

}
