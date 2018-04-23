package net.tvburger.sjawl.deploy.local.worker;

import net.tvburger.sjawl.deploy.DeployException;
import net.tvburger.sjawl.deploy.worker.WorkerActivator;
import net.tvburger.sjawl.deploy.worker.WorkerDeployer;

public abstract class AbstractWorker implements Runnable {

    public static class Activator<T extends AbstractWorker> implements WorkerActivator<T> {

        public static class Singleton {

            private static final Activator<?> INSTANCE = new Activator();

            @SuppressWarnings("unchecked")
            public static <T extends AbstractWorker> Activator<T> get() {
                return (Activator<T>) INSTANCE;
            }

            private Singleton() {
            }

        }

        @Override
        public boolean isActive(AbstractWorker workerInstance) {
            synchronized (workerInstance) {
                return workerInstance.workerThread != null;
            }
        }

        @Override
        public void activateWorker(AbstractWorker workerInstance, WorkerDeployer.WorkerFinishedCallback callback) throws DeployException {
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
        public void deactivateWorker(AbstractWorker workerInstance) throws DeployException {
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

    protected abstract void performOneWorkUnit() throws InterruptedException;

}
