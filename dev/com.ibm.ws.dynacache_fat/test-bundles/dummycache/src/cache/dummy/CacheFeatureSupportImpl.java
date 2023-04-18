package cache.dummy;

import com.ibm.wsspi.cache.CacheFeatureSupport;

public class CacheFeatureSupportImpl extends CacheFeatureSupport {

	@Override
	public boolean isAliasSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDiskCacheSupported() {

		return true;
	}

	@Override
	public boolean isReplicationSupported() {
		// TODO Auto-generated method stub
		return false;
	}

}
