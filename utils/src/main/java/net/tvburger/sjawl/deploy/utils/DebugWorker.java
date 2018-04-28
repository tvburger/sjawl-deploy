package net.tvburger.sjawl.deploy.utils;

import net.tvburger.sjawl.deploy.DeployException;

public abstract class DebugWorker extends ManagedWorker {

    protected void activate() throws DeployException {
        System.out.println("activate: " + this);
    }

    protected void started() throws DeployException {
        System.out.println("started: " + this);
    }

    protected void deactivate() throws DeployException {
        System.out.println("deactivate: " + this);
    }

    protected void stopped() throws DeployException {
        System.out.println("stopped: " + this);
    }

    protected void interrupted(InterruptedException source) throws DeployException {
        System.out.println("interrupted: " + this);
    }

}
