package net.tvburger.sjawl.deploy.remote.protocol;

import net.tvburger.sjawl.deploy.DeployException;

import java.util.UUID;

public interface ServiceProxyFactory {

    <T> T createServiceProxy(Class<T> serviceTypeClass, UUID siteId, UUID serviceId) throws DeployException;

}
