package net.tvburger.sjawl.deploy.remote.service;

import net.tvburger.sjawl.deploy.DeployException;

import java.rmi.Remote;
import java.util.UUID;

public interface ServiceProxyFactory {

    <R extends Remote> R createServiceProxy(Class<R> serviceTypeClass, UUID siteId, UUID serviceId) throws DeployException;

}
