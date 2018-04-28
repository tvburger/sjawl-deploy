package net.tvburger.sjawl.deploy.distributed;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.distributed.protocol.Address;

import java.util.Collection;
import java.util.UUID;

public interface SiteRegistry<A extends Address> {

    Collection<UUID> getSiteIds() throws DeployException;

    A getAddress(UUID siteId) throws DeployException;

    UUID getSiteId(A address) throws DeployException;

    boolean hasSite(UUID siteId) throws DeployException;

    boolean hasSite(A address) throws DeployException;

}
