package net.tvburger.sjawl.deploy;

import net.tvburger.sjawl.deploy.admin.ServicesAdministrator;
import net.tvburger.sjawl.deploy.admin.WorkersAdministrator;

public interface DeploymentContext extends AutoCloseable {

    String getDeploymentId();

    ServiceProvider getServiceProvider();

    ServiceRegistry getServiceRegistry();

    ServicesAdministrator getServicesAdministrator();

    WorkerDeployer getWorkerDeployer();

    WorkersAdministrator getWorkersAdministrator();

    @Override
    void close() throws DeployException;

}
