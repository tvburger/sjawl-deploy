package net.tvburger.sjawl.deploy.strategies;

import net.tvburger.sjawl.deploy.ServiceDeploymentStrategy;
import net.tvburger.sjawl.deploy.ServiceFilter;
import net.tvburger.sjawl.deploy.admin.ServiceRegistration;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractServiceDeploymentStrategy<T> implements ServiceDeploymentStrategy<T> {

    @Override
    public final ServiceRegistration<T> selectService(Class<T> serviceTypeClass, Collection<ServiceRegistration<T>> serviceRegistrations, ServiceFilter serviceFilter) {
        ServiceRegistration<T> serviceRegistration;
        Collection<ServiceRegistration<T>> matchingServiceRegistrations = filterServiceRegistration(serviceRegistrations, serviceFilter);
        switch (matchingServiceRegistrations.size()) {
            case 0:
                serviceRegistration = null;
                break;
            case 1:
                serviceRegistration = matchingServiceRegistrations.iterator().next();
                break;
            default:
                serviceRegistration = doSelectService(serviceTypeClass, matchingServiceRegistrations, serviceFilter);
        }
        return serviceRegistration;
    }

    private Collection<ServiceRegistration<T>> filterServiceRegistration(Collection<ServiceRegistration<T>> serviceRegistrations, ServiceFilter serviceFilter) {
        if (serviceFilter == null) {
            return serviceRegistrations;
        }
        List<ServiceRegistration<T>> matchingServiceRegistrations = new LinkedList<>();
        for (ServiceRegistration<T> serviceRegistration : serviceRegistrations) {
            if (serviceFilter.matches(serviceRegistration.getServiceProperties())) {
                matchingServiceRegistrations.add(serviceRegistration);
            }
        }
        return matchingServiceRegistrations;
    }

    protected abstract ServiceRegistration<T> doSelectService(Class<T> serviceTypeClass, Collection<ServiceRegistration<T>> matchingServiceRegistrations, ServiceFilter serviceFilter);

}
