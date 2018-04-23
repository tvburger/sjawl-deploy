package net.tvburger.sjawl.deploy.remote.service;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.local.service.LocalServicesStateManager;
import net.tvburger.sjawl.deploy.remote.mappers.ClassNameMapper;
import net.tvburger.sjawl.deploy.remote.mappers.InstanceNameMapper;
import net.tvburger.sjawl.deploy.service.ServiceDeploymentStrategy;
import net.tvburger.sjawl.deploy.service.ServiceRegistration;

import java.io.IOException;
import java.rmi.Remote;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class RemoteLocalServicesStateManager implements LocalServicesStateManager {

    @SuppressWarnings("unchecked")
    private class IgnoringUnknownRemoteStateListener implements RemoteServicesStateManager.Listener {

        private final Listener listener;
        private final LocalServicesStateManager localStateManager;

        private IgnoringUnknownRemoteStateListener(Listener listener, LocalServicesStateManager localStateManager) {
            this.listener = listener;
            this.localStateManager = localStateManager;
        }

        @Override
        public void serviceTypeAdded(RemoteServicesStateManager stateManager, String serviceTypeName, String deploymentStrategy) {
            try {
                listener.serviceTypeAdded(localStateManager,
                        serviceTypeMapper.toClass(serviceTypeName),
                        (ServiceDeploymentStrategy) deploymentStrategyMapper.toInstance(deploymentStrategy));
            } catch (DeployException cause) {
            }
        }

        @Override
        public void serviceTypeRemoved(RemoteServicesStateManager stateManager, String serviceTypeName) {
            try {
                listener.serviceTypeRemoved(localStateManager, serviceTypeMapper.toClass(serviceTypeName));
            } catch (DeployException cause) {
            }
        }

        @Override
        public void serviceRegistrationAdded(RemoteServicesStateManager stateManager, String serviceTypeName, RemoteServiceRegistration serviceRegistration) {
            try {
                Class serviceTypeClass = serviceTypeMapper.toClass(serviceTypeName);
                listener.serviceRegistrationAdded(localStateManager, serviceTypeClass,
                        serviceRegistrationMapper.fromRemote(serviceTypeClass, serviceRegistration));
            } catch (DeployException cause) {
            }
        }

        @Override
        public void serviceRegistrationRemoved(RemoteServicesStateManager stateManager, String serviceTypeName, RemoteServiceRegistration serviceRegistration) {
            try {
                Class serviceTypeClass = serviceTypeMapper.toClass(serviceTypeName);
                listener.serviceRegistrationRemoved(localStateManager, serviceTypeClass,
                        serviceRegistrationMapper.fromRemote(serviceTypeClass, serviceRegistration));
            } catch (DeployException cause) {
            }
        }

    }

    private final Map<Listener, IgnoringUnknownRemoteStateListener> listeners = new ConcurrentHashMap<>();
    private final RemoteServicesStateManager remoteStateManager;
    private final ClassNameMapper serviceTypeMapper;
    private final InstanceNameMapper<ServiceDeploymentStrategy<?>> deploymentStrategyMapper;
    private final RemoteServiceRegistrationMapper serviceRegistrationMapper;

    public RemoteLocalServicesStateManager(RemoteServicesStateManager remoteStateManager, ClassNameMapper serviceTypeMapper, InstanceNameMapper<ServiceDeploymentStrategy<?>> deploymentStrategyMapper, RemoteServiceRegistrationMapper serviceRegistrationMapper) {
        this.remoteStateManager = remoteStateManager;
        this.serviceTypeMapper = serviceTypeMapper;
        this.deploymentStrategyMapper = deploymentStrategyMapper;
        this.serviceRegistrationMapper = serviceRegistrationMapper;
    }

    @Override
    public void addListener(Listener listener) {
        IgnoringUnknownRemoteStateListener remoteStateListener = new IgnoringUnknownRemoteStateListener(listener, this);
        listeners.put(listener, remoteStateListener);
        remoteStateManager.addListener(remoteStateListener);
    }

    @Override
    public void removeListener(Listener listener) {
        remoteStateManager.removeListener(listeners.remove(listener));
    }

    @Override
    public Object getLock() throws DeployException {
        try {
            return remoteStateManager.getLock();
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    @Override
    public <T> void addServiceType(Class<T> serviceTypeClass, ServiceDeploymentStrategy<T> deploymentStrategy) throws DeployException {
        try {
            assertIsRemoteAndInterface(serviceTypeClass);
            remoteStateManager.addServiceType(serviceTypeMapper.toName(serviceTypeClass), deploymentStrategyMapper.toName(deploymentStrategy));
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    @Override
    public <T> void removeServiceType(Class<T> serviceTypeClass) throws DeployException {
        try {
            assertIsRemoteAndInterface(serviceTypeClass);
            remoteStateManager.removeServiceType(serviceTypeMapper.toName(serviceTypeClass));
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    @Override
    public <T> boolean hasServiceType(Class<T> serviceTypeClass) throws DeployException {
        try {
            return remoteStateManager.hasServiceType(serviceTypeMapper.toName(serviceTypeClass));
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    @Override
    public Collection<Class<?>> getServiceTypeClasses() throws DeployException {
        try {
            Set<Class<?>> serviceTypeClasses = new HashSet<>();
            for (String serviceTypeName : remoteStateManager.getServiceTypeNames()) {
                serviceTypeClasses.add(serviceTypeMapper.toClass(serviceTypeName));
            }
            return serviceTypeClasses;
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ServiceDeploymentStrategy<T> getServiceTypeDeploymentStrategy(Class<T> serviceTypeClass) throws DeployException {
        try {
            assertIsRemoteAndInterface(serviceTypeClass);
            return (ServiceDeploymentStrategy) deploymentStrategyMapper.toInstance(
                    remoteStateManager.getServiceTypeDeploymentName(
                            serviceTypeMapper.toName(serviceTypeClass)));
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    @Override
    public <T> void addServiceRegistration(Class<T> serviceTypeClass, ServiceRegistration<T> serviceRegistration) throws DeployException {
        try {
            assertIsRemoteAndInterface(serviceTypeClass);
            remoteStateManager.addServiceRegistration(
                    serviceTypeMapper.toName(serviceTypeClass),
                    serviceRegistrationMapper.toRemote(serviceRegistration));
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> void removeServiceRegistration(Class<T> serviceTypeClass, ServiceRegistration<T> serviceRegistration) throws DeployException {
        try {
            assertIsRemoteAndInterface(serviceTypeClass);
            String serviceTypeName = serviceTypeMapper.toName(serviceTypeClass);
            RemoteServiceRegistration remoteServiceRegistration = serviceRegistrationMapper.toRemote(serviceRegistration);
            remoteStateManager.removeServiceRegistration(serviceTypeName, remoteServiceRegistration);
        } catch (IOException cause) {
            throw new DeployException(cause);
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Collection<ServiceRegistration<T>> getServiceRegistrations(Class<T> serviceTypeClass) throws DeployException {
        try {
            assertIsRemoteAndInterface(serviceTypeClass);
            Class<Remote> remoteServiceTypeClass = (Class<Remote>) serviceTypeClass;
            Set<ServiceRegistration<T>> serviceRegistrations = new HashSet<>();
            String serviceTypeName = serviceTypeMapper.toName(serviceTypeClass);
            for (RemoteServiceRegistration remoteServiceRegistration : remoteStateManager.getServiceRegistrations(serviceTypeName)) {
                serviceRegistrations.add(serviceRegistrationMapper.fromRemote(remoteServiceTypeClass, remoteServiceRegistration));
            }
            return serviceRegistrations;
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    private <T> void assertIsRemoteAndInterface(Class<T> serviceTypeClass) throws DeployException {
        if (!Remote.class.isAssignableFrom(serviceTypeClass)) {
            throw new DeployException("Service type must specify Remote!");
        }
        if (!serviceTypeClass.isInterface()) {
            throw new DeployException("Must specify an interface!");
        }
    }

}
