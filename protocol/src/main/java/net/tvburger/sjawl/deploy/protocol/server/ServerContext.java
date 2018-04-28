package net.tvburger.sjawl.deploy.protocol.server;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.DeploymentContext;
import net.tvburger.sjawl.deploy.ServiceRegistry;
import net.tvburger.sjawl.deploy.WorkerDeployer;
import net.tvburger.sjawl.deploy.admin.ServicesAdministrator;
import net.tvburger.sjawl.deploy.admin.WorkersAdministrator;
import net.tvburger.sjawl.deploy.distributed.SiteRegistrationManager;
import net.tvburger.sjawl.deploy.local.LocalDeploymentContext;
import net.tvburger.sjawl.deploy.local.LocalServicesStore;
import net.tvburger.sjawl.deploy.protocol.TcpAddress;
import net.tvburger.sjawl.deploy.protocol.config.ProtocolConfiguration;
import net.tvburger.sjawl.deploy.strategies.DeploymentStrategyProvider;
import net.tvburger.sjawl.deploy.utils.DecoratedDeploymentContext;
import net.tvburger.sjawl.deploy.utils.ManagedWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class ServerContext extends DecoratedDeploymentContext {

    private static Logger LOGGER = LoggerFactory.getLogger(ServerContext.class);

    public static class Factory {

        public static ServerContext create(ProtocolConfiguration configuration, LocalServicesStore store, SiteRegistrationManager<TcpAddress> siteRegistrationManager, UUID siteId) throws DeployException {
            LocalDeploymentContext context = null;
            try {
                context = LocalDeploymentContext.Factory.create();
                BlockingQueue<Socket> clientConnectionQueue = new LinkedBlockingQueue<>();

                WorkersAdministrator administrator = context.getWorkersAdministrator();
                administrator.registerWorkerType(ConnectionAcceptor.class, DeploymentStrategyProvider.getActiveStandbyStrategy());
                administrator.registerWorkerType(RequestHandler.class, DeploymentStrategyProvider.getActiveActiveStrategy());

                WorkerDeployer deployer = context.getWorkerDeployer();
                ConnectionAcceptor connectionAcceptor = new ConnectionAcceptor(configuration.getAddress(), clientConnectionQueue, siteRegistrationManager, siteId);
                deployer.deployWorker(ConnectionAcceptor.class, connectionAcceptor, ManagedWorker.Activator.Singleton.get());
                for (int i = 0; i < configuration.getServiceThreadCount(); i++) {
                    deployer.deployWorker(RequestHandler.class, new RequestHandler(clientConnectionQueue, store), ManagedWorker.Activator.Singleton.get());
                }

                ServicesAdministrator servicesAdministrator = context.getServicesAdministrator();
                servicesAdministrator.registerServiceType(AddressProvider.class, DeploymentStrategyProvider.getFirstAvailableStrategy());

                ServiceRegistry registry = context.getServiceRegistry();
                registry.registerService(AddressProvider.class, connectionAcceptor);

                return new ServerContext(context, clientConnectionQueue);
            } catch (DeployException cause) {
                System.out.println(cause.getMessage());
                try {
                    context.close();
                } catch (DeployException innerCause) {
                }
                throw new DeployException("Failed to initialize server: " + cause.getMessage(), cause);
            }
        }
    }

    private final BlockingQueue<Socket> clientConnectionQueue;

    ServerContext(DeploymentContext deploymentContext, BlockingQueue<Socket> clientConnectionQueue) {
        super(deploymentContext);
        this.clientConnectionQueue = clientConnectionQueue;
        LOGGER.debug("ServerContext instantiated");
    }

    public TcpAddress getAddress() throws DeployException {
        return getServiceProvider().getService(AddressProvider.class).getAddress();
    }

    @Override
    public void close() throws DeployException {
        LOGGER.debug("close");
        try {
            super.close();
        } finally {
            closeClientSockets();
        }
    }

    private void closeClientSockets() {
        Set<Socket> sockets = new HashSet<>();
        clientConnectionQueue.drainTo(sockets);
        LOGGER.debug("Going to close sockets: " + sockets);
        for (Socket socket : sockets) {
            try {
                socket.close();
            } catch (IOException cause) {
                LOGGER.debug("Failed to close socket: " + socket);
            }
        }
    }

}
