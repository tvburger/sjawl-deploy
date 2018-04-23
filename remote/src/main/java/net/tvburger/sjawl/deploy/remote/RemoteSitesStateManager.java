package net.tvburger.sjawl.deploy.remote;

import net.tvburger.sjawl.deploy.DeployException;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

public interface RemoteSitesStateManager<A extends Address> {

    interface Listener<A extends Address> {

        void siteAdded(RemoteSitesStateManager<A> stateManager, UUID siteId, A address);

        void siteAddressUpdated(RemoteSitesStateManager<A> stateManager, UUID siteId, A updatedAddress);

        void siteRemoved(RemoteSitesStateManager<A> stateManager, UUID siteId);

    }

    void addListener(Listener<A> listener);

    void removeListener(Listener<A> listener);

    Object getLock() throws DeployException, IOException;

    A getSiteAddress(UUID siteId) throws DeployException, IOException;

    Collection<UUID> getSiteIds() throws DeployException, IOException;

    void addSite(UUID siteId, Address address) throws DeployException, IOException;

    void updateSiteAddress(UUID siteId, Address newAddress) throws DeployException, IOException;

    void removeSite(UUID siteId) throws DeployException, IOException;

}
