package net.tvburger.sjawl.deploy.distributed.remote.impl;

import net.tvburger.sjawl.common.UnlimitedCache;
import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.ServiceDeploymentStrategy;
import net.tvburger.sjawl.deploy.admin.ServiceRegistration;
import net.tvburger.sjawl.deploy.local.LocalServicesStore;
import net.tvburger.sjawl.deploy.distributed.remote.RemoteServiceRegistration;
import net.tvburger.sjawl.deploy.distributed.remote.RemoteServicesStore;
import net.tvburger.sjawl.deploy.distributed.mappers.ClassNameMapper;
import net.tvburger.sjawl.deploy.distributed.mappers.InstanceNameMapper;
import net.tvburger.sjawl.deploy.distributed.mappers.RemoteServiceRegistrationMapper;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class RemoteLocalServicesStore implements LocalServicesStore {

    public static final class Factory {

        @SuppressWarnings("unchecked")
        public static RemoteLocalServicesStore create(RemoteServicesStore remoteServicesStore, RemoteStoreSiteRegistry<?> siteRegistry, RemoteServiceRegistrationMapper mapper) {
            return new RemoteLocalServicesStore(
                    remoteServicesStore,
                    new ClassNameMapper(new UnlimitedCache<>()),
                    new InstanceNameMapper<>((Class) ServiceDeploymentStrategy.class, new UnlimitedCache<>()),
                    mapper);
        }

        private Factory() {
        }

    }

    @SuppressWarnings("unchecked")
    private class IgnoringUnknownRemoteStateListener implements RemoteServicesStore.Listener {

        private final Listener listener;
        private final LocalServicesStore localStateManager;

        private IgnoringUnknownRemoteStateListener(Listener listener, LocalServicesStore localStateManager) {
            this.listener = listener;
            this.localStateManager = localStateManager;
        }

        @Override
        public void serviceTypeAdded(RemoteServicesStore stateManager, String serviceTypeName, String deploymentStrategy) {
            try {
                listener.serviceTypeAdded(localStateManager,
                        serviceTypeMapper.toClass(serviceTypeName),
                        (ServiceDeploymentStrategy) deploymentStrategyMapper.toInstance(deploymentStrategy));
            } catch (DeployException cause) {
            }
        }

        @Override
        public void serviceTypeRemoved(RemoteServicesStore stateManager, String serviceTypeName) {
            try {
                listener.serviceTypeRemoved(localStateManager, serviceTypeMapper.toClass(serviceTypeName));
            } catch (DeployException cause) {
            }
        }

        @Override
        public void serviceRegistrationAdded(RemoteServicesStore stateManager, String serviceTypeName, RemoteServiceRegistration serviceRegistration) {
            try {
                listener.serviceRegistrationAdded(localStateManager, serviceRegistrationMapper.fromRemote(serviceRegistration));
            } catch (DeployException cause) {
            }
        }

        @Override
        public void serviceRegistrationRemoved(RemoteServicesStore stateManager, String serviceTypeName, RemoteServiceRegistration serviceRegistration) {
            try {
                listener.serviceRegistrationRemoved(localStateManager, serviceRegistrationMapper.fromRemote(serviceRegistration));
            } catch (DeployException cause) {
            }
        }

    }

    private final Map<Listener, IgnoringUnknownRemoteStateListener> listeners = new ConcurrentHashMap<>();
    private final RemoteServicesStore remoteStore;
    private final ClassNameMapper serviceTypeMapper;
    private final InstanceNameMapper<ServiceDeploymentStrategy<?>> deploymentStrategyMapper;
    private final RemoteServiceRegistrationMapper serviceRegistrationMapper;

    public RemoteLocalServicesStore(RemoteServicesStore remoteStore, ClassNameMapper serviceTypeMapper, InstanceNameMapper<ServiceDeploymentStrategy<?>> deploymentStrategyMapper, RemoteServiceRegistrationMapper serviceRegistrationMapper) {
        this.remoteStore = remoteStore;
        this.serviceTypeMapper = serviceTypeMapper;
        this.deploymentStrategyMapper = deploymentStrategyMapper;
        this.serviceRegistrationMapper = serviceRegistrationMapper;
    }

    @Override
    public void addListener(Listener listener) {
        IgnoringUnknownRemoteStateListener remoteStateListener = new IgnoringUnknownRemoteStateListener(listener, this);
        listeners.put(listener, remoteStateListener);
        remoteStore.addListener(remoteStateListener);
    }

    @Override
    public void removeListener(Listener listener) {
        remoteStore.removeListener(listeners.remove(listener));
    }

    @Override
    public Object getLock() throws DeployException {
        try {
            return remoteStore.getLock();
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    @Override
    public <T> void addServiceType(Class<T> serviceTypeClass, ServiceDeploymentStrategy<T> deploymentStrategy) throws DeployException {
        try {
            assertIsInterface(serviceTypeClass);
            remoteStore.addServiceType(serviceTypeMapper.toName(serviceTypeClass), deploymentStrategyMapper.toName(deploymentStrategy));
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    @Override
    public <T> void removeServiceType(Class<T> serviceTypeClass) throws DeployException {
        try {
            assertIsInterface(serviceTypeClass);
            remoteStore.removeServiceType(serviceTypeMapper.toName(serviceTypeClass));
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    @Override
    public <T> boolean hasServiceType(Class<T> serviceTypeClass) throws DeployException {
        try {
            return remoteStore.hasServiceType(serviceTypeMapper.toName(serviceTypeClass));
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    @Override
    public Collection<Class<?>> getServiceTypes() throws DeployException {
        try {
            Set<Class<?>> serviceTypeClasses = new HashSet<>();
            for (String serviceTypeName : remoteStore.getServiceTypes()) {
                serviceTypeClasses.add(serviceTypeMapper.toClass(serviceTypeName));
            }
            return serviceTypeClasses;
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ServiceDeploymentStrategy<T> getServiceDeploymentStrategy(Class<T> serviceTypeClass) throws DeployException {
        try {
            assertIsInterface(serviceTypeClass);
            return (ServiceDeploymentStrategy) deploymentStrategyMapper.toInstance(
                    remoteStore.getServiceDeploymentStrategy(
                            serviceTypeMapper.toName(serviceTypeClass)));
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    @Override
    public <T> void addServiceRegistration(ServiceRegistration<T> serviceRegistration) throws DeployException {
        try {
            assertIsInterface(serviceRegistration.getServiceType());
            remoteStore.addServiceRegistration(serviceRegistrationMapper.toRemote(serviceRegistration));
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeServiceRegistration(UUID serviceRegistrationId) throws DeployException {
        try {
            remoteStore.removeServiceRegistration(serviceRegistrationId);
        } catch (IOException cause) {
            throw new DeployException(cause);
        }

    }

    @Override
    public boolean hasServiceRegistration(UUID serviceRegistrationId) throws DeployException {
        try {
            return remoteStore.hasServiceRegistration(serviceRegistrationId);
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    @Override
    public <T> ServiceRegistration<T> getServiceRegistration(UUID serviceRegistrationId) throws DeployException {
        try {
            RemoteServiceRegistration remoteServiceRegistration = remoteStore.getServiceRegistration(serviceRegistrationId);
            return serviceRegistrationMapper.fromRemote(remoteServiceRegistration);
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    @Override
    public <T> Collection<ServiceRegistration<T>> getServiceRegistrations(Class<T> serviceTypeClass) throws DeployException {
        try {
            assertIsInterface(serviceTypeClass);
            Set<ServiceRegistration<T>> serviceRegistrations = new HashSet<>();
            String serviceTypeName = serviceTypeMapper.toName(serviceTypeClass);
            for (RemoteServiceRegistration remoteServiceRegistration : remoteStore.getServiceRegistrations(serviceTypeName)) {
                serviceRegistrations.add(serviceRegistrationMapper.fromRemote(remoteServiceRegistration));
            }
            return serviceRegistrations;
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    private <T> void assertIsInterface(Class<T> serviceTypeClass) throws DeployException {
        if (!serviceTypeClass.isInterface()) {
            throw new DeployException("Must specify an interface!");
        }
    }

}
