package net.tvburger.sjawl.deploy.utils;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.WorkerActivator;
import net.tvburger.sjawl.deploy.WorkerDeployer;

public abstract class ManagedWorker implements Runnable {

    public static class Activator<T extends ManagedWorker> implements WorkerActivator<T> {

        public static class Singleton {

            private static final Activator<?> INSTANCE = new Activator();

            @SuppressWarnings("unchecked")
            public static <T extends ManagedWorker> Activator<T> get() {
                return (Activator<T>) INSTANCE;
            }

            private Singleton() {
            }

        }

        @Override
        public boolean isActive(ManagedWorker workerInstance) {
            synchronized (workerInstance) {
                return workerInstance.workerThread != null;
            }
        }

        @Override
        public void activateWorker(ManagedWorker workerInstance, WorkerDeployer.WorkerFinishedCallback callback) throws DeployException {
            synchronized (workerInstance) {
                if (isActive(workerInstance)) {
                    throw new DeployException("Already active!");
                }
                workerInstance.callback = callback;
                workerInstance.workerThread = new Thread(workerInstance);
                workerInstance.workerThread.start();
                workerInstance.activate();
            }
        }

        @Override
        public void deactivateWorker(ManagedWorker workerInstance) throws DeployException {
            synchronized (workerInstance) {
                if (!isActive(workerInstance)) {
                    throw new DeployException("Not active!");
                }
                Thread workerThread = workerInstance.workerThread;
                workerInstance.workerThread = null;
                workerThread.interrupt();
                workerInstance.notifyAll();
                workerInstance.deactivate();
            }
        }

    }

    private Thread workerThread = null;
    private WorkerDeployer.WorkerFinishedCallback callback;

    @Override
    public void run() {
        try {
            started();
            while (isActive()) {
                try {
                    performOneWorkUnit();
                } catch (InterruptedException cause) {
                    interrupted(cause);
                }
            }
            stopped();
        } catch (DeployException cause) {
        } finally {
            invokeCallback();
        }
    }

    protected final boolean isActive() {
        synchronized (this) {
            return workerThread != null;
        }
    }

    protected final void invokeCallback() {
        try {
            synchronized (this) {
                callback.workerFinished();
            }
        } catch (DeployException cause) {
        }
    }

    protected void activate() throws DeployException {
    }

    protected void started() throws DeployException {
    }

    protected void deactivate() throws DeployException {
    }

    protected void stopped() throws DeployException {
    }

    protected void interrupted(InterruptedException source) throws DeployException {
    }

    protected abstract void performOneWorkUnit() throws InterruptedException;

}
