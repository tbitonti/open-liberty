// IBM Confidential OCO Source Material
// 5724-J08, 5724-I63, 5724-H88, 5724-H89, 5655-N02, 5733-W70 (C) COPYRIGHT International Business Machines Corp. 1997, 2022
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.websphere.cache.Cache;
import com.ibm.websphere.cache.DynamicCacheAccessor;

public class ClearCacheServlet extends HttpServlet {
    //**
    //** init( ServletConfig config )
    //**
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    //**
    //** destroy()
    //**
    @Override
    public void destroy() {
        super.destroy();
    }

    //**
    //** doGet( HttpServletRequest req, HttpServletResponse resp )
    //**
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    //**
    //** doPost( HttpServletRequest req, HttpServletResponse resp )
    //**
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    //**
    //** processRequest( HttpServletRequest req, HttpServletResponse resp )
    //**
    public void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter out = resp.getWriter();
        long start = System.currentTimeMillis();

        Cache c = DynamicCacheAccessor.getCache();
        if (c != null)
            c.clear();
        long end = System.currentTimeMillis();
        out.println("<html><body>");
        out.println("<br> Starting clear: timestamp=" + start + "  cache cleared elapsed=" + (end - start));
        out.println("</body></html>");
    }
}
