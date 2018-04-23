package net.tvburger.sjawl.deploy;

import net.tvburger.sjawl.deploy.service.ServiceProvider;
import net.tvburger.sjawl.deploy.service.ServiceRegistry;
import net.tvburger.sjawl.deploy.service.ServicesAdministrator;
import net.tvburger.sjawl.deploy.worker.WorkerDeployer;
import net.tvburger.sjawl.deploy.worker.WorkersAdministrator;

public interface DeploymentContext {

    String getDeploymentId();

    ServiceProvider getServiceProvider();

    ServiceRegistry getServiceRegistry();

    ServicesAdministrator getServicesAdministrator();

    WorkerDeployer getWorkerDeployer();

    WorkersAdministrator getWorkersAdministrator();

}
