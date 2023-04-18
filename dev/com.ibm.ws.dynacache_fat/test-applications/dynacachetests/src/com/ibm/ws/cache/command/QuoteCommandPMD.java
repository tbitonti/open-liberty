// IBM Confidential OCO Source Material
// 5639-D57 (C) COPYRIGHT International Business Machines Corp. 1997, 2002
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.

package com.ibm.ws.cache.command;

import java.util.Random;

import com.ibm.websphere.cache.EntryInfo;
import com.ibm.websphere.servlet.cache.DynamicCacheAccessor;

//QuoteCommand that implements prepareMetaData

public class QuoteCommandPMD extends QuoteCommand {
    static final long serialVersionUID = -5573075254048153742L;

    static protected Random rand = new Random();

    static {
        try {
            DynamicCacheAccessor.getCache().invalidateByTemplate(QuoteCommandPMD.class.getName(), true);
        } catch (Exception ex) {
        }
    }

    @Override
    protected void prepareMetadata() {
        String commandName = QuoteCommand.class.getName();
        int sharingPolicy = EntryInfo.NOT_SHARED;
        EntryInfo entryInfo = super.getEntryInfo();
        entryInfo.setId(commandName + "?" + ticker);
        entryInfo.setSharingPolicy(sharingPolicy);
        entryInfo.addDataId("ticker:" + ticker);
        entryInfo.setTimeLimit(60); //expire in 60 seconds
    }

}
