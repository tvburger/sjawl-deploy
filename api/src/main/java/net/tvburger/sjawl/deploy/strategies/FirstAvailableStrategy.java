package net.tvburger.sjawl.deploy.strategies;

import net.tvburger.sjawl.deploy.ServiceFilter;
import net.tvburger.sjawl.deploy.admin.ServiceRegistration;

import java.util.Collection;

public final class FirstAvailableStrategy<T> extends AbstractServiceDeploymentStrategy<T> {

    @Override
    protected ServiceRegistration<T> doSelectService(Class<T> serviceTypeClass, Collection<ServiceRegistration<T>> matchingServiceRegistrations, ServiceFilter serviceFilter) {
        return matchingServiceRegistrations.iterator().next();
    }

}
