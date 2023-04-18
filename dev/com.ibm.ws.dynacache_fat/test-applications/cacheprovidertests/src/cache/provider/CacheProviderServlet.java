/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * Copyright IBM Corp. 2013
 *
 * The source code for this program is not published or otherwise divested 
 * of its trade secrets, irrespective of what has been deposited with the 
 * U.S. Copyright Office.
 */
package cache.provider;

import java.io.IOException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.websphere.cache.DistributedMap;

/**
 *
 */
@WebServlet("/cacheprovider")
public class CacheProviderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public CacheProviderServlet() {}

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("CacheTestServlet invoked");

        String testKey = "bazinga";
        boolean result = false;
        try {
            String jndiName = request.getParameter("jndiName");
            if (jndiName == null)
                jndiName = "services/cache/dummy";
            InitialContext ic = new InitialContext();
            DistributedMap dm1 = (DistributedMap) ic.lookup(jndiName);
            result = dm1.containsKey(testKey);
        } catch (NamingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (result == true)
            response.getWriter().print("Dummy cache was used");
        else
            response.getWriter().print("Dummy cache was not used");
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {}

}
