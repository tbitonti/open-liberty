//IBM Confidential OCO Source Material
//5639-D57, 5630-A36, 5630-A37, 5724-D18 (C) COPYRIGHT International Business
// Machines Corp. 1997, 2002
//The source code for this program is not published or otherwise divested
//of its trade secrets, irrespective of what has been deposited with the
//U.S. Copyright Office.
package com.ibm.ws.cache.cachemonitor;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.Arrays;
import java.util.StringTokenizer;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.ibm.ws.cache.servlet.SharedServer;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;

public class CachePolicies extends CacheMonitorTest {

    private final int _numServletTables = 5,
                    _numCommandTables = 5,
                    _numWebserviceTables = 5,
                    _numPortletTables = 5;
    private StringBuffer _tableContents;
    private final StringBuffer _servletInfo[] = new StringBuffer[_numServletTables],
                    _commandInfo[] = new StringBuffer[_numCommandTables],
                    _webserviceInfo[] = new StringBuffer[_numWebserviceTables],
                    _portletInfo[] = new StringBuffer[_numPortletTables];

    String tables[] = new String[] { "Cache ID Rule 0", "Cache ID Rule 1", "Dependency ID Rule 0", "Invalidation ID Rule 0", "" };

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("CachePolicies", "cachemonitor");

    @Test
    public void testCachePolicies() throws Exception {

        WebRequest req = null;
        WebResponse resp = null;
        WebResponse frame = null;
        WebTable cacheId = null;
        WebForm webForm = null;
        WebLink link = null;

        WebTable allPolicies = null;

        WebTable allIds = null;
        WebTable cacheIdRule = null;
        WebTable cacheIdComponents = null;
        WebTable dataIdRule = null;
        WebTable dataIdComponents = null;

        WebConversation wc = startNewConversation();
        System.out.println("CachePolicies Test");

        System.out.println("********* Verify Policies Page *********");
        req = new GetMethodWebRequest(url + "/selectInstance.jsp?instance=baseCache");
        resp = wc.getResponse(req);
        assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
        frame = getPoliciesFrame(wc);
        allPolicies = frame.getTableWithID("policies");

        //loop through all policies and check that the test policies display the right info
        //if a test policy is not found, it should throw an exception later when trying to click the link
        for (int i = 0; i < allPolicies.getRowCount(); i++) {
            //Servlet Policy
            if (allPolicies.getCellAsText(i, 0).equals("/dynacachetests/CacheMonitorTestServletPolicy")) {
                assertEquals(msg("Servlet policy class displays incorrectly", ""), "servlet", allPolicies.getCellAsText(i, 1));
                assertEquals(msg("Servlet policy replication displays incorrectly", ""), "Push", allPolicies.getCellAsText(i, 2).trim());
                assertTrue(msg("Servlet policy properties display incorrectly", ""), allPolicies.getCellAsText(i, 3).indexOf("externalcache = CMTextCache") != -1);
                assertTrue(msg("Servlet policy properties display incorrectly", ""), allPolicies.getCellAsText(i, 3).indexOf("edgeable = true") != -1);
            }
            //Command Policy
            if (allPolicies.getCellAsText(i, 0).equals("/CacheMonitorTestCommandPolicy.class")) {
                assertEquals(msg("Command policy class displays incorrectly", ""), "command", allPolicies.getCellAsText(i, 1));
                assertEquals(msg("Command policy replication displays incorrectly", ""), "None", allPolicies.getCellAsText(i, 2).trim());
                assertTrue(msg("Command policy properties display incorrectly", ""), allPolicies.getCellAsText(i, 3).indexOf("delay-invalidations = true") != -1);
                assertTrue(msg("Command policy properties display incorrectly", ""), allPolicies.getCellAsText(i, 3).indexOf("delay-invalidations = true") != -1);
                assertTrue(msg("Command policy properties display incorrectly", ""), allPolicies.getCellAsText(i, 3).indexOf("applicationname = CMTAppName") != -1);
                assertTrue(msg("Command policy properties display incorrectly", ""), allPolicies.getCellAsText(i, 3).indexOf("persist-to-disk = false") != -1);
            }
            //Webservice Policy
            if (allPolicies.getCellAsText(i, 0).equals("/dynacachetests/CacheMonitorTestWebservicePolicy")) {
                assertEquals(msg("Webservice policy class displays incorrectly", ""), "webservice", allPolicies.getCellAsText(i, 1));
                assertEquals(msg("Webservice policy replication displays incorrectly", ""), "Push-Pull", allPolicies.getCellAsText(i, 2).trim());
                assertTrue(msg("Webservice policy properties display incorrectly", ""), allPolicies.getCellAsText(i, 3).indexOf("consume-subfragments = true") != -1);
                assertTrue(msg("Webservice policy properties display incorrectly", ""), allPolicies.getCellAsText(i, 3).indexOf("do-not-consume = true") != -1);
            }
            //Portlet Policy
            if (allPolicies.getCellAsText(i, 0).equals("/dynacachetests/CacheMonitorTestPortletPolicy")) {
                assertEquals(msg("Portlet policy class displays incorrectly", ""), "portlet", allPolicies.getCellAsText(i, 1));
                assertEquals(msg("Portlet policy replication displays incorrectly", ""), "Pull", allPolicies.getCellAsText(i, 2).trim());
                assertTrue(msg("Portlet policy properties display incorrectly", ""), allPolicies.getCellAsText(i, 3).indexOf("alternate_url = /CMTURL") != -1);
                assertTrue(msg("Portlet policy properties display incorrectly", ""), allPolicies.getCellAsText(i, 3).indexOf("save-attributes = false") != -1);
            }
        }

        //String to hold ID of each rule in the Policies for error messages

        System.out.println("********* Verify Servlet Policy *********");
        frame = getPoliciesFrame(wc);
        link = frame.getLinkWithID("/dynacachetests/CacheMonitorTestServletPolicy");
        assertNotNull(msg("/dynacachetests/CacheMonitorTestServletPolicy policy does not exist", ""), link);
        frame = link.click();
        assertEquals(msg("Response code was not OK", frame), frame.getResponseCode(), 200);

        allIds = frame.getTableWithID("allIds");
        String policy1[][] = allIds.asText();

        //check that the right number of tables are displayed
        assertEquals(msg("Servlet Policy has incorrect number of tables", Arrays.deepToString(policy1)), _numServletTables, policy1.length);

        //loop through each table and check that it is correct
        for (int i = 0; i < policy1.length; i++) {
            for (int j = 0; j < policy1[i].length; j++) {
                _tableContents.append(policy1[i][j]);
            }

            compareCacheIDs(_servletInfo[i].toString(), stripLineBreaks(_tableContents), i);
            _tableContents.delete(0, _tableContents.length());
        }

        System.out.println("********* Verify Command Policy *********");
        frame = getPoliciesFrame(wc);
        link = frame.getLinkWithID("/CacheMonitorTestCommandPolicy.class");
        assertNotNull(msg("/CacheMonitorTestCommandPolicy.class policy does not exist", ""), link);
        frame = link.click();
        assertEquals(msg("Response code was not OK", frame), frame.getResponseCode(), 200);

        allIds = frame.getTableWithID("allIds");
        String policy2[][] = allIds.asText();

        //check that the right number of tables are displayed
        assertEquals(msg("Command Policy has incorrect number of tables", ""), _numCommandTables, policy2.length);

        //loop through each table and check that it is correct
        for (int i = 0; i < policy2.length; i++) {
            for (int j = 0; j < policy2[i].length; j++) {
                _tableContents.append(policy2[i][j]);
            }

            compareCacheIDs(_commandInfo[i].toString(), stripLineBreaks(_tableContents), i);
            _tableContents.delete(0, _tableContents.length());
        }

        System.out.println("********* Verify WebServices Policy *********");
        frame = getPoliciesFrame(wc);
        link = frame.getLinkWithID("/dynacachetests/CacheMonitorTestWebservicePolicy");
        assertNotNull(msg("/dynacachetests/CacheMonitorTestWebservicePolicy policy does not exist", ""), link);
        frame = link.click();
        assertEquals(msg("Response code was not OK", frame), frame.getResponseCode(), 200);

        allIds = frame.getTableWithID("allIds");
        String policy3[][] = allIds.asText();
        //check that the right number of tables are displayed
        assertEquals(msg("Webservice Policy has incorrect number of tables", ""), _numWebserviceTables, policy3.length);

        //loop through each table and check that it is correct
        for (int i = 0; i < policy3.length; i++) {
            for (int j = 0; j < policy3[i].length; j++) {
                _tableContents.append(policy3[i][j]);
            }
            compareCacheIDs(_webserviceInfo.toString(), stripLineBreaks(_tableContents), i);
            _tableContents.delete(0, _tableContents.length());
        }
    }

    private void compareCacheIDs(String expected, String received, int id) {

        java.util.StringTokenizer st1 = new StringTokenizer(expected, "|");
        StringTokenizer st2 = new StringTokenizer(expected, "|");

        if (st1.countTokens() != st2.countTokens()) {
            fail("Cache ID rule "
                    + id
                    + " expected and received lengths are not equal expected:"
                    + expected
                    + " received:"
                    + received);
        }

        while (st1.hasMoreTokens() && st2.hasMoreTokens()) {
            assertEquals(
                         "Part of the cacheid rule " + id + " is unmatched",
                         st1.nextToken(),
                         st2.nextToken());
        }
    }

    private String stripLineBreaks(StringBuffer sb) {

        //first strip any newline chars ('\n')
        int index = sb.indexOf("\n");
        while (index >= 0) {
            sb.deleteCharAt(index);
            index = sb.indexOf("\n", index);
        }

        //next strip any form feed chars ('\f')
        index = sb.indexOf("\f");
        while (index >= 0) {
            sb.deleteCharAt(index);
            index = sb.indexOf("\f", index);
        }

        //next strip any carriage return chars ('\r')
        index = sb.indexOf("\r");
        while (index >= 0) {
            sb.deleteCharAt(index);
            index = sb.indexOf("\r", index);
        }

        return sb.toString();
    }

    @Before
    public void setupPolicies() {

        _tableContents = new StringBuffer();

        //Static Servlet Policy Info
        for (int i = 0; i < _numServletTables; i++) {
            _servletInfo[i] = new StringBuffer();
        }

        _servletInfo[0].append(" | Cache ID Rule   0 | timeout: 0 | priority: 0 | inactivity: 0 | idGenerator: null | metaDataGenerator: null | properties:");
        _servletInfo[0].append(" | Component | ID | Type | Ignore Value | Method | Field | Required | Values | Not-Values | Index | Multiple IDs");
        _servletInfo[0].append(" | Component   0 | CMTparameter | parameter | false | null | null | true |   |   |  | false");
        _servletInfo[0].append(" | Component   1 | CMTsession | session | false | null | null | true |   |   |  | false");

        _servletInfo[1].append(" | Cache ID Rule   1 | timeout: 0 | priority: 0 | inactivity: 0 | idGenerator: null | metaDataGenerator: null | properties:");
        _servletInfo[1].append(" | Component | ID | Type | Ignore Value | Method | Field | Required | Values | Not-Values | Index | Multiple IDs");
        _servletInfo[1].append(" | Component   0 | CMTcookie | cookie | false | null | null | true |   |   |  | false");
        _servletInfo[1].append(" | Component   1 | CMTattribute | attribute | false | null | null | true |   |   |  | false");

        _servletInfo[2].append(" | Dependency ID Rule   0 | Base Name:");
        _servletInfo[2].append(" | Component | ID | Type | Ignore Value | Method | Field | Required | Values | Not-Values | Index | Multiple IDs");
        _servletInfo[2].append(" | Component   0 | CMTparameterList | parameter-list | false | null | null | true |   |   |  | false");

        _servletInfo[3].append(" | Invalidation ID Rule   0 | Base Name: | Invalidation Generator: null");
        _servletInfo[3].append(" | Component | ID | Type | Ignore Value | Method | Field | Required | Values | Not-Values | Index | Multiple IDs");
        _servletInfo[3].append(" | Component   0 | CMTservletPath | servletpath | false | null | null | true |   |   |  | false");

        //Static Command Policy Info
        for (int i = 0; i < _numCommandTables; i++) {
            _commandInfo[i] = new StringBuffer();
        }

        _commandInfo[0].append(" | Cache ID Rule   0 | timeout: 0 | priority: 0 | inactivity: 0 | idGenerator: null | metaDataGenerator: null | properties:");
        _commandInfo[0].append(" | Component | ID | Type | Ignore Value | Method | Field | Required | Values | Not-Values | Index | Multiple IDs");
        _commandInfo[0].append(" | Component   0 | CMTmethod | method | false | null | null | true |   |   |  | false");

        _commandInfo[1].append(" | Cache ID Rule   1 | timeout: 0 | priority: 0 | inactivity: 0 | idGenerator: null | metaDataGenerator: null | properties:");
        _commandInfo[1].append(" | Component | ID | Type | Ignore Value | Method | Field | Required | Values | Not-Values | Index | Multiple IDs");
        _commandInfo[1].append(" | Component   0 | CMTfield | field | false | null | null | true |   |   |  | false");

        _commandInfo[2].append(" | Dependency ID Rule   0 | Base Name:");
        _commandInfo[2].append(" | Component | ID | Type | Ignore Value | Method | Field | Required | Values | Not-Values | Index | Multiple IDs");
        _commandInfo[2].append(" | Component   0 | CMTmethod | method | false | null | null | true |   |   |  | false");

        _commandInfo[3].append(" | Invalidation ID Rule   0 | Base Name: | Invalidation Generator: null");
        _commandInfo[3].append(" | Component | ID | Type | Ignore Value | Method | Field | Required | Values | Not-Values | Index | Multiple IDs");
        _commandInfo[3].append(" | Component   0 | CMTmethod | method | false | null | null | true |   |   |  | false");

        //Static Webservice Policy Info
        for (int i = 0; i < _numWebserviceTables; i++) {
            _webserviceInfo[i] = new StringBuffer();
        }

        _webserviceInfo[0].append(" | Cache ID Rule   0 | timeout: 0 | priority: 0 | inactivity: 0 | idGenerator: null | metaDataGenerator: null | properties:");
        _webserviceInfo[0].append(" | Component | ID | Type | Ignore Value | Method | Field | Required | Values | Not-Values | Index | Multiple IDs");
        _webserviceInfo[0].append(" | Component   0 | CMTSOAPEnvelope | SOAPEnvelope | false | null | null | true |   |   |  | false");
        _webserviceInfo[0].append(" | Component   1 | CMTSOAPAction | SOAPAction | false | null | null | true |   |   |  | false");

        _webserviceInfo[1].append(" | Cache ID Rule   1 | timeout: 0 | priority: 0 | inactivity: 0 | idGenerator: null | metaDataGenerator: null | properties:");
        _webserviceInfo[1].append(" | Component | ID | Type | Ignore Value | Method | Field | Required | Values | Not-Values | Index | Multiple IDs");
        _webserviceInfo[1].append(" | Component   0 | CMTserviceOperation | serviceOperation | false | null | null | true |   |   |  | false");
        _webserviceInfo[1].append(" | Component   1 | CMTserviceOperationParameter | serviceOperationParameter | false | null | null | true |   |   |  | false");

        _webserviceInfo[2].append(" | Dependency ID Rule   0 | Base Name:");
        _webserviceInfo[2].append(" | Component | ID | Type | Ignore Value | Method | Field | Required | Values | Not-Values | Index | Multiple IDs");
        _webserviceInfo[2].append(" | Component   0 | CMTserviceOperation | serviceOperation | false | null | null | true |   |   |  | false");

        _webserviceInfo[3].append(" | Invalidation ID Rule   0 | Base Name: | Invalidation Generator: null");
        _webserviceInfo[3].append(" | Component | ID | Type | Ignore Value | Method | Field | Required | Values | Not-Values | Index | Multiple IDs");
        _webserviceInfo[3].append(" | Component   0 | CMTSOAPEnvelope | SOAPEnvelope | false | null | null | true |   |   |  | false");

    }
}
