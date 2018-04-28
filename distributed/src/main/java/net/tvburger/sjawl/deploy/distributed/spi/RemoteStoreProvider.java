package net.tvburger.sjawl.deploy.distributed.spi;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.distributed.remote.RemoteServicesStore;
import net.tvburger.sjawl.deploy.distributed.remote.RemoteSitesStore;
import net.tvburger.sjawl.deploy.distributed.protocol.Address;
import net.tvburger.sjawl.deploy.distributed.protocol.AddressSerializer;

public interface RemoteStoreProvider<A extends Address> {

    interface Factory {

        <A extends Address> RemoteStoreProvider<A> create(String deploymentId, AddressSerializer<A> addressSerializer) throws DeployException;

    }

    RemoteSitesStore<A> getRemoteSitesStore();

    RemoteServicesStore getRemoteServicesStore();

}
