package net.tvburger.sjawl.deploy.distributed;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.DeploymentContext;
import net.tvburger.sjawl.deploy.distributed.protocol.Address;
import net.tvburger.sjawl.deploy.distributed.spi.ProtocolProvider;
import net.tvburger.sjawl.deploy.distributed.spi.RemoteStoreProvider;
import net.tvburger.sjawl.deploy.local.LocalDeploymentContext;
import net.tvburger.sjawl.deploy.local.impl.DefaultLocalWorkersStore;
import net.tvburger.sjawl.deploy.utils.DecoratedDeploymentContext;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.UUID;

public class DistributedDeploymentContext<A extends Address> extends DecoratedDeploymentContext {

    public static final class Factory {

        public static <A extends Address> DistributedDeploymentContext<A> create() throws DeployException {
            return create("default");
        }

        @SuppressWarnings("unchecked")
        public static <A extends Address> DistributedDeploymentContext<A> create(String deploymentId) throws DeployException {
            ProtocolProvider.Factory<A> protocolFactory = getService(ProtocolProvider.Factory.class);
            RemoteStoreProvider.Factory remoteStoreFactory = getService(RemoteStoreProvider.Factory.class);

            RemoteStoreProvider<A> remoteStoreProvider = remoteStoreFactory.create(deploymentId, protocolFactory.getAddressSerializer());
            ProtocolProvider<A> protocolProvider = protocolFactory.create(remoteStoreProvider);

            DeploymentContext deploymentContext = LocalDeploymentContext.Factory.create(
                    deploymentId,
                    protocolProvider.getLocalServicesStore(),
                    new DefaultLocalWorkersStore());

            return new DistributedDeploymentContext(deploymentContext, protocolProvider);
        }

        private static <T> T getService(Class<T> serviceClass) throws DeployException {
            ServiceLoader<T> serviceLoader = ServiceLoader.load(serviceClass);
            Iterator<T> iterator = serviceLoader.iterator();
            if (!iterator.hasNext()) {
                throw new DeployException("No service registered for: " + serviceClass.getName());
            }
            T service = iterator.next();
            if (iterator.hasNext()) {
                throw new DeployException("Multiple services registered for: " + serviceClass.getName());
            }
            return service;
        }

    }

    private final ProtocolProvider<A> protocolProvider;

    protected DistributedDeploymentContext(DeploymentContext deploymentContext, ProtocolProvider<A> protocolProvider) {
        super(deploymentContext);
        this.protocolProvider = protocolProvider;
    }

    public A getAddress() {
        return protocolProvider.getAddress();
    }

    public UUID getSiteId() {
        return protocolProvider.getSiteId();
    }

    public SiteRegistry<A> getSiteRegistry() {
        return protocolProvider.getSiteRegistry();
    }

    @Override
    public void close() throws DeployException {
        try {
            super.close();
        } finally {
            protocolProvider.getServerContext().close();
        }
    }

}
