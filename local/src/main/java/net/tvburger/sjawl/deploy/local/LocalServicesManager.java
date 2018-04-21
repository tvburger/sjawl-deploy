package net.tvburger.sjawl.deploy.local;

import net.tvburger.sjawl.common.AssertUtil;
import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.service.*;

import java.util.*;

@SuppressWarnings("unchecked")
public final class LocalServicesManager implements ServiceRegistry, ServiceProvider, ServicesAdministrator {

    private final Object lock = new Object();
    private final Map<Class, ServiceDeploymentStrategy> serviceTypes = new HashMap<>();
    private final Map<Class, List<ServiceRegistration>> services = new HashMap<>();

    @Override
    public <T> boolean isRegistered(Class<T> serviceTypeClass, T serviceInstance, ServiceProperties serviceProperties) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        AssertUtil.assertNotNull(serviceInstance);
        synchronized (lock) {
            assertRegistered(serviceTypeClass);
            for (ServiceRegistration<T> serviceRegistration : services.get(serviceTypeClass)) {
                if (serviceRegistration.getServiceInstance().equals(serviceInstance) &&
                        Objects.equals(serviceRegistration.getServiceProperties(), serviceProperties)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public <T> void registerService(Class<T> serviceTypeClass, T serviceInstance, ServiceProperties serviceProperties) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        AssertUtil.assertNotNull(serviceInstance);
        synchronized (lock) {
            assertRegistered(serviceTypeClass);
            services.get(serviceTypeClass).add(new ServiceRegistration<>(serviceInstance, serviceProperties));
        }
    }

    @Override
    public <T> void unregisterService(Class<T> serviceTypeClass, T serviceInstance, ServiceProperties serviceProperties) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        AssertUtil.assertNotNull(serviceInstance);
        synchronized (lock) {
            assertRegistered(serviceTypeClass);
            for (ServiceRegistration<T> serviceRegistration : services.get(serviceTypeClass)) {
                if (serviceRegistration.getServiceInstance().equals(serviceInstance) &&
                        Objects.equals(serviceRegistration.getServiceProperties(), serviceProperties)) {
                    services.get(serviceTypeClass).remove(serviceRegistration);
                    return;
                }
            }
            throw new DeployException("No such service is deployed!");
        }
    }

    @Override
    public boolean hasService(Class<?> serviceTypeClass, ServiceFilter serviceFilter) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        synchronized (lock) {
            return serviceTypes.containsKey(serviceTypeClass) && selectServiceRegistration(serviceTypeClass, serviceFilter) != null;
        }
    }

    @Override
    public <T> T getService(Class<T> serviceTypeClass, ServiceFilter serviceFilter) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        synchronized (lock) {
            assertRegistered(serviceTypeClass);
            ServiceRegistration<T> serviceRegistration = selectServiceRegistration(serviceTypeClass, serviceFilter);
            if (serviceRegistration == null) {
                throw new DeployException("No service available!");
            }
            return serviceRegistration.getServiceInstance();
        }
    }

    @Override
    public Collection<Class<?>> getRegisteredServiceTypes() {
        synchronized (lock) {
            return new LinkedHashSet(serviceTypes.keySet());
        }
    }

    @Override
    public <T> ServiceDeploymentStrategy<T> getServiceDeploymentStrategy(Class<T> serviceTypeClass) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        synchronized (lock) {
            assertRegistered(serviceTypeClass);
            return serviceTypes.get(serviceTypeClass);
        }
    }

    @Override
    public <T> Collection<ServiceRegistration<T>> getServiceRegistrations(Class<T> serviceTypeClass) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        synchronized (lock) {
            assertRegistered(serviceTypeClass);
            return new LinkedHashSet(services.get(serviceTypeClass));
        }
    }

    @Override
    public <T> boolean isRegisteredServiceType(Class<T> serviceTypeClass) {
        AssertUtil.assertNotNull(serviceTypeClass);
        synchronized (lock) {
            return serviceTypes.containsKey(serviceTypeClass);
        }
    }

    @Override
    public <T> void registerServiceType(Class<T> serviceTypeClass, ServiceDeploymentStrategy<T> deploymentStrategy) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        AssertUtil.assertNotNull(deploymentStrategy);
        synchronized (lock) {
            if (serviceTypes.containsKey(serviceTypeClass)) {
                throw new DeployException("Already serviceType defined for: " + serviceTypeClass.getName());
            }
            serviceTypes.put(serviceTypeClass, deploymentStrategy);
            services.put(serviceTypeClass, new LinkedList<>());
        }
    }

    @Override
    public <T> void unregisterServiceType(Class<T> serviceTypeClass) throws DeployException {
        AssertUtil.assertNotNull(serviceTypeClass);
        synchronized (lock) {
            assertRegistered(serviceTypeClass);
            if (!serviceTypes.containsKey(serviceTypeClass)) {
                throw new DeployException("No such serviceType registered: " + serviceTypeClass.getName());
            }
            services.remove(serviceTypeClass);
            serviceTypes.remove(serviceTypeClass);
        }
    }

    private <T> ServiceRegistration<T> selectServiceRegistration(Class<T> serviceTypeClass, ServiceFilter serviceFilter) throws DeployException {
        return getServiceDeploymentStrategy(serviceTypeClass).selectService(serviceTypeClass, (List) services.get(serviceTypeClass), serviceFilter);
    }

    private <T> void assertRegistered(Class<T> serviceTypeClass) throws DeployException {
        if (!isRegisteredServiceType(serviceTypeClass)) {
            throw new DeployException("Not registered serviceType: " + serviceTypeClass.getName());
        }
    }

}
