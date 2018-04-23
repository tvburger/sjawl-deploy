package net.tvburger.sjawl.deploy.protocol;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.local.DefaultDeploymentContext;
import net.tvburger.sjawl.deploy.local.worker.AbstractWorker;
import net.tvburger.sjawl.deploy.protocol.client.ObjectStreamServiceProxyFactory;
import net.tvburger.sjawl.deploy.protocol.client.SiteConnectionProvider;
import net.tvburger.sjawl.deploy.protocol.client.TcpSiteConnectionProvider;
import net.tvburger.sjawl.deploy.protocol.server.ConnectionAcceptor;
import net.tvburger.sjawl.deploy.protocol.server.RequestHandler;
import net.tvburger.sjawl.deploy.remote.service.ServiceRegistrationRegistry;
import net.tvburger.sjawl.deploy.service.ServiceRegistration;
import net.tvburger.sjawl.deploy.strategies.DeploymentStrategyProvider;
import net.tvburger.sjawl.deploy.worker.WorkerDeployer;
import net.tvburger.sjawl.deploy.worker.WorkersAdministrator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RemoteDemo {

    private final DefaultDeploymentContext serverContext = DefaultDeploymentContext.Factory.create();
    private final ServiceRegistrationRegistry registry = new ServiceRegistrationRegistry();

    public void demo() throws Exception {
        try {
            initServer();

            ServiceRegistration<?> serviceRegistration = new ServiceRegistration<>(new HelloServiceImpl(), null);

            UUID serviceId = registry.addServiceRegistration(serviceRegistration);
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
        BlockingQueue<Socket> socketQueue = new LinkedBlockingQueue<>();
        ServerSocket serverSocket = new ServerSocket(8081, 10, InetAddress.getLoopbackAddress());

        WorkersAdministrator adminstrator = serverContext.getWorkersAdministrator();
        adminstrator.registerWorkerType(ConnectionAcceptor.class, DeploymentStrategyProvider.getActiveStandbyStrategy());
        adminstrator.registerWorkerType(RequestHandler.class, DeploymentStrategyProvider.getActiveActiveStrategy());

        WorkerDeployer deployer = serverContext.getWorkerDeployer();
        deployer.deployWorker(ConnectionAcceptor.class, new ConnectionAcceptor(serverSocket, socketQueue), AbstractWorker.Activator.Singleton.get());
        deployer.deployWorker(RequestHandler.class, new RequestHandler(socketQueue, registry), AbstractWorker.Activator.Singleton.get());
    }

    public static void main(String[] args) throws Exception {
        new RemoteDemo().demo();
    }

}
