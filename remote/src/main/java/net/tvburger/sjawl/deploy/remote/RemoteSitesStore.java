package net.tvburger.sjawl.deploy.remote;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.remote.protocol.Address;
import net.tvburger.sjawl.deploy.remote.protocol.AddressSerializer;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

public interface RemoteSitesStore<A extends Address> {

    interface Factory {

        <A extends Address> RemoteSitesStore<A> create(AddressSerializer<A> addressSerializer);

    }

    interface Listener<A extends Address> {

        void siteAdded(RemoteSitesStore<A> stateManager, UUID siteId, A address);

        void siteAddressUpdated(RemoteSitesStore<A> stateManager, UUID siteId, A updatedAddress);

        void siteRemoved(RemoteSitesStore<A> stateManager, UUID siteId);

    }

    void addListener(Listener<A> listener);

    void removeListener(Listener<A> listener);

    Object getLock() throws DeployException, IOException;

    A getSiteAddress(UUID siteId) throws DeployException, IOException;

    Collection<UUID> getSiteIds() throws DeployException, IOException;

    void addSite(UUID siteId, A address) throws DeployException, IOException;

    void updateSiteAddress(UUID siteId, A newAddress) throws DeployException, IOException;

    void removeSite(UUID siteId) throws DeployException, IOException;

}
