package net.tvburger.sjawl.deploy.example;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.DeploymentContext;
import net.tvburger.sjawl.deploy.local.LocalDeploymentContext;
import net.tvburger.sjawl.deploy.service.ServiceProvider;
import net.tvburger.sjawl.deploy.service.ServiceRegistry;
import net.tvburger.sjawl.deploy.service.ServicesAdministrator;
import net.tvburger.sjawl.deploy.strategies.DeploymentStrategyProvider;
import net.tvburger.sjawl.deploy.worker.WorkerDeployer;
import net.tvburger.sjawl.deploy.worker.WorkersAdministrator;
import org.junit.Assert;
import org.junit.Test;

public class ExampleTest {

    @Test
    public void testRoundRobinStrategy() throws DeployException {
        DeploymentContext context = LocalDeploymentContext.Factory.create();
        ServicesAdministrator administrator = context.getServicesAdministrator();
        administrator.registerServiceType(HelloService.class, DeploymentStrategyProvider.getRoundRobinStrategy());

        ServiceRegistry registry = context.getServiceRegistry();
        registry.registerService(HelloService.class, new HelloServiceImpl("Howdy, %s!"));
        registry.registerService(HelloService.class, new HelloServiceImpl("Hi, %s!"));

        ServiceProvider provider = context.getServiceProvider();
        Assert.assertEquals("Howdy, John!", provider.getService(HelloService.class).sayHelloTo("John"));
        Assert.assertEquals("Hi, Simon!", provider.getService(HelloService.class).sayHelloTo("Simon"));
        Assert.assertEquals("Howdy, Alma!", provider.getService(HelloService.class).sayHelloTo("Alma"));
        Assert.assertEquals("Hi, Edith!", provider.getService(HelloService.class).sayHelloTo("Edith"));

        administrator.unregisterServiceType(HelloService.class);
    }

    @Test
    public void testFirstActiveStrategy() throws DeployException {
        DeploymentContext context = LocalDeploymentContext.Factory.create();
        ServicesAdministrator administrator = context.getServicesAdministrator();
        administrator.registerServiceType(HelloService.class, DeploymentStrategyProvider.getFirstAvailableStrategy());

        ServiceRegistry registry = context.getServiceRegistry();
        registry.registerService(HelloService.class, new HelloServiceImpl("Howdy, %s!"));
        registry.registerService(HelloService.class, new HelloServiceImpl("Hi, %s!"));

        ServiceProvider provider = context.getServiceProvider();
        Assert.assertEquals("Howdy, John!", provider.getService(HelloService.class).sayHelloTo("John"));
        Assert.assertEquals("Howdy, Simon!", provider.getService(HelloService.class).sayHelloTo("Simon"));
        Assert.assertEquals("Howdy, Alma!", provider.getService(HelloService.class).sayHelloTo("Alma"));
        Assert.assertEquals("Howdy, Edith!", provider.getService(HelloService.class).sayHelloTo("Edith"));

        administrator.unregisterServiceType(HelloService.class);
    }

    @Test
    public void testFilterDemo() throws DeployException {
        DeploymentContext context = LocalDeploymentContext.Factory.create();
        ServicesAdministrator administrator = context.getServicesAdministrator();
        administrator.registerServiceType(HelloService.class, DeploymentStrategyProvider.getFirstAvailableStrategy());

        ServiceRegistry registry = context.getServiceRegistry();
        registry.registerService(HelloService.class, new HelloServiceImpl("Hi %s!"), LanguageSpecification.Factory.create("US"));
        registry.registerService(HelloService.class, new HelloServiceImpl("Hallo %s!"), LanguageSpecification.Factory.create("NL"));
        registry.registerService(HelloService.class, new HelloServiceImpl("Namaste %s!"), LanguageSpecification.Factory.create("IN"));

        ServiceProvider provider = context.getServiceProvider();
        Assert.assertEquals("Hi John!", provider.getService(HelloService.class, LanguageSpecification.Factory.create("US")).sayHelloTo("John"));
        Assert.assertEquals("Hallo John!", provider.getService(HelloService.class, LanguageSpecification.Factory.create("NL")).sayHelloTo("John"));
        Assert.assertEquals("Namaste John!", provider.getService(HelloService.class, LanguageSpecification.Factory.create("IN")).sayHelloTo("John"));

        administrator.unregisterServiceType(HelloService.class);
    }

    @Test
    public void testWorkerActiveActiveDemo() throws DeployException, InterruptedException {
        DeploymentContext context = LocalDeploymentContext.Factory.create();
        WorkersAdministrator administrator = context.getWorkerAdministrator();
        administrator.registerWorkerType(TestWorker.class, DeploymentStrategyProvider.getActiveActiveStrategy());

        TestWorker worker1 = new TestWorker("Worker 1");
        TestWorker worker2 = new TestWorker("Worker 2");

        synchronized (this) {
            WorkerDeployer deployer = context.getWorkerDeployer();
            deployer.deployWorker(TestWorker.class, worker1, worker1);
            deployer.deployWorker(TestWorker.class, worker2, worker2);

            wait(250);

            deployer.undeployWorker(TestWorker.class, worker1);

            wait(250);
        }

        administrator.unregisterWorkerType(TestWorker.class);
    }

    @Test
    public void testWorkerActiveStandbyDemo() throws DeployException, InterruptedException {
        DeploymentContext context = LocalDeploymentContext.Factory.create();
        WorkersAdministrator administrator = context.getWorkerAdministrator();
        administrator.registerWorkerType(TestWorker.class, DeploymentStrategyProvider.getActiveStandbyStrategy());

        TestWorker worker1 = new TestWorker("Worker 1");
        TestWorker worker2 = new TestWorker("Worker 2");

        synchronized (this) {
            WorkerDeployer deployer = context.getWorkerDeployer();
            deployer.deployWorker(TestWorker.class, worker1, worker1);
            deployer.deployWorker(TestWorker.class, worker2, worker2);

            wait(250);

            deployer.undeployWorker(TestWorker.class, worker1);

            wait(250);
        }

        administrator.unregisterWorkerType(TestWorker.class);
    }

}
