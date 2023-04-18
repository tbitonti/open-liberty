package cache.dummy;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import com.ibm.wsspi.cache.CacheConfig;
import com.ibm.wsspi.cache.CacheFeatureSupport;
import com.ibm.wsspi.cache.CacheProvider;
import com.ibm.wsspi.cache.CoreCache;

@Component(service = CacheProvider.class, property = { "name=dummy", "service.vendor=IBM" })
public class DummyCacheProvider implements CacheProvider {
    private static final String name = "dummy";
    CacheFeatureSupport cfs = null;
    CoreCache cache = null;

    public DummyCacheProvider() {
        System.out.println("DUMMY cache constructor called");
        cfs = new CacheFeatureSupportImpl();
    }

    @Override
    public CoreCache createCache(CacheConfig arg0) {
        System.out.println("DUMMY createCache called");
        synchronized (this) {
            if (cache == null)
                cache = new CoreCacheImpl();
        }
        return cache;

    }

    @Override
    public CacheFeatureSupport getCacheFeatureSupport() {
        System.out.println("DUMMY getCacheFeatureSupport called");

        return cfs;
    }

    @Override
    public String getName() {
        System.out.println("DUMMY getName called");
        return name;
    }

    @Activate
    @Override
    public void start() {
        System.out.println("DUMMY start called");
    }

    @Deactivate
    @Override
    public void stop() {
        System.out.println("DUMMY stop called");
    }

}
