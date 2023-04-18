// IBM Confidential OCO Source Material
// 5639-D57 (C) COPYRIGHT International Business Machines Corp. 1997, 2002
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.

package com.ibm.ws.cache.command;

import java.util.ArrayList;

import com.ibm.websphere.command.CacheableCommand;
import com.ibm.websphere.command.CommandIdGenerator;

public class QuoteIdGenerator implements CommandIdGenerator {

    @Override
    public String getId(CacheableCommand command, ArrayList groupIds) {
        QuoteCommand cs = (QuoteCommand) command;
        // add dependency ids for quotecommand the ticker for this command
        groupIds.add("QuoteCommandIDGen");
        groupIds.add("ticker:" + cs.getTicker());
        return "QuoteCommmandIdGen Ticker:" + cs.getTicker();
    }
}
