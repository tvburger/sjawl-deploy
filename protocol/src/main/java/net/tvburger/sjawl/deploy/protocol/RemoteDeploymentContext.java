package net.tvburger.sjawl.deploy.protocol;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.DeploymentContext;
import net.tvburger.sjawl.deploy.WorkerDeployer;
import net.tvburger.sjawl.deploy.admin.WorkersAdministrator;
import net.tvburger.sjawl.deploy.local.LocalDeploymentContext;
import net.tvburger.sjawl.deploy.local.LocalServicesStore;
import net.tvburger.sjawl.deploy.local.impl.DefaultLocalWorkersStore;
import net.tvburger.sjawl.deploy.protocol.server.ConnectionAcceptor;
import net.tvburger.sjawl.deploy.protocol.server.RequestHandler;
import net.tvburger.sjawl.deploy.remote.RemoteProvider;
import net.tvburger.sjawl.deploy.remote.impl.RemoteStateSiteRegistry;
import net.tvburger.sjawl.deploy.strategies.DeploymentStrategyProvider;
import net.tvburger.sjawl.deploy.utils.DecoratedDeploymentContext;
import net.tvburger.sjawl.deploy.utils.ManagedWorker;

import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RemoteDeploymentContext extends DecoratedDeploymentContext {

    public static final class Factory {

        public static RemoteDeploymentContext create(String deploymentId, UUID siteId, TcpAddress address, RemoteProvider.Factory factory) throws DeployException {
            ProtocolProvider protocolProvider = ProtocolProvider.Factory.create(siteId, address, factory);
            DeploymentContext deploymentContext = LocalDeploymentContext.Factory.create(
                    deploymentId, protocolProvider.getLocalServicesStore(), new DefaultLocalWorkersStore());
            LocalDeploymentContext serverContext = createServerContext(address, protocolProvider.getLocalServicesStore());
            return new RemoteDeploymentContext(deploymentContext, protocolProvider, serverContext);
        }

        private static LocalDeploymentContext createServerContext(TcpAddress address, LocalServicesStore store) throws DeployException {
            LocalDeploymentContext serverContext = null;
            try {
                serverContext = LocalDeploymentContext.Factory.create(UUID.randomUUID().toString());
                BlockingQueue<Socket> socketQueue = new LinkedBlockingQueue<>();

                WorkersAdministrator administrator = serverContext.getWorkersAdministrator();
                administrator.registerWorkerType(ConnectionAcceptor.class, DeploymentStrategyProvider.getActiveStandbyStrategy());
                administrator.registerWorkerType(RequestHandler.class, DeploymentStrategyProvider.getActiveActiveStrategy());

                WorkerDeployer deployer = serverContext.getWorkerDeployer();
                deployer.deployWorker(ConnectionAcceptor.class, new ConnectionAcceptor(address, socketQueue), ManagedWorker.Activator.Singleton.get());
                deployer.deployWorker(RequestHandler.class, new RequestHandler(socketQueue, store), ManagedWorker.Activator.Singleton.get());
                deployer.deployWorker(RequestHandler.class, new RequestHandler(socketQueue, store), ManagedWorker.Activator.Singleton.get());
                deployer.deployWorker(RequestHandler.class, new RequestHandler(socketQueue, store), ManagedWorker.Activator.Singleton.get());
                deployer.deployWorker(RequestHandler.class, new RequestHandler(socketQueue, store), ManagedWorker.Activator.Singleton.get());

                return serverContext;
            } catch (DeployException cause) {
                if (serverContext != null) {
                    try {
                        serverContext.close();
                    } catch (DeployException innerCause) {
                    }
                }
                throw new DeployException("Failed to initialize server: " + cause.getMessage(), cause);
            }
        }
    }

    private final ProtocolProvider protocolProvider;
    private final LocalDeploymentContext serverContext;

    protected RemoteDeploymentContext(DeploymentContext deploymentContext, ProtocolProvider protocolProvider, LocalDeploymentContext serverContext) {
        super(deploymentContext);
        this.protocolProvider = protocolProvider;
        this.serverContext = serverContext;
    }

    public TcpAddress getAddress() {
        return protocolProvider.getAddress();
    }

    public UUID getSiteId() {
        return protocolProvider.getSiteId();
    }

    public RemoteStateSiteRegistry<TcpAddress> getSiteRegistry() {
        return protocolProvider.getSiteRegistry();
    }

    @Override
    public void close() throws DeployException {
        super.close();
        serverContext.close();
    }

}
