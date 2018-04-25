package net.tvburger.sjawl.deploy.remote;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.remote.protocol.Address;
import net.tvburger.sjawl.deploy.remote.protocol.AddressSerializer;

public interface RemoteProvider<A extends Address> {

    interface Factory {

        <A extends Address> RemoteProvider<A> create(AddressSerializer<A> addressSerializer) throws DeployException;

    }

    RemoteSitesStore<A> getRemoteSitesStore();

    RemoteServicesStore getRemoteServicesStore();

}
