package net.tvburger.sjawl.deploy.protocol;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.WorkerDeployer;
import net.tvburger.sjawl.deploy.admin.ServiceRegistration;
import net.tvburger.sjawl.deploy.admin.WorkersAdministrator;
import net.tvburger.sjawl.deploy.local.LocalDeploymentContext;
import net.tvburger.sjawl.deploy.local.LocalServicesStore;
import net.tvburger.sjawl.deploy.local.impl.DefaultLocalServicesStore;
import net.tvburger.sjawl.deploy.protocol.client.ObjectStreamServiceProxyFactory;
import net.tvburger.sjawl.deploy.protocol.client.SiteConnectionProvider;
import net.tvburger.sjawl.deploy.protocol.client.TcpSiteConnectionProvider;
import net.tvburger.sjawl.deploy.protocol.server.ConnectionAcceptor;
import net.tvburger.sjawl.deploy.protocol.server.RequestHandler;
import net.tvburger.sjawl.deploy.strategies.DeploymentStrategyProvider;
import net.tvburger.sjawl.deploy.utils.ManagedWorker;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RemoteDemo {

    private final LocalDeploymentContext serverContext = LocalDeploymentContext.Factory.create();
    private final LocalServicesStore store = new DefaultLocalServicesStore();

    public void demo() throws Exception {
        try {
            initServer();

            UUID serviceId = UUID.randomUUID();
            ServiceRegistration<?> serviceRegistration = new ServiceRegistration<>(serviceId, HelloService.class, new HelloServiceImpl(), null);

            store.addServiceType(HelloService.class, DeploymentStrategyProvider.getFirstAvailableStrategy());
            store.addServiceRegistration(serviceRegistration);
            UUID siteId = UUID.randomUUID();

            SiteConnectionProvider<TcpAddress> provider = new TcpSiteConnectionProvider();
            ObjectStreamServiceProxyFactory<TcpAddress> factory = new ObjectStreamServiceProxyFactory<>(null, provider);
            HelloService helloService = factory.createServiceProxy(HelloService.class, siteId, serviceId);

            System.out.println(helloService.sayHello());
            System.out.println(helloService.sayHello("John"));
        } finally {
            shutdownServer();
        }
    }

    private void shutdownServer() throws DeployException {
        WorkersAdministrator administrator = serverContext.getWorkersAdministrator();
        administrator.unregisterWorkerType(RequestHandler.class);
        administrator.unregisterWorkerType(ConnectionAcceptor.class);
    }

    private void initServer() throws DeployException, IOException {
        TcpAddress address = new TcpAddress(InetAddress.getLoopbackAddress(), 8081);
        BlockingQueue<Socket> socketQueue = new LinkedBlockingQueue<>();

        WorkersAdministrator administrator = serverContext.getWorkersAdministrator();
        administrator.registerWorkerType(ConnectionAcceptor.class, DeploymentStrategyProvider.getActiveStandbyStrategy());
        administrator.registerWorkerType(RequestHandler.class, DeploymentStrategyProvider.getActiveActiveStrategy());

        WorkerDeployer deployer = serverContext.getWorkerDeployer();
        deployer.deployWorker(ConnectionAcceptor.class, new ConnectionAcceptor(address, socketQueue), ManagedWorker.Activator.Singleton.get());
        deployer.deployWorker(RequestHandler.class, new RequestHandler(socketQueue, store), ManagedWorker.Activator.Singleton.get());
    }

    public static void main(String[] args) throws Exception {
        new RemoteDemo().demo();
    }

}
