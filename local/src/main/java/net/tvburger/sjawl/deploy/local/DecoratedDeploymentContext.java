package net.tvburger.sjawl.deploy.local;

import net.tvburger.sjawl.deploy.DeploymentContext;
import net.tvburger.sjawl.deploy.service.ServiceProvider;
import net.tvburger.sjawl.deploy.service.ServiceRegistry;
import net.tvburger.sjawl.deploy.service.ServicesAdministrator;
import net.tvburger.sjawl.deploy.worker.WorkerDeployer;
import net.tvburger.sjawl.deploy.worker.WorkersAdministrator;

public abstract class DecoratedDeploymentContext implements DeploymentContext {

    private final DeploymentContext deploymentContext;

    public DecoratedDeploymentContext(DeploymentContext deploymentContext) {
        this.deploymentContext = deploymentContext;
    }

    @Override
    public String getDeploymentId() {
        return deploymentContext.getDeploymentId();
    }

    @Override
    public ServiceProvider getServiceProvider() {
        return deploymentContext.getServiceProvider();
    }

    @Override
    public ServiceRegistry getServiceRegistry() {
        return deploymentContext.getServiceRegistry();
    }

    @Override
    public ServicesAdministrator getServicesAdministrator() {
        return deploymentContext.getServicesAdministrator();
    }

    @Override
    public WorkerDeployer getWorkerDeployer() {
        return deploymentContext.getWorkerDeployer();
    }

    @Override
    public WorkersAdministrator getWorkersAdministrator() {
        return deploymentContext.getWorkersAdministrator();
    }

}
