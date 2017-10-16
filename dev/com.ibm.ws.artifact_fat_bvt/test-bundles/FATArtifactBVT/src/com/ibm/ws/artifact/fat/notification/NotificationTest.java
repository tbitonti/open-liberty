/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * Copyright IBM Corp. 2012, 2017
 *
 * The source code for this program is not published or otherwise divested 
 * of its trade secrets, irrespective of what has been deposited with the 
 * U.S. Copyright Office.
 */
package com.ibm.ws.artifact.fat.notification;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import com.ibm.wsspi.artifact.factory.ArtifactContainerFactory;
import com.ibm.wsspi.artifact.overlay.OverlayContainerFactory;

/**
 *
 */
public class NotificationTest {

    private abstract class FSTestThread extends Thread {
        private final String testName;
        private final String testId;
        private final StringWriter data;
        protected final PrintWriter out;
        protected volatile Boolean result = null;

        public FSTestThread(String testName, String testId) {
            super(testName + ":" + testId);
            this.testName = testName;
            this.testId = testId;
            this.data = new StringWriter();
            this.out = new PrintWriter(data);
        }

        protected ArtifactNotificationTest getCloneOfArtifactNotifierTest(ArtifactNotificationTest ant) throws IllegalStateException {
            try {
                //use reflection to get a new instance of our test class
                ArtifactNotificationTest a = (ArtifactNotificationTest) Class.forName(ant.getClass().getName()).newInstance();
                return a;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new IllegalStateException(e);
            } catch (InstantiationException e) {
                e.printStackTrace();
                throw new IllegalStateException(e);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new IllegalStateException(e);
            }
        }

        public Boolean getResult() {
            return this.result;
        }

        public String getOutput() {
            return this.data.toString();
        }
    }

    private Set<FSTestThread> collectTests(final ArtifactNotificationTest ant, final File testDataBaseDir, final ArtifactContainerFactory acf,
                                                  final OverlayContainerFactory ocf) throws IllegalStateException {
        Set<FSTestThread> t = new HashSet<FSTestThread>();

        //for each method, add a thread to sort it out.. 

        t.add(new FSTestThread(ant.getClass().getSimpleName(), "SoleNRoot") {
            @Override
            public void run() {
                try {
                    ArtifactNotificationTest copy = getCloneOfArtifactNotifierTest(ant);
                    boolean r = copy.setup(ant.getClass().getSimpleName() + ".SoleNRoot", testDataBaseDir, acf, ocf, out);
                    if (r)
                        r = copy.runSingleNonRootListenerTest(out);
                    if (r)
                        copy.tearDown(out);
                    this.result = r;
                } catch (Throwable t) {
                    out.println("FAIL: test threw execption " + t.getClass().getName());
                    t.printStackTrace(out);
                    this.result = false;
                }
            }
        });

        return t;
    }

    public void doNotifierTest(File testDataBaseDir, ArtifactContainerFactory acf, OverlayContainerFactory ocf, PrintWriter out) {
        ArtifactNotificationTest[] notifierTests = new ArtifactNotificationTest[] {
                                                                                   new FileArtifactNotifierTest()
                                                                                   , new JarArtifactNotifierTest()
                                                                                   , new BundleArtifactNotifierTest()
                                                                                   , new LooseArtifactNotifierTest()
                                                                                   };

        Set<FSTestThread> tests = new HashSet<FSTestThread>();
        for (ArtifactNotificationTest ant : notifierTests) {
            Set<FSTestThread> currentSet = null;
            try {
                currentSet = collectTests(ant, testDataBaseDir, acf, ocf);
            } catch (IllegalStateException e) {
                out.println("FAIL: suite test for artifact notfier " + ant.getClass().getName() + " reported a failure during test gathering. " + e.getCause().getMessage());
                e.getCause().printStackTrace(out);
            }
            tests.addAll(currentSet);
        }

        //Particularly wimpy machines are failing with a 60 second timeout.
        //Changing this to 120 to prevent future errors. 
        int TOTALWAIT = 120 * 1000; // set the total wait time to be 1 minute. 
        int WAIT = 50; // interval at which to poll threads for results.. 

        long time = System.currentTimeMillis();

        for (Thread t : tests) {
            t.start();
        }

        boolean allPassed = true;
        long start = System.currentTimeMillis();
        long end = start + TOTALWAIT;
        while (!tests.isEmpty() && System.currentTimeMillis() < end) {
            try {
                Thread.sleep(WAIT);
            } catch (InterruptedException e) {
                //no op.
            }

            Set<FSTestThread> done = new HashSet<FSTestThread>();
            for (FSTestThread t : tests) {
                if (t.getResult() != null) {
                    done.add(t);
                }
            }
            tests.removeAll(done);

            for (FSTestThread t : done) {
                out.print(t.getOutput());
                allPassed &= t.getResult();
                out.println("Test took " + (System.currentTimeMillis() - time) + "ms");
                if (t.getResult()) {
                    out.println("PASS: " + t.getName());
                }
                out.println(""); //add blank line to help formatting.
            }
        }

        if (!tests.isEmpty()) {
            String fails = "";
            for (FSTestThread t : tests) {
                fails += " " + t.getName();

                out.println("--INCOMPLETE( " + t.getName() + " )--");
                out.println(t.getOutput());
            }
            out.println("FAIL: tests did not complete within " + TOTALWAIT + "ms.. (" + fails + ")");

        } else {
            if (allPassed)
                out.println("PASS: all tests completed on time");
            else
                out.println("FAIL: all tests completed on time, but some reported failures");
        }

    }
}
