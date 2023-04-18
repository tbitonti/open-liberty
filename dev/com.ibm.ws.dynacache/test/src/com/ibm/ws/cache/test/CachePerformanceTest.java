/*******************************************************************************
 * Copyright (c) 1997,2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.cache.test;

import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.ws.cache.CacheEntry;

//@formatter:off
public class CachePerformanceTest extends BaseTest {

    public static String getCacheName() {
        return CachePerformanceTest.class.getSimpleName();
    }

    @BeforeClass
    public static void setup() throws Exception {
        BaseTest.setupCache( BaseTest.createCacheConfig(getCacheName()),
                                BaseTest::initializeServerCache );
    }

    @AfterClass
    public static void teardown() throws Exception {
        BaseTest.tearDownCache();
    }

    @Before
    public void setupTest() {
        super.setupTest( getCacheName() );
    }

    @After
    public void teardownTest() {
        super.teardownTest( getCacheName() );
    }

    //

    @Test
    public void testConcurrentActivity() throws Exception {
        populateCache(UNSET_TIME_LIMIT);

        ActionThreads actionThreads = new ActionThreads(IDS);
        actionThreads.start();
        Thread.sleep(30000);
        actionThreads.stop();
        actionThreads.yield();

        actionThreads.display();
    }

    //

    public enum Action {
        UPDATE,
        VALIDATE,
        INVALIDATE;

        public static final int LENGTH = Action.values().length;
    }

    private class ActionThread extends Thread {
        public ActionThread(ActionThreads actionThreads,
                            String tag,
                            BooleanSupplier keepRunning,
                            Predicate<String> action,
                            BooleanSupplier consumeAction,
                            String[] ids, long idSelectorSeed,
                            int batchSize) {

            this.actionThreads = actionThreads;

            this.tag = tag;
            this.action = action;

            this.consumeAction = consumeAction;

            this.ids = ids;
            this.idSelectorSeed = idSelectorSeed;
            this.idSelector = new Random(idSelectorSeed);

            this.batchSize = batchSize;

            this.actions = 0;
            this.actionsFailed = 0;
        }

        private final ActionThreads actionThreads;

        public ActionThreads getActionThreads() {
            return actionThreads;
        }

        private final String tag;

        public String getTag() {
            return tag;
        }

        private final int batchSize;

        public int getBatchSize() {
            return batchSize;
        }

        private int actions;
        private int actionsFailed;

        public int getActions() {
            return actions;
        }

        public int getActionsFailed() {
            return actionsFailed;
        }

        public void acted(boolean failed) {
            actions++;
            if (failed) {
                actionsFailed++;
            }
        }

        public BooleanSupplier keepRunning;

        public boolean keepRunning() {
            return keepRunning.getAsBoolean();
        }

        public Predicate<String> action;

        public void act(String id) {
            acted(action.test(id));
        }

        public BooleanSupplier consumeAction;

        public boolean consumeAction() {
            return consumeAction.getAsBoolean();
        }

        private final String[] ids;
        @SuppressWarnings("unused")
        private final long idSelectorSeed;
        private final Random idSelector;

        private String selectId() {
            return ids[ idSelector.nextInt(ids.length) ];
        }

        @Override
        public void run() {
            String m = getTag() + ".run";
            System.out.println(m + ": starting ...");

            while (keepRunning()) {
                synchronized (getActionThreads()) {
                    if (consumeAction()) {
                        int useBatchSize = getBatchSize();
                        for ( int idNo = 0; idNo < useBatchSize; idNo++ ) {
                            act( selectId() );
                        }
                    }
                }

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    System.out.println(m + ": ... interrupted");
                    return;
                }
            }

            System.out.println(m + ": ... completed");
        }

        public void display() {
            System.out.println("Activity [ " + getTag() + " ]");

            int useActions = getActions();
            int useFailed = getActionsFailed();
            System.out.println("  Actions [ " + useActions + " ] Failed [ " + useFailed + " ]");

            float failureRate = ((float) useFailed) / ((float) useActions);
            System.out.println("  Failure rate: [ " + String.format("%4.2f", failureRate) + " ]");
        }
    }

    private static final long ACTIONS_SEED = 77L;

    private static final long UPDATE_SEED = 91L;
    private static final long INVALIDATE_SEED = 93L;
    private static final long VALIDATE_SEED = 97L;

    private static final int BATCH_SIZE = 20;

    private class ActionThreads {
        public ActionThreads(String[] ids) {
            this.ids = ids;

            this.updateThread = createUpdateThread();
            this.validateThread = createValidateThread();
            this.invalidateThread = createInvalidateThread();

            this.keepRunning = false;

            this.actionSeed = ACTIONS_SEED;
            this.numActions = Action.LENGTH;
            this.actionSelector = new Random(this.actionSeed);

            this.nextAction = null;
        }

        public void display() {
            System.out.println("IDs [ " + ids.length + " ]");
            System.out.println("Action Seed [ " + actionSeed + " ]");

            updateThread.display();
            validateThread.display();
            invalidateThread.display();
        }

        private final String[] ids;

        public String[] getIds() {
            return ids;
        }

        public boolean keepRunning;

        public synchronized void start() {
            keepRunning = true;
            nextAction = nextAction();

            updateThread.start();
            validateThread.start();
            invalidateThread.start();
        }

        public synchronized boolean keepRunning() {
            return keepRunning;
        }

        public synchronized void stop() {
            keepRunning = false;
            nextAction = null;
        }

        public void yield() {
            try {
                updateThread.join();
            } catch ( InterruptedException e ) {
                // Ignore
            }
            try {
                invalidateThread.join();
            } catch ( InterruptedException e ) {
                // Ignore
            }
            try {
                validateThread.join();
            } catch ( InterruptedException e ) {
                // Ignore
            }
        }

        //

        private final Long actionSeed;
        private final int numActions;
        private final Random actionSelector;

        public Action nextAction;

        protected Action nextAction() {
            return Action.values()[actionSelector.nextInt(numActions)];
        }

        protected boolean consume(Action action) {
            if (isAction(action)) {
                setAction(nextAction());
                return true;
            } else {
                return false;
            }
        }

        public boolean isAction(Action nextAction) {
            return this.nextAction == nextAction;
        }

        public void setAction(Action nextAction) {
            this.nextAction = nextAction;
        }

        //

        private final ActionThread updateThread;
        private final ActionThread invalidateThread;
        private final ActionThread validateThread;

        private ActionThread createUpdateThread() {
            Predicate<String> updateAction = ((String id) -> {
                CacheEntry setEntry = createEntry(id);
                setEntry(setEntry);

                CacheEntry getEntry = getEntry(id);
                return (getEntry == null);
            });

            return new ActionThread(this,
                "update",
                () -> keepRunning(),
                updateAction,
                () -> consume(Action.UPDATE),
                getIds(), UPDATE_SEED, BATCH_SIZE);
        }

        private ActionThread createValidateThread() {
            Predicate<String> validateAction = ((String id) -> {
                CacheEntry entry = getEntry(id);
                return (entry == null);
            });

            return new ActionThread(this,
                "validate",
                () -> keepRunning(),
                validateAction,
                () -> consume(Action.VALIDATE),
                getIds(), VALIDATE_SEED, BATCH_SIZE);
        }

        private ActionThread createInvalidateThread() {
            Predicate<String> invalidateAction = ((String id) -> {
                invalidateEntry(id, !DO_WAIT);
                return true;
            });

            return new ActionThread(this,
                "invalidate",
                () -> keepRunning(),
                invalidateAction,
                () -> consume(Action.INVALIDATE),
                getIds(), INVALIDATE_SEED, BATCH_SIZE);
        }
    }
}
//@formatter:on