// 1.7, 10/9/07
// IBM Confidential OCO Source Material
// 5724-J08, 5724-I63, 5724-H88, 5724-H89, 5655-N02, 5733-W70 (C) COPYRIGHT International Business Machines Corp. 1997, 2007
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.command;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.websphere.command.CacheableCommand;
import com.ibm.ws.cache.ServerCache;
import com.ibm.ws.cache.intf.DCache;

public class CommandTestServlet extends HttpServlet {
    public static Random rand = new Random();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        PrintWriter out;

        res.setContentType("text/html");
        out = res.getWriter();
        out.println("<html><body topmargin=\"20\" leftmargin=\"20\" bgcolor=\"#0066cc\">");
        out.println("<font color=\"#ffffff\">");
        out.println("<h1>Command Results</h1>");
        String command = req.getParameter("command");
        out.println("<h2>command to execute: " + command + "</h2>");
        if (command == null) {
            res.sendError(500, "missing command");
            return;
        }

        if (command.startsWith("getIDs")) {
            String cacheInstance = req.getParameter("cacheInstance");
            String dependencyID = req.getParameter("dependencyID");
            DCache cc = ServerCache.getCache(cacheInstance);
            Collection cacheIDs = cc.getAllDependencyIds();
            out.println(cc.toString());
            if (null != cacheIDs) {
                res.setHeader("cacheIDs", cacheIDs.toString());
                out.println("<h2>" + cacheIDs.toString() + "</h2>");
            }
            return;
        }

        String mytickers[] = null;
        if (command.startsWith("WatchListCommand")) {
            try {
                String userId = "Userid:" + rand.nextInt(1000);
                WatchListCommand wlc = null;
                if (command.equals("WatchListCommand"))
                    wlc = new WatchListCommand();
                else if (command.equals("WatchListCommandMixed"))
                    wlc = new WatchListCommandMixed();
                wlc.setUserGroup(req.getParameter("group"));
                wlc.setUserNumber(Integer.parseInt(req.getParameter("user")));
                wlc.execute();
                mytickers = wlc.getWatchList();
                /*
                 * // simple cache hit perf test
                 * long start = System.currentTimeMillis();
                 * for (int i=0;i<1000000;i++) {
                 * wlc = new WatchListCommand();
                 * wlc.setUserid(userId);
                 * wlc.execute();
                 * mytickers = wlc.getWatchList();
                 * }
                 * long end = System.currentTimeMillis();
                 * out.println("watchlist commands/sec="+(1000000L*1000L/(end-start)));
                 */
                //change command to get output...
                command = "QuoteCommand";

            } catch (Exception ex) {
                res.setStatus(500);
                ex.printStackTrace();
                ex.printStackTrace(out);
                return;
            }
        } else {
            String ticker = req.getParameter("ticker");
            if (ticker == null) {
                res.sendError(500, "missing ticker");
                return;
            }
            mytickers = new String[] { ticker };
        }

        out.println("<table id=\"results\" BORDER=\"2\" BGCOLOR=\"#DDDDFF\" cellpadding=\"5\">");

        DecimalFormat df = new DecimalFormat("#.00");
        try {
            for (int i = 0; i < mytickers.length; i++) {

                CacheableCommand stockCommand = null;
                if (command.equals("QuoteCommand")) {
                    stockCommand = new QuoteCommand();

                }

                else if (command.equals("QuoteCommandIdGen")) {

                    stockCommand = new QuoteCommandIdGen();
                    //-----Tamera's code----------

                    String nonser = req.getParameter("nonser");

                    if (nonser != null && nonser.equals("true")) {
                        if (stockCommand instanceof QuoteCommandIdGen) {
                            ((QuoteCommandIdGen) stockCommand).setObject(new Object());

                        }
                    }

                    //---end of Tamera's code------

                } else if (command.equals("QuoteCommandPMD"))
                    stockCommand = new QuoteCommandPMD();
                else if (command.equals("NoOutputPropCommand"))
                    stockCommand = new NoOutputPropCommand();
                else if (command.equals("QuoteCommandComplex"))
                    stockCommand = new QuoteCommandComplex();
                else if (command.equals("MyQuoteCommand")) {
                    stockCommand = new MyQuoteCommand();
                } else {
                    res.sendError(500, "unknown command:" + command);
                    return;
                }

                String price = "";
                if (stockCommand instanceof Quote) {
                    ((Quote) stockCommand).setTicker(mytickers[i]);
                    stockCommand.execute();
                    price = df.format(((Quote) stockCommand).getPrice());
                } else if (stockCommand instanceof MyQuoteCommand) {
                    ((MyQuoteCommand) stockCommand).setTicker(mytickers[i]);
                    stockCommand.execute();
                    price = df.format(((MyQuoteCommand) stockCommand).get52WeekHigh());
                }

                out.println("<tr><td>" + mytickers[i] + "</td><td>" + price + "</td></tr>");
            }
        } catch (Exception ex) {
            res.sendError(500, ex.getMessage());
            ex.printStackTrace();
            ex.printStackTrace(out);
            return;
        }
        out.println("</table>");
        out.println("</body></html>");
/*
 * out.println("<br>");
 * out.println("<h2>Manual Cache Update Test</h2>");
 * try {
 * String ticker = "BKM-"+rand.nextInt(10);
 * QuoteCommand aolCommand = new QuoteCommand();
 * aolCommand.setTicker(ticker);
 * boolean inCache = aolCommand.executeFromCache();
 * out.println("<br>Command for ticker "+ticker+" in cache: "+inCache);
 * if (inCache) {
 * out.println("<br>"+ticker+" (cached)="+df.format(aolCommand.getPrice()));
 * out.println("<br>invalidating cached "+ticker);
 * try {
 * DynamicCacheAccessor.getCache().invalidateById(aolCommand.getId(),true);
 * } catch (Exception ex) {
 * }
 * } else {
 * aolCommand.randomPrice();
 * out.println("<br>generated new "+ticker+" quote="+df.format(aolCommand.getPrice()));
 * aolCommand.updateCache();
 * }
 * } catch (Exception ex) {
 * res.setStatus(500);
 * ex.printStackTrace();
 * ex.printStackTrace(out);
 * }
 */
    }

}
