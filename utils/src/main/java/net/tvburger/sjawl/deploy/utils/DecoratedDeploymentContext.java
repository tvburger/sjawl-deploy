package net.tvburger.sjawl.deploy.utils;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.DeploymentContext;
import net.tvburger.sjawl.deploy.ServiceProvider;
import net.tvburger.sjawl.deploy.ServiceRegistry;
import net.tvburger.sjawl.deploy.admin.ServicesAdministrator;
import net.tvburger.sjawl.deploy.WorkerDeployer;
import net.tvburger.sjawl.deploy.admin.WorkersAdministrator;

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

    @Override
    public void close() throws DeployException {
        deploymentContext.close();
    }

}
