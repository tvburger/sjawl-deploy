package net.tvburger.sjawl.deploy.protocol.client;

import net.tvburger.sjawl.deploy.remote.Address;

import java.io.IOException;

public interface SiteConnectionProvider<A extends Address> {

    SiteConnection<A> getSiteConnection(A address) throws IOException;

    void resetConnection(SiteConnection<A> connection) throws IOException;

}
