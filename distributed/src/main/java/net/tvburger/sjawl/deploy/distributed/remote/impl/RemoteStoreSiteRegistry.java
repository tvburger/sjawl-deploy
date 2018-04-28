package net.tvburger.sjawl.deploy.distributed.remote.impl;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.distributed.SiteRegistrationManager;
import net.tvburger.sjawl.deploy.distributed.SiteRegistry;
import net.tvburger.sjawl.deploy.distributed.protocol.Address;
import net.tvburger.sjawl.deploy.distributed.remote.RemoteSitesStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

// TODO: remove syncs when events from store are provided
public final class RemoteStoreSiteRegistry<A extends Address> implements SiteRegistry<A>, SiteRegistrationManager<A> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteStoreSiteRegistry.class);

    private class Listener implements RemoteSitesStore.Listener<A> {

        @Override
        public void siteAdded(RemoteSitesStore<A> stateManager, UUID siteId, A address) {
            synchronized (lock) {
                addresses.put(siteId, address);
                siteIds.put(address, siteId);
            }
        }

        @Override
        public void siteAddressUpdated(RemoteSitesStore<A> stateManager, UUID siteId, A updatedAddress) {
            synchronized (lock) {
                addresses.put(siteId, updatedAddress);
                siteIds.put(updatedAddress, siteId);
            }
        }

        @Override
        public void siteRemoved(RemoteSitesStore<A> stateManager, UUID siteId) {
            synchronized (lock) {
                siteIds.remove(addresses.remove(siteId));
            }
        }

    }

    private final Listener listener = new Listener();
    private final RemoteSitesStore<A> stateManager;
    private final Object lock = new Object();
    private final Map<UUID, A> addresses = new HashMap<>();
    private final Map<A, UUID> siteIds = new HashMap<>();

    public RemoteStoreSiteRegistry(RemoteSitesStore<A> stateManager) {
        this.stateManager = stateManager;
    }

    public void init() throws DeployException {
        try {
            synchronized (stateManager.getLock()) {
                sync();
                stateManager.addListener(listener);
            }
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    private void sync() throws DeployException {
        LOGGER.debug("syncing...");
        try {
            Collection<UUID> remoteSiteIds = stateManager.getSiteIds();
            for (UUID remoteSiteId : remoteSiteIds) {
                if (!addresses.containsKey(remoteSiteId)) {
                    LOGGER.info("Found new site: " + remoteSiteId);
                    A address = stateManager.getSiteAddress(remoteSiteId);
                    LOGGER.info(String.format("Site %s has address: %s", remoteSiteId, address));
                    addresses.put(remoteSiteId, address);
                    siteIds.put(address, remoteSiteId);
                }
            }
            for (UUID localSiteId : addresses.keySet()) {
                if (!remoteSiteIds.contains(localSiteId)) {
                    LOGGER.info("Found old site: " + localSiteId);
                    A address = addresses.remove(localSiteId);
                    siteIds.remove(address);
                    LOGGER.info("Site removed: " + localSiteId);
                }
            }
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    @Override
    public void registerSite(UUID mySiteId, A myAddress) throws DeployException {
        try {
            synchronized (stateManager.getLock()) {
                stateManager.addSite(mySiteId, myAddress);
                addresses.put(mySiteId, myAddress);
                siteIds.put(myAddress, mySiteId);
            }
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    @Override
    public void unregisterSite(UUID mySiteId) throws DeployException {
        try {
            synchronized (stateManager.getLock()) {
                stateManager.removeSite(mySiteId);
                siteIds.remove(addresses.remove(mySiteId));
            }
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    @Override
    public Collection<UUID> getSiteIds() throws DeployException {
        synchronized (lock) {
            sync();
            return new HashSet<>(addresses.keySet());
        }
    }

    @Override
    public A getAddress(UUID siteId) throws DeployException {
        synchronized (lock) {
            sync();
            if (!addresses.containsKey(siteId)) {
                throw new DeployException("No such site!");
            }
            return addresses.get(siteId);
        }
    }

    @Override
    public UUID getSiteId(A address) throws DeployException {
        synchronized (lock) {
            sync();
            if (!siteIds.containsKey(address)) {
                throw new DeployException("No such site!");
            }
            return siteIds.get(address);
        }
    }

    @Override
    public boolean hasSite(UUID siteId) throws DeployException {
        synchronized (lock) {
            sync();
            return addresses.containsKey(siteId);
        }
    }

    @Override
    public boolean hasSite(A address) throws DeployException {
        synchronized (lock) {
            sync();
            return siteIds.containsKey(address);
        }
    }

}
