package com.ibm.ws.cache.servlet;

import com.ibm.websphere.cache.PreInvalidationListener;

public class PreInvalidationListenerImpl implements PreInvalidationListener  {

	public boolean shouldInvalidate (Object id, int invalidationSource, int invalidationCause) {
		return false;
		/*
		if (invalidationCause == PreInvalidationListener.CLEAR_ALL )
			return true;
		else return false;
		*/
	}
}
