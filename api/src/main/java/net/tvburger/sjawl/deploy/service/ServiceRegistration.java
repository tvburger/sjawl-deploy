package net.tvburger.sjawl.deploy.service;

public final class ServiceRegistration<T> {

    private final T serviceInstance;
    private final ServiceProperties serviceProperties;

    public ServiceRegistration(T serviceInstance, ServiceProperties serviceProperties) {
        this.serviceInstance = serviceInstance;
        this.serviceProperties = serviceProperties;
    }

    public T getServiceInstance() {
        return serviceInstance;
    }

    public ServiceProperties getServiceProperties() {
        return serviceProperties;
    }

}
