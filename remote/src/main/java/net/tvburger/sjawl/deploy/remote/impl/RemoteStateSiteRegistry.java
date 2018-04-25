package net.tvburger.sjawl.deploy.remote.impl;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.remote.RemoteSitesStore;
import net.tvburger.sjawl.deploy.remote.SiteRegistry;
import net.tvburger.sjawl.deploy.remote.protocol.Address;

import java.io.IOException;
import java.util.*;

public final class RemoteStateSiteRegistry<A extends Address> implements SiteRegistry<A> {

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

    public RemoteStateSiteRegistry(RemoteSitesStore<A> stateManager) {
        this.stateManager = stateManager;
    }

    public void init(UUID mySiteId, A myAddress) throws DeployException {
        try {
            synchronized (stateManager.getLock()) {
                for (UUID siteId : stateManager.getSiteIds()) {
                    addresses.put(siteId, stateManager.getSiteAddress(siteId));
                }
                if (!addresses.containsKey(mySiteId)) {
                    stateManager.addSite(mySiteId, myAddress);
                    addresses.put(mySiteId, myAddress);
                    siteIds.put(myAddress, mySiteId);
                }
                stateManager.addListener(listener);
            }
        } catch (IOException cause) {
            throw new DeployException(cause);
        }
    }

    @Override
    public Collection<UUID> getSiteIds() throws DeployException {
        synchronized (lock) {
            return new HashSet<>(addresses.keySet());
        }
    }

    @Override
    public A getAddress(UUID siteId) throws DeployException {
        synchronized (lock) {
            if (!addresses.containsKey(siteId)) {
                throw new DeployException("No such site!");
            }
            return addresses.get(siteId);
        }
    }

    @Override
    public UUID getSiteId(A address) throws DeployException {
        synchronized (lock) {
            if (!siteIds.containsKey(address)) {
                throw new DeployException("No such site!");
            }
            return siteIds.get(address);
        }
    }

    @Override
    public boolean hasSite(UUID siteId) {
        synchronized (lock) {
            return addresses.containsKey(siteId);
        }
    }

    @Override
    public boolean hasSite(A address) {
        synchronized (lock) {
            return siteIds.containsKey(address);
        }
    }

}