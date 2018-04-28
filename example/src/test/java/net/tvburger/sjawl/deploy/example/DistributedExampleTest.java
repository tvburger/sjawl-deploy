package net.tvburger.sjawl.deploy.example;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.ServiceProvider;
import net.tvburger.sjawl.deploy.ServiceRegistry;
import net.tvburger.sjawl.deploy.admin.ServicesAdministrator;
import net.tvburger.sjawl.deploy.distributed.DistributedDeploymentContext;
import net.tvburger.sjawl.deploy.strategies.DeploymentStrategyProvider;
import org.apache.curator.test.TestingServer;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class DistributedExampleTest {

    private final CountDownLatch serverStarted = new CountDownLatch(1);
    private final CountDownLatch clientFinished = new CountDownLatch(1);

    private class ServicePublisherSite implements Runnable {

        @Override
        public void run() {
            try (DistributedDeploymentContext context = DistributedDeploymentContext.Factory.create()) {

                ServicesAdministrator administrator = context.getServicesAdministrator();
                if (!administrator.isRegisteredServiceType(HelloService.class)) {
                    administrator.registerServiceType(HelloService.class, DeploymentStrategyProvider.getFirstAvailableStrategy());
                }

                ServiceRegistry registry = context.getServiceRegistry();
                HelloService usHelloService = new HelloServiceImpl("Hi %s!");
                HelloService nlHelloService = new HelloServiceImpl("Hallo %s!");
                HelloService inHelloService = new HelloServiceImpl("Namaste %s!");

                registry.registerService(HelloService.class, usHelloService, LanguageSpecification.Factory.create("US"));
                registry.registerService(HelloService.class, nlHelloService, LanguageSpecification.Factory.create("NL"));
                registry.registerService(HelloService.class, inHelloService, LanguageSpecification.Factory.create("IN"));

                serverStarted.countDown();
                clientFinished.await();

                registry.unregisterService(HelloService.class);
            } catch (DeployException | InterruptedException cause) {
                System.err.println("cause: " + cause.getMessage());
            }
        }

    }

    @SuppressWarnings("unchecked")
    @Test
    public void demo() throws Exception {
        new TestingServer(2181);

        Thread providerSiteThread = new Thread(new ServicePublisherSite());
        providerSiteThread.start();
        serverStarted.await();

        try (DistributedDeploymentContext context = DistributedDeploymentContext.Factory.create()) {
            ServiceProvider serviceProvider = context.getServiceProvider();

            HelloService inHelloService = serviceProvider.getService(HelloService.class, LanguageSpecification.Factory.create("IN"));
            System.out.println(inHelloService.sayHelloTo("John"));
            System.out.println(inHelloService.sayHelloTo("Simon"));
            System.out.println(inHelloService.sayHelloTo("Alma"));

            HelloService nlHelloService = serviceProvider.getService(HelloService.class, LanguageSpecification.Factory.create("NL"));
            System.out.println(nlHelloService.sayHelloTo("John"));
            System.out.println(nlHelloService.sayHelloTo("Simon"));
            System.out.println(nlHelloService.sayHelloTo("Alma"));
        } finally {
            clientFinished.countDown();
        }
    }

}
