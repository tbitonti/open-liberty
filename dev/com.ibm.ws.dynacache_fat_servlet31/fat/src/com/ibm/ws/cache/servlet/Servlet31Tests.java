/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WLP Copyright IBM Corp. 2014
 *
 * The source code for this program is not published or otherwise divested 
 * of its trade secrets, irrespective of what has been deposited with the 
 * U.S. Copyright Office.
 */
package com.ibm.ws.cache.servlet;

import static junit.framework.Assert.assertEquals;

import org.junit.ClassRule;
import org.junit.Test;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

/**
 *
 */
public class Servlet31Tests extends ServletTestCase {
    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("Servlet31Tests");

    public Servlet31Tests() {
        super();
    }

    @Test
    public void testSetContentLengthLong() throws Exception {
        String URI = "/dynacachetests/servlet31?action=setContentLengthLong";
        WebConversation wc = startNewConversation();
        //send 2 successive requests.  should get a miss and a hit
        WebResponse resp1 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp1), resp1.getResponseCode(), 200);

        String ts1 = resp1.getTableWithID("TimeStamp").getCellAsText(0, 0);

        WebResponse resp2 = getWebResponse(wc, URI);
        assertEquals(msg("Response code was not OK", resp2), resp2.getResponseCode(), 200);
        String ts2 = resp2.getTableWithID("TimeStamp").getCellAsText(0, 0);
        assertEquals(msg("TimeStamps were not equal, response was not cached.", resp1), ts1, ts2);
    }

}
