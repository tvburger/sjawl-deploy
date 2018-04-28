package net.tvburger.sjawl.deploy.distributed.mappers;

import net.tvburger.sjawl.common.AssertUtil;
import net.tvburger.sjawl.common.Cache;
import net.tvburger.sjawl.deploy.DeployException;

public final class InstanceNameMapper<T> {

    private final Class<T> instanceTypeClass;
    private final Cache<String, T> cache;

    public InstanceNameMapper(Class<T> instanceTypeClass, Cache<String, T> cache) {
        this.instanceTypeClass = instanceTypeClass;
        this.cache = cache;
    }

    public String toName(T instance) {
        AssertUtil.assertNotNull(instance);
        return instance.getClass().getName();
    }

    public T toInstance(String name) throws DeployException {
        T instance;
        if (!cache.has(name)) {
            instance = loadInstance(name);
            cache.put(name, instance);
        } else {
            instance = cache.get(name);
        }
        return instance;
    }

    private T loadInstance(String name) throws DeployException {
        try {
            return instanceTypeClass.cast(Class.forName(name).newInstance());
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException cause) {
            throw new DeployException("Failed to load instance: " + cause.getMessage(), cause);
        }
    }

}
