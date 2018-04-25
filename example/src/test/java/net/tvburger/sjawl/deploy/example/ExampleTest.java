package net.tvburger.sjawl.deploy.example;

import net.tvburger.sjawl.deploy.*;
import net.tvburger.sjawl.deploy.admin.ServicesAdministrator;
import net.tvburger.sjawl.deploy.admin.WorkersAdministrator;
import net.tvburger.sjawl.deploy.local.LocalDeploymentContext;
import net.tvburger.sjawl.deploy.strategies.DeploymentStrategyProvider;
import net.tvburger.sjawl.deploy.utils.ManagedWorker;
import org.junit.Assert;
import org.junit.Test;

public class ExampleTest {

    @Test
    public void testRoundRobinStrategy() throws DeployException {
        try (DeploymentContext context = LocalDeploymentContext.Factory.create()) {
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
        }
    }

    @Test
    public void testFirstActiveStrategy() throws DeployException {
        try (DeploymentContext context = LocalDeploymentContext.Factory.create()) {
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
        }
    }

    @Test
    public void testFilterDemo() throws DeployException {
        try (DeploymentContext context = LocalDeploymentContext.Factory.create()) {
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
        }
    }

    @Test
    public void testWorkerActiveActiveDemo() throws DeployException, InterruptedException {
        try (DeploymentContext context = LocalDeploymentContext.Factory.create()) {
            WorkersAdministrator administrator = context.getWorkersAdministrator();
            administrator.registerWorkerType(TestWorker.class, DeploymentStrategyProvider.getActiveActiveStrategy());

            TestWorker worker1 = new TestWorker("Worker 1");
            TestWorker worker2 = new TestWorker("Worker 2");

            synchronized (this) {
                WorkerDeployer deployer = context.getWorkerDeployer();
                WorkerActivator<TestWorker> activator = ManagedWorker.Activator.Singleton.get();
                deployer.deployWorker(TestWorker.class, worker1, activator);
                deployer.deployWorker(TestWorker.class, worker2, activator);

                wait(250);

                deployer.undeployWorker(TestWorker.class, worker1);

                wait(250);
            }
        }
    }

    @Test
    public void testWorkerActiveStandbyDemo() throws DeployException, InterruptedException {
        try (DeploymentContext context = LocalDeploymentContext.Factory.create()) {
            WorkersAdministrator administrator = context.getWorkersAdministrator();
            administrator.registerWorkerType(TestWorker.class, DeploymentStrategyProvider.getActiveStandbyStrategy());

            TestWorker worker1 = new TestWorker("Worker 1");
            TestWorker worker2 = new TestWorker("Worker 2");

            synchronized (this) {
                WorkerDeployer deployer = context.getWorkerDeployer();
                WorkerActivator<TestWorker> activator = ManagedWorker.Activator.Singleton.get();
                deployer.deployWorker(TestWorker.class, worker1, activator);
                deployer.deployWorker(TestWorker.class, worker2, activator);

                wait(250);

                deployer.undeployWorker(TestWorker.class, worker1);

                wait(250);
            }
        }
    }

}
