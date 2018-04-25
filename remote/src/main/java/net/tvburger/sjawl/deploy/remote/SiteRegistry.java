package net.tvburger.sjawl.deploy.remote;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.remote.protocol.Address;

import java.util.Collection;
import java.util.UUID;

public interface SiteRegistry<A extends Address> {

    Collection<UUID> getSiteIds() throws DeployException;

    A getAddress(UUID siteId) throws DeployException;

    UUID getSiteId(A address) throws DeployException;

    boolean hasSite(UUID siteId);

    boolean hasSite(A address);

}
