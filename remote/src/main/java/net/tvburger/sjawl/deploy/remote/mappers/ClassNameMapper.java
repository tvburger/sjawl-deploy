package net.tvburger.sjawl.deploy.remote.mappers;

import net.tvburger.sjawl.common.AssertUtil;
import net.tvburger.sjawl.common.Cache;
import net.tvburger.sjawl.deploy.DeployException;

public final class ClassNameMapper {

    private final Cache<String, Class<?>> cache;

    public ClassNameMapper(Cache<String, Class<?>> cache) {
        this.cache = cache;
    }

    public String toName(Class<?> aClass) {
        AssertUtil.assertNotNull(aClass);
        return aClass.getName();
    }

    public Class<?> toClass(String name) throws DeployException {
        Class<?> aClass;
        if (!cache.has(name)) {
            aClass = loadClass(name);
            cache.put(name, aClass);
        } else {
            aClass = cache.get(name);
        }
        return aClass;
    }

    private Class<?> loadClass(String name) throws DeployException {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException cause) {
            throw new DeployException("Failed to load class: " + cause.getMessage(), cause);
        }
    }

}
