/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * Copyright IBM Corp. 2012
 *
 * The source code for this program is not published or otherwise divested 
 * of its trade secrets, irrespective of what has been deposited with the 
 * U.S. Copyright Office.
 */
package com.ibm.ws.artifact.url.test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 */
public class ConnectionTest {

    private static final String TEST_NAME = "WSJarTest";

    private static final String CONTENT_TYPE_UNKNOWN = "content/unknown";
    private static final String CONTENT_TYPE_XML = "application/xml";

    static void test(PrintWriter writer) {
        testConnection(false, writer);
        testConnection(true, writer);
    }

    private static void testConnection(boolean wsjar, PrintWriter writer) {
        testConnectValidEntry(wsjar, writer);
        testConnectNonexistentJar(wsjar, writer);
        testConnectNonexistentEntry(wsjar, writer);

        testGetInputStreamValidEntry(wsjar, writer);
        testGetInputStreamNonexistentJar(wsjar, writer);
        testGetInputStreamNonexistentEntry(wsjar, writer);

        testGetContentTypeValidEntry(wsjar, writer);
        testGetContentTypeNonexistentJar(wsjar, writer);
        testGetContentTypeNonexistentEntry(wsjar, writer);
        testGetContentTypeNonexistentNamedEntry(wsjar, writer);
    }

    private static URL createURL(boolean wsjar, String jar, String entry, PrintWriter writer) {
        if (jar == null) {
            jar = "wsjar.jar";
        }
        if (entry == null) {
            entry = "/entry-txt";
        }

        URL url = ConnectionTest.class.getResource("/WEB-INF/lib/");
        String protocol = wsjar ? "wsjar" : "jar";

        try {
            return new URL(protocol + ":" + url + jar + "!" + entry);
        } catch (MalformedURLException ex) {
            writer.println("FAIL: Unable to build url requested during connectiontest " + protocol + ":" + url + jar + "!" + entry);
            ex.printStackTrace(writer);
            throw new RuntimeException("URL Creation failure: " + ex.getMessage(), ex);
        }
    }

    private static void triggerEvent(boolean wsjar, String name, PrintWriter writer) {
        writer.println("PASS: " + TEST_NAME + " " + (wsjar ? "wsjar" : "jar") + "-" + name);
    }

    private static class EqualityAssert {
        private final String testName;
        private final String assertName;
        private final PrintWriter writer;

        public EqualityAssert(String testName, String assertName, PrintWriter writer) {
            this.testName = testName;
            this.assertName = assertName;
            this.writer = writer;
        }

        public void assertEquals(String wanted, String got) {
            if (wanted == null) {
                if (got == wanted) {
                    writer.println("PASS: " + testName + " " + assertName);
                } else {
                    writer.println("FAIL: " + testName + " " + assertName);
                }
            } else {
                if (wanted.equals(got)) {
                    writer.println("PASS: " + testName + " " + assertName);
                } else {
                    writer.println("FAIL: " + testName + " " + assertName);
                }
            }
        }
    }

    private static EqualityAssert createAssertion(boolean wsjar, String name, PrintWriter writer) {
        return new EqualityAssert(TEST_NAME, (wsjar ? "wsjar" : "jar") + "-" + name, writer);
    }

    private static void testConnectValidEntry(boolean wsjar, PrintWriter writer) {
        URLConnection conn;
        try {
            conn = createURL(wsjar, null, null, writer).openConnection();
        } catch (Exception ex) {
            writer.println("FAIL: " + ex.getMessage());
            ex.printStackTrace(writer);
            return;
        }

        try {
            conn.connect();
            triggerEvent(wsjar, "connect-valid", writer);
        } catch (IOException ex) {
            writer.println("FAIL: connect-valid " + ex.getMessage());
            ex.printStackTrace(writer);
        }
    }

    private static void testConnectNonexistentJar(boolean wsjar, PrintWriter writer) {
        URLConnection conn;
        try {
            conn = createURL(wsjar, "nonexistent.jar", null, writer).openConnection();
        } catch (Exception ex) {
            writer.println("FAIL: " + ex.getMessage());
            ex.printStackTrace(writer);
            return;
        }

        try {
            conn.connect();
            writer.println("FAIL: connect-nonexistent-jar connected.");
        } catch (IOException ex) {
            triggerEvent(wsjar, "connect-nonexistent-jar", writer);
        }
    }

    private static void testConnectNonexistentEntry(boolean wsjar, PrintWriter writer) {
        URLConnection conn;
        try {
            conn = createURL(wsjar, null, "/nonexistent", writer).openConnection();
        } catch (Exception ex) {
            writer.println("FAIL: " + ex.getMessage());
            ex.printStackTrace(writer);
            return;
        }

        try {
            conn.connect();
            writer.println("FAIL: connect-nonexistent-entry connected.");
        } catch (FileNotFoundException ex) {
            triggerEvent(wsjar, "connect-nonexistent-entry", writer);
        } catch (IOException ex) {
            writer.println("FAIL: connect-nonexistent-entry " + ex.getMessage());
            ex.printStackTrace(writer);
        }
    }

    private static void testGetInputStreamValidEntry(boolean wsjar, PrintWriter writer) {
        try {
            InputStream input = createURL(wsjar, null, null, writer).openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
            String line = reader.readLine();
            if ("test".equals(line)) {
                input.close();
                triggerEvent(wsjar, "getInputStream-valid", writer);
            } else {
                writer.println("FAIL: getInputStream-valid content not as expected. Got " + line);
            }
        } catch (Exception ex) {
            writer.println("FAIL: " + ex.getMessage());
            ex.printStackTrace(writer);
        }
    }

    private static void testGetInputStreamNonexistentJar(boolean wsjar, PrintWriter writer) {
        URLConnection conn;
        try {
            conn = createURL(wsjar, "nonexistent.jar", null, writer).openConnection();
        } catch (Exception ex) {
            writer.println("FAIL: " + ex.getMessage());
            ex.printStackTrace(writer);
            return;
        }

        try {
            conn.getInputStream();
            writer.println("FAIL: getInputStream-nonexistent-jar succeeded when it should have failed");
        } catch (IOException ex) {
            triggerEvent(wsjar, "getInputStream-nonexistent-jar", writer);
        }
    }

    private static void testGetInputStreamNonexistentEntry(boolean wsjar, PrintWriter writer) {
        URLConnection conn;
        try {
            conn = createURL(wsjar, null, "/nonexistent", writer).openConnection();
        } catch (Exception ex) {
            writer.println("FAIL: " + ex.getMessage());
            ex.printStackTrace(writer);
            return;
        }

        try {
            conn.getInputStream();
            writer.println("FAIL: getInputStream-nonexistent-entry succeeded when it should have failed");
        } catch (FileNotFoundException ex) {
            triggerEvent(wsjar, "getInputStream-nonexistent-entry", writer);
        } catch (IOException ex) {
            writer.println("FAIL: " + ex.getMessage());
            ex.printStackTrace(writer);
        }
    }

    private static void testGetContentTypeValidEntry(boolean wsjar, PrintWriter writer) {
        try {
            String contentType = createURL(wsjar, null, "/entry-xml", writer).openConnection().getContentType();
            createAssertion(wsjar, "getContentType-valid", writer).assertEquals(contentType, CONTENT_TYPE_XML);
        } catch (Exception ex) {
            writer.println("FAIL: " + ex.getMessage());
            ex.printStackTrace(writer);
        }
    }

    private static void testGetContentTypeNonexistentJar(boolean wsjar, PrintWriter writer) {
        try {
            String contentType = createURL(wsjar, "nonexistent.jar", "/nonexistent", writer).openConnection().getContentType();
            createAssertion(wsjar, "getContentType-nonexistent-jar", writer).assertEquals(contentType, CONTENT_TYPE_UNKNOWN);
        } catch (Exception ex) {
            writer.println("FAIL: " + ex.getMessage());
            ex.printStackTrace(writer);
        }
    }

    private static void testGetContentTypeNonexistentEntry(boolean wsjar, PrintWriter writer) {
        try {
            String contentType = createURL(wsjar, null, "/nonexistent", writer).openConnection().getContentType();
            createAssertion(wsjar, "getContentType-nonexistent-entry", writer).assertEquals(contentType, CONTENT_TYPE_UNKNOWN);
        } catch (Exception ex) {
            writer.println("FAIL: " + ex.getMessage());
            ex.printStackTrace(writer);
        }
    }

    private static void testGetContentTypeNonexistentNamedEntry(boolean wsjar, PrintWriter writer) {
        try {
            String contentType = createURL(wsjar, null, "/nonexistent.xml", writer).openConnection().getContentType();
            createAssertion(wsjar, "getContentType-nonexistent-named-entry", writer).assertEquals(contentType, CONTENT_TYPE_XML);
        } catch (Exception ex) {
            writer.println("FAIL: " + ex.getMessage());
            ex.printStackTrace(writer);
        }
    }
}
