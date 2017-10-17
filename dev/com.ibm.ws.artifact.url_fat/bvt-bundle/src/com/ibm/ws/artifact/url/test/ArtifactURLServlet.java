package com.ibm.ws.artifact.url.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import com.ibm.ws.artifact.url.URLService;

public class ArtifactURLServlet extends HttpServlet {
    private static final long serialVersionUID = 8783096430712965126L;
    private static final String PROTOCOL = "notabundleresource";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/plain");

        //eyecatcher.. looked for by the client to know it's hit the right place.
        writer.println("This is WOPR. Welcome Dr Falken.");

        //Hackzor.
        BundleContext bc = FrameworkUtil.getBundle(Servlet.class).getBundleContext();
        URLService urls = null;
        ServiceReference<URLService> urlsr = bc.getServiceReference(URLService.class);
        try {
            if (urlsr != null) {
                urls = bc.getService(urlsr);
                String test = req.getQueryString();
                if (test != null && urls != null) {
                    if (test.equals("testURLService")) {
                        testURLService(urls, bc, urlsr, writer);
                    }
                    if (test.equals("parseTest")) {
                        ParseTest.test(writer);
                    }
                    if (test.equals("connectionTest")) {
                        ConnectionTest.test(writer);
                    }
                } else if (urls == null) {
                    writer.println("FAIL: Unable to use null URLService the magic 8ball says 'NO'.");
                } else {
                    writer.println("FAIL: Failure to specify test to execute.\nSelecting Global Thermonuclear War.");
                }
            } else {
                writer.println("FAIL: Lack of custard filling detected, unable to obtain URLService service reference.");
            }
        } finally {
            if (bc != null) {
                if (urlsr != null) {
                    bc.ungetService(urlsr);
                }
            }
        }
    }

    public void testURLService(URLService urls, BundleContext bc, ServiceReference<URLService> urlsr, PrintWriter writer) {
        //simple tests..
        Bundle b = FrameworkUtil.getBundle(URLService.class);

        URL originalURL = b.getResource("com/ibm/ws/artifact/url/URLService.class");
        try {
            //try to convert..
            URL converted = urls.convertURL(originalURL, b);
            if (converted == null) {
                writer.println("FAIL: unable to convert url, service returned null");
                return;
            }
            //did it comeback with expected protocol ?
            if (!converted.toExternalForm().startsWith(PROTOCOL)) {
                writer.println("FAIL: converted url did not start with " + PROTOCOL + " " + converted.toExternalForm());
                return;
            }
            //round trip it.. 
            URL roundTrip = new URL(converted.toExternalForm());

            //read the streams..
            InputStream original = null;
            InputStream is = null;
            InputStream rt = null;
            try {
                original = originalURL.openStream();
                is = converted.openStream();
                rt = roundTrip.openStream();

                BufferedReader br1 = new BufferedReader(new InputStreamReader(original));
                BufferedReader br2 = new BufferedReader(new InputStreamReader(is));
                BufferedReader br3 = new BufferedReader(new InputStreamReader(rt));

                String s1 = br1.readLine();
                String s2 = br2.readLine();
                String s3 = br3.readLine();

                if (!s1.equals(s2) || !s1.equals(s3) || !s2.equals(s3)) {
                    System.out.println("FAIL: urls did not give back matching data orig:" + s1 + " converted:" + s2 + " roundtrip:" + s3);
                    return;
                }
                br1.close();
                original = null;
                br2.close();
                is = null;
                br3.close();
                rt = null;

                //this next bit is truly evil.. we're going to use a url from a liberty kernel bundle
                //and convert it, then stop the liberty bundle, to check the url is 'forgotten' properly, 
                //then restart the bundle to verify the url can be reregistered & still works.               
                Bundle target = null;
                for (Bundle bundle : bc.getBundles()) {
                    if (bundle.getSymbolicName().startsWith("com.ibm.ws.artifact.bundle")) {
                        target = bundle;
                        break;
                    }
                }

                if (target == null) {
                    writer.println("FAIL: could not locate test bundle.");
                    return;
                }

                URL manifest = target.getEntry("META-INF/MANIFEST.MF");
                URL convertedManifest = urls.convertURL(manifest, target);
                URL rtManifest = new URL(convertedManifest.toExternalForm());

                try {
                    InputStream mf = rtManifest.openStream();
                    if (mf == null) {
                        writer.println("FAIL: unable to open manifest in target via round trip(null)");
                        return;
                    } else {
                        mf.close();
                    }

                    try {
                        target.stop();
                    } catch (BundleException e) {
                        writer.println("FAIL: unable to stop target bundle..");
                        return;
                    }

                    //this should now fail, as the bundle should be dead.
                    try {
                        InputStream mf2 = rtManifest.openStream();
                        if (mf2 != null) {
                            writer.println("FAIL: url still worked after bundle stop");
                            mf2.close();
                            return;
                        }
                    } catch (IOException io) {
                        //expected.
                    }

                    try {
                        target.start();
                    } catch (BundleException e) {
                        writer.println("FAIL: unable to start target bundle..");
                        return;
                    }

                    //this should still fail, as the url must be re-added.
                    try {
                        InputStream mf2 = rtManifest.openStream();
                        if (mf2 != null) {
                            writer.println("FAIL: url still worked after bundle stop-start");
                            mf2.close();
                            return;
                        }
                    } catch (IOException io) {
                        //expected.
                    }

                    //reregister the url, and retest.
                    manifest = target.getEntry("META-INF/MANIFEST.MF");
                    convertedManifest = urls.convertURL(manifest, target);
                    rtManifest = new URL(convertedManifest.toExternalForm());

                    //test...
                    mf = rtManifest.openStream();
                    if (mf == null) {
                        writer.println("FAIL: unable to open manifest in target via round trip(null)pt2");
                        return;
                    } else {
                        mf.close();
                    }

                } catch (IOException io) {
                    writer.println("FAIL: unable to open manifest in target via round trip." + io);
                    return;
                }

            } catch (IOException io) {
                writer.println("FAIL: unable to open stream.. " + io);
                return;
            } finally {
                try {
                    if (original != null)
                        original.close();
                    if (is != null)
                        is.close();
                    if (rt != null)
                        rt.close();
                } catch (IOException io) {
                }
            }

        } catch (IllegalStateException ise) {
            writer.println("FAIL: url conversion service threw unexpected IllegalStateException " + ise);
            return;
        } catch (MalformedURLException mue) {
            writer.println("FAIL: could not round trip url, due to " + mue);
            return;
        }
        writer.println("PASS: all tests complete");
    }
}
