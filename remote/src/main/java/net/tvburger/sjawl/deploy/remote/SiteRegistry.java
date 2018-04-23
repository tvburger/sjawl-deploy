package net.tvburger.sjawl.deploy.remote;

import net.tvburger.sjawl.deploy.DeployException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SiteRegistry<A extends Address> {

    private class Listener implements RemoteSitesStateManager.Listener<A> {

        @Override
        public void siteAdded(RemoteSitesStateManager<A> stateManager, UUID siteId, A address) {
            synchronized (lock) {
                addresses.put(siteId, address);
                siteIds.put(address, siteId);
            }
        }

        @Override
        public void siteAddressUpdated(RemoteSitesStateManager<A> stateManager, UUID siteId, A updatedAddress) {
            synchronized (lock) {
                addresses.put(siteId, updatedAddress);
                siteIds.put(updatedAddress, siteId);
            }
        }

        @Override
        public void siteRemoved(RemoteSitesStateManager<A> stateManager, UUID siteId) {
            synchronized (lock) {
                siteIds.remove(addresses.remove(siteId));
            }
        }

    }

    private final Listener listener = new Listener();
    private final RemoteSitesStateManager<A> stateManager;
    private final Object lock = new Object();
    private final Map<UUID, A> addresses = new HashMap<>();
    private final Map<A, UUID> siteIds = new HashMap<>();

    public SiteRegistry(RemoteSitesStateManager<A> stateManager) {
        this.stateManager = stateManager;
    }

    public void init() throws DeployException {
        try {
            synchronized (stateManager.getLock()) {
                for (UUID siteId : stateManager.getSiteIds()) {
                    addresses.put(siteId, stateManager.getSiteAddress(siteId));
                }
                stateManager.addListener(listener);
            }
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    public A getAddress(UUID siteId) throws DeployException {
        synchronized (lock) {
            if (!addresses.containsKey(siteId)) {
                throw new DeployException("No such site!");
            }
            return addresses.get(siteId);
        }
    }

    public UUID getSiteId(A address) throws DeployException {
        synchronized (lock) {
            if (!siteIds.containsKey(address)) {
                throw new DeployException("No such site!");
            }
            return siteIds.get(address);
        }
    }

    public boolean hasSite(UUID siteId) {
        synchronized (lock) {
            return addresses.containsKey(siteId);
        }
    }

    public boolean hasSite(A address) {
        synchronized (lock) {
            return siteIds.containsKey(address);
        }
    }

}
