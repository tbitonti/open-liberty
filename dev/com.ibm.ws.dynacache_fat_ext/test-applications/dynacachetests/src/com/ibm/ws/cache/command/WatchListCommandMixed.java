// IBM Confidential OCO Source Material
// 5639-D57 (C) COPYRIGHT International Business Machines Corp. 1997, 2002
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.

package com.ibm.ws.cache.command;

import java.io.Serializable;

import com.ibm.websphere.servlet.cache.DynamicCacheAccessor;


public class WatchListCommandMixed extends WatchListCommand implements Serializable
{
    static final long serialVersionUID = -5573075254048153742L;

    static {
       try {
          DynamicCacheAccessor.getCache().invalidateByTemplate(WatchListCommandMixed.class.getName(),true);
       } catch (Exception ex) {
       }
    }
}
