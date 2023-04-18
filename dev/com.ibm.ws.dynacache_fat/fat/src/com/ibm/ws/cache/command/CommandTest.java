// IBM Confidential OCO Source Material
// 5724-J08, 5724-I63, 5724-H88, 5724-H89, 5655-N02, 5733-W70 (C) COPYRIGHT International Business Machines Corp. 1997, 2007
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.command;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.junit.ClassRule;
import org.junit.Test;

import com.ibm.ws.cache.servlet.ServletTestCase;
import com.ibm.ws.cache.servlet.SharedServer;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;

public class CommandTest extends ServletTestCase {

    @ClassRule
    public static SharedServer SHARED_SERVER = new SharedServer("CommandTest");

    private static Random rand = new Random();
    private final boolean debug = false;
    private final HashMap tickers = new HashMap();

    int totalRequests = 5;
    int totalIterations = 3;

    public CommandTest() {
        super();
    }

    @Test
    public void testQuoteCommand() throws Exception {
        System.out.println("Testing QuoteCommnad....");

        String URI = "/dynacachetests/commandtest?command=QuoteCommand";
        int requests = totalRequests;
        tickers.clear();
        while (requests-- > 0) {
            if (debug) {
                System.out.println("Request......." + requests);
            }
            // get a random ticker
            String ticker = randomTicker();
            WebConversation wc = startNewConversation();
            WebResponse resp = getWebResponse(wc, URI + "&ticker=" + ticker);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

            WebTable results = resp.getTableWithID("results");
            assertNotNull(msg("missing results table", resp), results);
            String resultTicker = results.getCellAsText(0, 0);
            assertEquals(msg("output ticker does not match request", resp), ticker, resultTicker);

            String resultValue = results.getCellAsText(0, 1);
            assertNotNull(msg("missing quote results", resp), resultValue);

            // request again
            Thread.sleep(2000); //Added to avoid intermittent timeout issues in solaris/windows
            resp = getWebResponse(wc, URI + "&ticker=" + ticker);
            results = resp.getTableWithID("results");
            assertNotNull(msg("missing results table2", resp), results);
            String resultTicker2 = results.getCellAsText(0, 0);
            assertEquals(msg("output ticker does not match request", resp), ticker, resultTicker2);
            String resultValue2 = results.getCellAsText(0, 1);
            assertNotNull(msg("missing quote results", resp), resultValue2);
            resultValue.trim();
            resultValue2.trim();

            if (!resultValue.equals(resultValue2)) {
                System.out.println("QuoteCommand iteration: " + requests + " resultValue: " + resultValue + " resultValue2:" + resultValue2);
                Thread.sleep(2000); //Added to avoid intermittent timeout issues in solaris/windows
                resp = getWebResponse(wc, URI + "&ticker=" + ticker);
                results = resp.getTableWithID("results");
                assertNotNull(msg("missing results table2", resp), results);
                resultTicker2 = results.getCellAsText(0, 0);
                assertEquals(msg("output ticker does not match request", resp), ticker, resultTicker2);
                resultValue2 = results.getCellAsText(0, 1);
                assertNotNull(msg("missing quote results", resp), resultValue2);
                resultValue2.trim();
                System.out.println("QuoteCommand iteration: " + requests + " resultValue: " + resultValue + " resultValue2:" + resultValue2);
            }
            assertEquals(msg("quote value did not match, quote was not cached!", resp), resultValue, resultValue2);
        }
        System.out.println("Testing QuoteCommnad Complete");
    }

    @Test
    public void testQuoteCommandComplex() throws Exception {
        System.out.println("Testing QuoteCommnadComplex....");

        String URI = "/dynacachetests/commandtest?command=QuoteCommandComplex";
        int requests = totalRequests;
        tickers.clear();
        while (requests-- > 0) {
            if (debug) {
                System.out.println("Request......." + requests);
            }
            // get a random ticker
            String ticker = randomTicker();
            WebConversation wc = startNewConversation();
            WebResponse resp = getWebResponse(wc, URI + "&ticker=" + ticker);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

            WebTable results = resp.getTableWithID("results");
            assertNotNull(msg("missing results table", resp), results);
            String resultTicker = results.getCellAsText(0, 0);
            assertEquals(msg("output ticker does not match request", resp), ticker, resultTicker);

            String resultValue = results.getCellAsText(0, 1);
            assertNotNull(msg("missing quote results", resp), resultValue);

            // request again
            Thread.sleep(2000); //Added to avoid intermittent timeout issues in solaris/windows
            resp = getWebResponse(wc, URI + "&ticker=" + ticker);
            results = resp.getTableWithID("results");
            assertNotNull(msg("missing results table2", resp), results);
            String resultTicker2 = results.getCellAsText(0, 0);
            assertEquals(msg("output ticker does not match request", resp), ticker, resultTicker2);
            String resultValue2 = results.getCellAsText(0, 1);
            assertNotNull(msg("missing quote results", resp), resultValue2);
            resultValue.trim();
            resultValue2.trim();

            if (!resultValue.equals(resultValue2)) {
                System.out.println("QuoteCommandComplex iteration: " + requests + " resultValue: " + resultValue + " resultValue2:" + resultValue2);
                Thread.sleep(2000); //Added to avoid intermittent timeout issues in solaris/windows
                resp = getWebResponse(wc, URI + "&ticker=" + ticker);
                results = resp.getTableWithID("results");
                assertNotNull(msg("missing results table2", resp), results);
                resultTicker2 = results.getCellAsText(0, 0);
                assertEquals(msg("output ticker does not match request", resp), ticker, resultTicker2);
                resultValue2 = results.getCellAsText(0, 1);
                resultValue2.trim();
                System.out.println("QuoteCommandComplex iteration: " + requests + " resultValue: " + resultValue + " resultValue2:" + resultValue2);
            }
            assertEquals(msg("quote value did not match, quote was not cached!", resp), resultValue, resultValue2);
        }
        System.out.println("Testing QuoteCommnadComplex Complete");
    }

    @Test
    public void testQuoteCommandPMD() throws Exception {
        System.out.println("Testing QuoteCommnadPMD....");

        String URI = "/dynacachetests/commandtest?command=QuoteCommandPMD";
        int requests = totalRequests;
        tickers.clear();
        while (requests-- > 0) {
            if (debug) {
                System.out.println("Request......." + requests);
            }
            // get a random ticker
            String ticker = randomTicker();
            WebConversation wc = startNewConversation();
            WebResponse resp = getWebResponse(wc, URI + "&ticker=" + ticker);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

            WebTable results = resp.getTableWithID("results");
            assertNotNull(msg("missing results table", resp), results);
            String resultTicker = results.getCellAsText(0, 0);
            assertEquals(msg("output ticker does not match request", resp), ticker, resultTicker);

            String resultValue = results.getCellAsText(0, 1);
            assertNotNull(msg("missing quote results", resp), resultValue);

            // request again
            Thread.sleep(1000); //RJMP08
            resp = getWebResponse(wc, URI + "&ticker=" + ticker);
            results = resp.getTableWithID("results");
            assertNotNull(msg("missing results table2", resp), results);
            String resultTicker2 = results.getCellAsText(0, 0);
            assertEquals(msg("output ticker does not match request", resp), ticker, resultTicker2);
            String resultValue2 = results.getCellAsText(0, 1);
            assertNotNull(msg("missing quote results", resp), resultValue2);
            resultValue.trim();
            resultValue2.trim();

            if (!resultValue.equals(resultValue2)) {
                System.out.println("QuoteCommandPMD iteration: " + requests + " resultValue: " + resultValue + " resultValue2:" + resultValue2);
                // request again
                Thread.sleep(2000); //RJMP08
                resp = getWebResponse(wc, URI + "&ticker=" + ticker);
                results = resp.getTableWithID("results");
                assertNotNull(msg("missing results table2", resp), results);
                resultTicker2 = results.getCellAsText(0, 0);
                assertEquals(msg("output ticker does not match request", resp), ticker, resultTicker2);
                resultValue2 = results.getCellAsText(0, 1);
                assertNotNull(msg("missing quote results", resp), resultValue2);
                resultValue2.trim();
                System.out.println("QuoteCommandPMD iteration: " + requests + " resultValue: " + resultValue + " resultValue2:" + resultValue2);
            }
            assertEquals(msg("quote value did not match, quote was not cached!", resp), resultValue, resultValue2);
        }
        System.out.println("Testing QuoteCommnadPMD Complete");
    }

    @Test
    public void testQuoteCommandIdGen() throws Exception {
        System.out.println("Testing QuoteCommnadIdGen....");

        String URI = "/dynacachetests/commandtest?command=QuoteCommandIdGen";
        int requests = totalRequests;
        tickers.clear();
        while (requests-- > 0) {
            if (debug) {
                System.out.println("Request......." + requests);
            }
            // get a random ticker
            String ticker = randomTicker();
            WebConversation wc = startNewConversation();
            WebResponse resp = getWebResponse(wc, URI + "&ticker=" + ticker);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

            WebTable results = resp.getTableWithID("results");
            assertNotNull(msg("missing results table", resp), results);
            String resultTicker = results.getCellAsText(0, 0);
            assertEquals(msg("output ticker does not match request", resp), ticker, resultTicker);

            String resultValue = results.getCellAsText(0, 1);
            assertNotNull(msg("missing quote results", resp), resultValue);

            // request again
            Thread.sleep(2000); //Added to avoid intermittent timeout issues in solaris/windows
            resp = getWebResponse(wc, URI + "&ticker=" + ticker);
            results = resp.getTableWithID("results");
            assertNotNull(msg("missing results table2", resp), results);
            String resultTicker2 = results.getCellAsText(0, 0);
            assertEquals(msg("output ticker does not match request", resp), ticker, resultTicker2);
            String resultValue2 = results.getCellAsText(0, 1);
            assertNotNull(msg("missing quote results", resp), resultValue2);
            resultValue.trim();
            resultValue2.trim();

            if (!resultValue.equals(resultValue2)) {
                System.out.println("QuoteCommandIdGen iteration: " + requests + " resultValue: " + resultValue + " resultValue2:" + resultValue2);
                Thread.sleep(2000); //Added to avoid intermittent timeout issues in solaris/windows
                resp = getWebResponse(wc, URI + "&ticker=" + ticker);
                results = resp.getTableWithID("results");
                assertNotNull(msg("missing results table2", resp), results);
                resultTicker2 = results.getCellAsText(0, 0);
                assertEquals(msg("output ticker does not match request", resp), ticker, resultTicker2);
                resultValue2 = results.getCellAsText(0, 1);
                assertNotNull(msg("missing quote results", resp), resultValue2);
                resultValue2.trim();
                System.out.println("QuoteCommandIdGen iteration: " + requests + " resultValue: " + resultValue + " resultValue2:" + resultValue2);

            }
            assertEquals(msg("quote value did not match, quote was not cached!", resp), resultValue, resultValue2);
        }
        System.out.println("Testing QuoteCommnadIdGen Complete");
    }

    @Test
    public void testNoOutputPropCommand() throws Exception {
        System.out.println("Testing NoOutputPropCommand....");

        String URI = "/dynacachetests/commandtest?command=NoOutputPropCommand";
        int requests = totalRequests;
        tickers.clear();
        while (requests-- > 0) {
            if (debug) {
                System.out.println("Request......." + requests);
            }
            // get a random ticker
            String ticker = randomTicker();
            WebConversation wc = startNewConversation();
            WebResponse resp = getWebResponse(wc, URI + "&ticker=" + ticker);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

            WebTable results = resp.getTableWithID("results");
            assertNotNull(msg("missing results table", resp), results);
            String resultTicker = results.getCellAsText(0, 0);
            assertEquals(msg("output ticker does not match request", resp), ticker, resultTicker);

            String resultValue = results.getCellAsText(0, 1);
            assertNotNull(msg("missing quote results", resp), resultValue);

            // request again
            Thread.sleep(2000); //Added to avoid intermittent timeout issues in solaris/windows
            resp = getWebResponse(wc, URI + "&ticker=" + ticker);
            results = resp.getTableWithID("results");
            assertNotNull(msg("missing results table2", resp), results);
            String resultTicker2 = results.getCellAsText(0, 0);
            assertEquals(msg("output ticker does not match request", resp), ticker, resultTicker2);
            String resultValue2 = results.getCellAsText(0, 1);
            assertNotNull(msg("missing quote results", resp), resultValue2);
            resultValue.trim();
            resultValue2.trim();

            if (!resultValue.equals(resultValue2)) {
                // request again
                System.out.println("NoOutputPropCommand iteration: " + requests + " resultValue: " + resultValue + " resultValue2:" + resultValue2);
                Thread.sleep(2000); //Added to avoid intermittent timeout issues in solaris/windows
                resp = getWebResponse(wc, URI + "&ticker=" + ticker);
                results = resp.getTableWithID("results");
                assertNotNull(msg("missing results table2", resp), results);
                resultTicker2 = results.getCellAsText(0, 0);
                assertEquals(msg("output ticker does not match request", resp), ticker, resultTicker2);
                resultValue2 = results.getCellAsText(0, 1);
                assertNotNull(msg("missing quote results", resp), resultValue2);
                resultValue2.trim();
                System.out.println("NoOutputPropCommand iteration: " + requests + " resultValue: " + resultValue + " resultValue2:" + resultValue2);
            }
            assertEquals(msg("quote value did not match, quote was not cached!", resp), resultValue, resultValue2);
        }
        System.out.println("Testing NoOutputPropCommand Complete");
    }

    @Test
    public void testWatchListField() throws Exception {
        System.out.println("Testing WatchListField....");

        String URI = "/dynacachetests/commandtest?command=WatchListCommand";
        int requests = totalRequests;
        tickers.clear();
        while (requests-- > 0) {
            if (debug) {
                System.out.println("Request......." + requests);
            }
            // get a random ticker
            String group = randomGroup();
            int userNum = rand.nextInt(100);
            WebConversation wc = startNewConversation();
            WebResponse resp = getWebResponse(wc, URI + "&group=" + group + "&user=" + userNum);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

            WebTable results = resp.getTableWithID("results");
            assertNotNull(msg("missing results table", resp), results);

            String resultTicker = results.getCellAsText(0, 0);
            assertNotNull(msg("missing quote ticker", resp), resultTicker);
            String resultValue = results.getCellAsText(0, 1);
            assertNotNull(msg("missing quote results", resp), resultValue);

            // request again
            resp = getWebResponse(wc, URI + "&group=" + group + "&user=" + userNum);
            results = resp.getTableWithID("results");
            assertNotNull(msg("missing results table2", resp), results);
            String resultTicker2 = results.getCellAsText(0, 0);
            assertEquals(msg("output ticker does not match first request", resp), resultTicker2, resultTicker);
            String resultValue2 = results.getCellAsText(0, 1);
            assertNotNull(msg("missing quote results", resp), resultValue2);
        }
        System.out.println("Testing WatchListField Complete");
    }

    @Test
    public void testWatchListMixed() throws Exception {
        System.out.println("Testing WatchListMixed....");

        String URI = "/dynacachetests/commandtest?command=WatchListCommandMixed";
        int requests = totalRequests;
        tickers.clear();
        while (requests-- > 0) {
            if (debug) {
                System.out.println("Request......." + requests);
            }
            // get a random ticker
            String group = randomGroup();
            int userNum = rand.nextInt(100);
            WebConversation wc = startNewConversation();
            WebResponse resp = getWebResponse(wc, URI + "&group=" + group + "&user=" + userNum);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

            WebTable results = resp.getTableWithID("results");
            assertNotNull(msg("missing results table", resp), results);

            String resultTicker = results.getCellAsText(0, 0);
            assertNotNull(msg("missing quote ticker", resp), resultTicker);
            String resultValue = results.getCellAsText(0, 1);
            assertNotNull(msg("missing quote results", resp), resultValue);

            // request again
            resp = getWebResponse(wc, URI + "&group=" + group + "&user=" + userNum);
            results = resp.getTableWithID("results");
            assertNotNull(msg("missing results table2", resp), results);
            String resultTicker2 = results.getCellAsText(0, 0);
            assertEquals(msg("output ticker does not match first request", resp), resultTicker2, resultTicker);
            String resultValue2 = results.getCellAsText(0, 1);
            assertNotNull(msg("missing quote results", resp), resultValue2);
        }
        System.out.println("Testing WatchListMixed Complete");
    }

    @Test
    public void testInvalidation() throws Exception {
        System.out.println("Testing Invalidation....");

        String URI = "/dynacachetests/commandtest?command=WatchListCommandMixed";
        int requests = totalRequests;
        tickers.clear();
        while (requests-- > 0) {
            if (debug) {
                System.out.println("Request......." + requests);
            }
            // get a random ticker
            String group = randomGroup();
            int userNum = rand.nextInt(100);
            WebConversation wc = startNewConversation();
            WebResponse resp = getWebResponse(wc, URI + "&group=" + group + "&user=" + userNum);
            assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);

            WebTable results = resp.getTableWithID("results");
            assertNotNull(msg("missing results table", resp), results);

            String resultTicker = results.getCellAsText(0, 0);
            assertNotNull(msg("missing quote ticker", resp), resultTicker);
            String resultValue = results.getCellAsText(0, 1);
            assertNotNull(msg("missing quote results", resp), resultValue);

            // request again
            resp = getWebResponse(wc, URI + "&group=" + group + "&user=" + userNum);
            WebTable results2 = resp.getTableWithID("results");
            assertNotNull(msg("missing results table2", resp), results2);
            for (int i = 0; i < 5; i++) {
                String resultTicker1 = results.getCellAsText(i, 0);
                String resultTicker2 = results2.getCellAsText(i, 0);
                assertEquals(msg("output ticker does not match first request", resp), resultTicker2, resultTicker1);
            }
            String resultValue2 = results.getCellAsText(0, 1);
            assertNotNull(msg("missing quote results", resp), resultValue2);

            //request again, but invalidate
            resp = getWebResponse(wc, URI + "&action=invalidate&group=" + group + "&user=" + userNum);
            WebTable results3 = resp.getTableWithID("results");
            assertNotNull(msg("missing results table3", resp), results3);
            boolean allequal = true;
            //make sure at least 1 of the 5 tickers is different... odds of them
            //all being the same is very slim...
            for (int i = 0; i < 5; i++) {
                String resultTicker1 = results.getCellAsText(i, 0);
                String resultTicker3 = results3.getCellAsText(i, 0);
                if (!resultTicker1.equals(resultTicker3))
                    allequal = false;
            }
            assertTrue(msg("output tickers match first request - should be new random", resp), !allequal);
            String resultValue3 = results.getCellAsText(0, 1);
            assertNotNull(msg("missing quote results", resp), resultValue3);
        }
        System.out.println("Testing Invalidation Complete");
    }

    @Test
    public void testExplicitUpdate() throws Exception {
        System.out.println("Testing ExplicitUpdate....");

        String URI = "/dynacachetests/maptest";
        int iterations = totalIterations;
        tickers.clear();
        while (iterations-- > 0) {
            if (debug) {
                System.out.println("Iteration......." + iterations);
            }
            // get some random tickers, use hashset to prevent dups
            HashSet hashset = new HashSet();
            for (int i = 0; i < 100; i++)
                hashset.add(randomTicker());

            String tickers[] = (String[]) hashset.toArray(new String[0]);

            WebConversation wc = startNewConversation();
            for (int i = 0; i < tickers.length; i++) {
                WebResponse resp = getWebResponse(wc, URI + "?action=put&key=" + tickers[i] + "&value=" + i);
                assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
                WebTable results = resp.getTableWithID("results");
                assertNotNull(msg("missing results table", resp), results);
            }

            for (int i = 0; i < tickers.length; i++) {
                WebResponse resp = getWebResponse(wc, URI + "?action=get&key=" + tickers[i]);
                assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
                WebTable results = resp.getTableWithID("results");
                assertNotNull(msg("missing results table", resp), results);
                assertEquals(msg("value not equal to put", resp), results.getCellAsText(0, 1), Integer.toString(i));
            }
            // request again
            for (int i = 0; i < tickers.length; i++) {
                WebResponse resp = getWebResponse(wc, URI + "?action=get&key=" + tickers[i]);
                assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
                WebTable results = resp.getTableWithID("results");
                assertNotNull(msg("missing results table", resp), results);
                assertEquals(msg("value not equal to put", resp), results.getCellAsText(0, 1), Integer.toString(i));
            }
            //clear the cache every 10th iteration, mix it up a bit
            if (iterations % 10 == 5) {
                //clear
                WebResponse cresp = getWebResponse(wc, URI + "?action=clear");
                assertEquals(msg("Response code was not OK", cresp), cresp.getResponseCode(), 200);
                // request again, make sure we get null
                for (int i = 0; i < tickers.length; i++) {
                    WebResponse resp = getWebResponse(wc, URI + "?action=get&key=" + tickers[i]);
                    assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
                    WebTable results = resp.getTableWithID("results");
                    assertNotNull(msg("missing results table", resp), results);
                    assertEquals(msg("value supposed to be null after clear", resp), results.getCellAsText(0, 1), "null");
                }
            }
        }
        System.out.println("Testing ExplicitUpdate Complete");
    }

    @Test
    public void testExplicitUpdatePush() throws Exception {
        System.out.println("Testing ExplicitUpdatePush....");

        String URI = "/dynacachetests/maptest";
        int iterations = totalIterations;
        tickers.clear();
        while (iterations-- > 0) {
            if (debug) {
                System.out.println("Iteration......." + iterations);
            }
            // get some random tickers, use hashset to prevent dups
            HashSet hashset = new HashSet();
            for (int i = 0; i < 100; i++)
                hashset.add(randomTicker());

            String tickers[] = (String[]) hashset.toArray(new String[0]);

            WebConversation wc = startNewConversation();
            for (int i = 0; i < tickers.length; i++) {
                WebResponse resp = getWebResponse(wc, URI + "?sharing=push&action=put&key=" + tickers[i] + "&value=" + i);
                assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
                WebTable results = resp.getTableWithID("results");
                assertNotNull(msg("missing results table", resp), results);
            }

            for (int i = 0; i < tickers.length; i++) {
                WebResponse resp = getWebResponse(wc, URI + "?sharing=push&action=get&key=" + tickers[i]);
                assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
                WebTable results = resp.getTableWithID("results");
                assertNotNull(msg("missing results table", resp), results);
                assertEquals(msg("value not equal to put", resp), results.getCellAsText(0, 1), Integer.toString(i));
            }
            // request again
            for (int i = 0; i < tickers.length; i++) {
                WebResponse resp = getWebResponse(wc, URI + "?sharing=push&action=get&key=" + tickers[i]);
                assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
                WebTable results = resp.getTableWithID("results");
                assertNotNull(msg("missing results table", resp), results);
                assertEquals(msg("value not equal to put", resp), results.getCellAsText(0, 1), Integer.toString(i));
            }
            //clear the cache every 10th iteration, mix it up a bit
            if (iterations % 10 == 5) {
                //clear
                WebResponse cresp = getWebResponse(wc, URI + "?action=clear");
                assertEquals(msg("Response code was not OK", cresp), cresp.getResponseCode(), 200);
                // request again, make sure we get null
                for (int i = 0; i < tickers.length; i++) {
                    WebResponse resp = getWebResponse(wc, URI + "?sharing=push&action=get&key=" + tickers[i]);
                    assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
                    WebTable results = resp.getTableWithID("results");
                    assertNotNull(msg("missing results table", resp), results);
                    assertEquals(msg("value supposed to be null after clear", resp), results.getCellAsText(0, 1), "null");
                }
            }
        }
        System.out.println("Testing ExplicitUpdatePush Complete");
    }

    @Test
    public void testExplicitUpdatePull() throws Exception {
        System.out.println("Testing ExplicitUpdateFull....");

        String URI = "/dynacachetests/maptest";
        int iterations = totalIterations;
        tickers.clear();
        while (iterations-- > 0) {
            if (debug) {
                System.out.println("Iteration......." + iterations);
            }
            // get some random tickers, use hashset to prevent dups
            HashSet hashset = new HashSet();
            for (int i = 0; i < 100; i++)
                hashset.add(randomTicker());

            String tickers[] = (String[]) hashset.toArray(new String[0]);
            WebConversation wc = startNewConversation();
            for (int i = 0; i < tickers.length; i++) {
                WebResponse resp = getWebResponse(wc, URI + "?sharing=pull&action=put&key=" + tickers[i] + "&value=" + i);
                assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
                WebTable results = resp.getTableWithID("results");
                assertNotNull(msg("missing results table", resp), results);
            }

            int numFailures = 0;
            for (int i = 0; i < tickers.length; i++) {
                WebResponse resp = getWebResponse(wc, URI + "?sharing=pull&action=get&key=" + tickers[i]);
                assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
                WebTable results = resp.getTableWithID("results");
                assertNotNull(msg("missing results table", resp), results);
                if (!results.getCellAsText(0, 1).equals(Integer.toString(i)))
                    numFailures++;
                if (numFailures > 5)
                    assertEquals(msg("value not equal to put", resp), results.getCellAsText(0, 1), Integer.toString(i));
            }
            // request again
            numFailures = 0;
            for (int i = 0; i < tickers.length; i++) {
                WebResponse resp = getWebResponse(wc, URI + "?sharing=pull&action=get&key=" + tickers[i]);
                assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
                WebTable results = resp.getTableWithID("results");
                assertNotNull(msg("missing results table", resp), results);
                if (!results.getCellAsText(0, 1).equals(Integer.toString(i)))
                    numFailures++;
                if (numFailures > 5)
                    assertEquals(msg("value not equal to put", resp), results.getCellAsText(0, 1), Integer.toString(i));
            }
            //clear the cache every 10th iteration, mix it up a bit
            if (iterations % 10 == 0) {
                //clear
                WebResponse cresp = getWebResponse(wc, URI + "?action=clear");
                assertEquals(msg("Response code was not OK", cresp), cresp.getResponseCode(), 200);
                // request again, make sure we get null
                for (int i = 0; i < tickers.length; i++) {
                    WebResponse resp = getWebResponse(wc, URI + "?sharing=pull&action=get&key=" + tickers[i]);
                    assertEquals(msg("Response code was not OK", resp), resp.getResponseCode(), 200);
                    WebTable results = resp.getTableWithID("results");
                    assertNotNull(msg("missing results table", resp), results);
                    assertEquals(msg("value supposed to be null after clear", resp), results.getCellAsText(0, 1), "null");
                }
            }
        }
        System.out.println("Testing ExplicitUpdateFull Complete");
    }

    @Test
    public void testCommandCacheInstanceSupport() throws Exception {
        System.out.println("Testing CommandCacheInstanceSupport....");

        String URI = "/dynacachetests/commandtest?command=MyQuoteCommand";
        //part 1 check if the command is cached

        // get a random ticker
        String ticker = randomTicker();
        WebConversation wc = startNewConversation();

        //first request
        WebResponse resp = getWebResponse(wc, URI + "&ticker=" + ticker);
        WebTable results = resp.getTableWithID("results");
        assertNotNull(msg("missing results table", resp), results);
        String resultValue = results.getCellAsText(0, 1);
        assertNotNull(msg("missing quote results", resp), resultValue);

        // second request
        resp = getWebResponse(wc, URI + "&ticker=" + ticker);
        results = resp.getTableWithID("results");
        assertNotNull(msg("missing results table2", resp), results);
        String resultValue2 = results.getCellAsText(0, 1);
        assertEquals(msg("quote value did not match, quote was not cached!", resp), resultValue, resultValue2);

        //part 2 check if the cached command belongs to the correct cache instance
        String query = "?command=getIDs&cacheInstance=services/cache/servletInstance_1&dependencyID=MyQuoteTicker";
        resp = getWebResponse(wc, "/dynacachetests/commandtest" + query);
        String cacheIDs = resp.getHeaderField("cacheIDs");
        assertTrue("Command NOT cached in the correct cache instance+ " + cacheIDs, cacheIDs.contains("MyQuoteTicker"));

        System.out.println("Testing CommandCacheInstanceSupport Complete");
    }

    static public String randomGroup() {
        String groups[] = { "public", "employee", "manager" };
        return groups[rand.nextInt(3)];
    }

    private String randomTicker() {
        String ticker = randomTicker1();
        while (tickers.containsKey(ticker)) {
            ticker = randomTicker1();
        }
        tickers.put(ticker, ticker);
        return ticker;
    }

    static public String randomTicker1() {
        // generates 262626 (17576) random tickers
        char ticker[] = new char[3];
        ticker[0] = (char) ('A' + rand.nextInt(26));
        ticker[1] = (char) ('A' + rand.nextInt(26));
        ticker[2] = (char) ('A' + rand.nextInt(26));
        return new String(ticker);
    }

}
