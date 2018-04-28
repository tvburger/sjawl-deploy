package net.tvburger.sjawl.deploy.distributed;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.distributed.protocol.Address;

import java.util.UUID;

public interface SiteRegistrationManager<A extends Address> {

    void registerSite(UUID mySiteId, A address) throws DeployException;

    void unregisterSite(UUID mySiteId) throws DeployException;

}
