package com.ibm.ws.cache.servlet;

import java.io.IOException;

import org.junit.Assert;

import com.ibm.websphere.simplicity.log.Log;
import com.ibm.ws.cache.TestConfig;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;

public class ServletTestCase {

    protected volatile static String testCaseName = "ServletTestCase";

    /* helper methods for servlet testcases */
    public WebConversation startNewConversation() {
        return new WebConversation();
    }

    public WebResponse getWebResponse(WebConversation wc, String uri) throws Exception {
        //Log.info(this.getClass(), "getWebResponse", "Instantiating GetMethodWebRequest: TestConig.getBaseURL: " + TestConfig.getBaseURL() + uri);
        WebRequest req = new GetMethodWebRequest(TestConfig.getBaseURL() + uri);
        //Log.info(this.getClass(), "getWebResponse", "Instantiated GetMethodWebRequest: WebRequest: " + req.toString());
        wc.set_connectTimeout(10000);
        wc.set_readTimeout(10000);
        WebResponse resp = null;
        try {
            resp = wc.getResponse(req);
            //Log.info(this.getClass(), "getWebResponse", "return from wc.getResponse: WebResponse: " + resp);
        } catch (Throwable t) {
            Log.error(this.getClass(), "getWebResponse", t);

        }
        return resp;
    }

    public WebResponse getCloneWebResponse(WebConversation wc, String uri) throws Exception {
        WebRequest req = new GetMethodWebRequest(TestConfig.getCloneURL() + uri);
        return wc.getResponse(req);
    }

    public WebResponse getRemoteWebResponse(WebConversation wc, String uri) throws Exception { //NK begin
        WebRequest req = new GetMethodWebRequest(uri);
        return wc.getResponse(req); //NK end
    }

    public void validateTable(WebTable table, String name, int rows, int columns, String value) {
        Assert.assertEquals(name + ": incorrect columns, expected=" + columns + " actual=" + table.getColumnCount(),
                            table.getColumnCount(), columns);

        Assert.assertEquals(name + ": incorrect rows, expected=" + rows + " actual=" + table.getRowCount(),
                            table.getRowCount(), rows);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                String cellData = table.getTableCell(i, j).asText();
                if (!cellData.equals(value)) {
                    Assert.fail(name + ": incorrect table value at (" + i + "," + j + ") expected=" + value + " actual=" + cellData);
                }
            }
        }
    }

    public void printTable(WebTable table) {
        if (null != table) {
            Log.info(ServletTestCase.class, "printTable", table.getID());
            for (int i = 0; i < table.getRowCount(); i++) {
                for (int j = 0; j < table.getColumnCount(); j++) {
                    TableCell cellData = table.getTableCell(i, j);
                    if (null != cellData) {
                        Log.info(ServletTestCase.class, "printTable", cellData.asText() + " ");
                    }
                }
                Log.info(ServletTestCase.class, "printTable", "\n");
            }
        } else {
            Log.info(ServletTestCase.class, "printTable", "table is NULL");
        }
    }

    public static String msg(String errorMessage, WebResponse resp) {
        String content = "<no content>";
        if (resp != null) {
            try {
                content = "\nResponse Headers:\n" + resp.toString();
                content += "\nURL:\n" + resp.getURL().toString();
                content += "\nBody:\n" + resp.getText();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return errorMessage + ": " + content;
    }

    public String msg(String errorMessage, String resp) {
        return errorMessage + ": " + resp;
    }

    public void clearCache() throws Exception {

        Log.info(ServletTestCase.class, "clearCache", "");
        WebConversation wc = startNewConversation();
        WebResponse resp = getWebResponse(wc, "/dynacachetests/clearcacheservlet");
        Assert.assertEquals("cache clear failed", resp.getResponseCode(), 200);
    }

    public String getEncodedParameterList(String pName, String[] pValues) {
        String uri = "/rparam=" + pName;
        for (int i = 0; i < pValues.length; i++) {
            uri += ("=" + pValues[i]);
        }

        return uri;
    }

    public boolean inCache(String cacheid) throws Exception {

        boolean exist = false;
        WebConversation wc = new WebConversation();
        WebResponse resp = null;
        resp = getWebResponse(wc, "/dynacachetests/CacheUtilityServlet?cacheid=" + cacheid);

        String s = "Error: no resp";
        if (resp != null) {
            s = resp.getText();
        }

        Log.info(ServletTestCase.class, "clearCache", s);
        int index = s.indexOf("Data:");
        if (index >= 0) {
            exist = true;
        }

        return exist;
    }
}
