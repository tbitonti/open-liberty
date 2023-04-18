package cache.dummy;

import java.util.Set;

import com.ibm.websphere.cache.CacheEntry;
import com.ibm.websphere.cache.EntryInfo;
import com.ibm.wsspi.cache.CacheStatistics;
import com.ibm.wsspi.cache.CoreCache;
import com.ibm.wsspi.cache.EventSource;

public class CoreCacheImpl implements CoreCache {

    @Override
    public void clear() {
    // TODO Auto-generated method stub

    }

    @Override
    public boolean containsCacheId(Object key) {
        if (((String) key).equals("bazinga"))
            return true;
        else
            return false;
    }

    @Override
    public CacheEntry get(Object arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Object> getCacheIds() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Object> getCacheIds(Object arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getCacheName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CacheStatistics getCacheStatistics() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Object> getDependencyIds() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Object> getTemplateIds() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void invalidate(Object arg0, boolean arg1) {
    // TODO Auto-generated method stub

    }

    @Override
    public void invalidateByCacheId(Object arg0, boolean arg1) {
    // TODO Auto-generated method stub

    }

    @Override
    public void invalidateByDependency(Object arg0, boolean arg1) {
    // TODO Auto-generated method stub

    }

    @Override
    public void invalidateByTemplate(String arg0, boolean arg1) {
    // TODO Auto-generated method stub

    }

    @Override
    public CacheEntry put(EntryInfo arg0, Object arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void refreshEntry(Object arg0) {
    // TODO Auto-generated method stub

    }

    @Override
    public void setEventSource(EventSource arg0) {
    // TODO Auto-generated method stub

    }

    @Override
    public void start() {
    // TODO Auto-generated method stub

    }

    @Override
    public void stop() {
    // TODO Auto-generated method stub

    }

    @Override
    public void touch(Object arg0, long arg1, long arg2) {
    // TODO Auto-generated method stub

    }

}
