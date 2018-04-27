package net.tvburger.sjawl.deploy.example;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.ServiceProvider;
import net.tvburger.sjawl.deploy.ServiceRegistry;
import net.tvburger.sjawl.deploy.admin.ServicesAdministrator;
import net.tvburger.sjawl.deploy.protocol.RemoteDeploymentContext;
import net.tvburger.sjawl.deploy.protocol.TcpAddress;
import net.tvburger.sjawl.deploy.remote.RemoteProvider;
import net.tvburger.sjawl.deploy.strategies.DeploymentStrategyProvider;
import net.tvburger.sjawl.deploy.zookeeper.ZooKeeperStoreProvider;

import java.net.InetAddress;
import java.util.UUID;

public class ZooKeeperExample {

    private final String deploymentId = "test-env";
    private final UUID siteId1 = UUID.fromString("3362a302-03b5-4a6f-9a52-56cc922b011e");
    private final UUID siteId2 = UUID.fromString("22aaaaaa-03b5-bbbb-9a52-cccccccccccc");

    @SuppressWarnings("unchecked")
    public void demo() throws DeployException {
        try (RemoteDeploymentContext servicesContext = createRemoteDeploymentContext(siteId1, 9099)) {

            try (RemoteDeploymentContext userContext = createRemoteDeploymentContext(siteId2, 9100)) {

                ServicesAdministrator administrator = servicesContext.getServicesAdministrator();
                if (!administrator.isRegisteredServiceType(HelloService.class)) {
                    administrator.registerServiceType(HelloService.class, DeploymentStrategyProvider.getFirstAvailableStrategy());
                }

                ServiceRegistry registry = servicesContext.getServiceRegistry();
                HelloService usHelloService = new HelloServiceImpl("Hi %s!");
                HelloService nlHelloService = new HelloServiceImpl("Hallo %s!");
                HelloService inHelloService = new HelloServiceImpl("Namaste %s!");
                registry.registerService(HelloService.class, usHelloService, LanguageSpecification.Factory.create("US"));
                registry.registerService(HelloService.class, nlHelloService, LanguageSpecification.Factory.create("NL"));
                registry.registerService(HelloService.class, inHelloService, LanguageSpecification.Factory.create("IN"));

                ServiceProvider provider = userContext.getServiceProvider();
                System.out.println(provider.getService(HelloService.class, LanguageSpecification.Factory.create("US")).sayHelloTo("John"));
                System.out.println(provider.getService(HelloService.class, LanguageSpecification.Factory.create("NL")).sayHelloTo("John"));
                System.out.println(provider.getService(HelloService.class, LanguageSpecification.Factory.create("IN")).sayHelloTo("John"));

                registry.unregisterService(usHelloService);
                registry.unregisterService(nlHelloService);
                registry.unregisterService(inHelloService);
            }
        }
    }

    private RemoteDeploymentContext createRemoteDeploymentContext(UUID siteId, int port) throws DeployException {
        TcpAddress address = new TcpAddress(InetAddress.getLoopbackAddress(), port);
        RemoteProvider.Factory remoteProviderFactory = ZooKeeperStoreProvider.Factory.get(deploymentId, false);
        return RemoteDeploymentContext.Factory.create(deploymentId, siteId, address, remoteProviderFactory);
    }

    public static void main(String[] args) throws DeployException {
        new ZooKeeperExample().demo();
    }

}
