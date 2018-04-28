package net.tvburger.sjawl.deploy.distributed.spi;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.DeploymentContext;
import net.tvburger.sjawl.deploy.distributed.SiteRegistry;
import net.tvburger.sjawl.deploy.distributed.protocol.Address;
import net.tvburger.sjawl.deploy.distributed.protocol.AddressSerializer;
import net.tvburger.sjawl.deploy.local.LocalServicesStore;

import java.util.UUID;

public interface ProtocolProvider<A extends Address> {

    interface Factory<A extends Address> {

        ProtocolProvider<A> create(RemoteStoreProvider<A> remoteStoreProvider) throws DeployException;

        AddressSerializer<A> getAddressSerializer() throws DeployException;

    }

    UUID getSiteId();

    A getAddress();

    Class<A> getAddressTypeClass();

    AddressSerializer<A> getAddressSerializer();

    SiteRegistry<A> getSiteRegistry();

    LocalServicesStore getLocalServicesStore();

    DeploymentContext getServerContext();

}
