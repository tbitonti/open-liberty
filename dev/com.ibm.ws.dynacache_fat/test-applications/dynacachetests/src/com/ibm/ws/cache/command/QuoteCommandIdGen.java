// IBM Confidential OCO Source Material
// 5639-D57 (C) COPYRIGHT International Business Machines Corp. 1997, 2002
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.

package com.ibm.ws.cache.command;

import com.ibm.websphere.servlet.cache.DynamicCacheAccessor;

//QuoteCommand that uses custom id generator


public class QuoteCommandIdGen extends QuoteCommand
{   Object o = null;

    static final long serialVersionUID = -5573075254048153742L;
    
   public void setObject(Object o)  //TD begin
    {
        this.o=o;
    }//TD end

    static {
       try {
          DynamicCacheAccessor.getCache().invalidateByTemplate(QuoteCommandIdGen.class.getName(),true);
       } catch (Exception ex) {
       }
    }
}
