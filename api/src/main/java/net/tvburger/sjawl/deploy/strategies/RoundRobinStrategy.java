package net.tvburger.sjawl.deploy.strategies;

import net.tvburger.sjawl.deploy.service.ServiceFilter;
import net.tvburger.sjawl.deploy.service.ServiceRegistration;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public final class RoundRobinStrategy<T> extends AbstractServiceDeploymentStrategy<T> {

    private final AtomicInteger count = new AtomicInteger();

    @Override
    protected ServiceRegistration<T> doSelectService(Class<T> serviceTypeClass, Collection<ServiceRegistration<T>> matchingServiceRegistrations, ServiceFilter serviceFilter) {
        int times = count.getAndIncrement() % matchingServiceRegistrations.size();
        Iterator<ServiceRegistration<T>> iterator = matchingServiceRegistrations.iterator();
        for (int i = 0; i < times; i++) {
            iterator.next();
        }
        return iterator.next();
    }

}
