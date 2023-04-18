/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
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
package com.ibm.ws.cache;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import com.ibm.websphere.ras.Tr;
import com.ibm.websphere.ras.TraceComponent;
import com.ibm.ws.ffdc.FFDCFilter;
import com.ibm.wsspi.kernel.service.location.WsLocationAdmin;

//@formatter:off
@Component(service = Scheduler.class,
           configurationPolicy = ConfigurationPolicy.IGNORE, property = "service.vendor=IBM")
public class Scheduler {
    private static TraceComponent tc =
        Tr.register(Scheduler.class, "WebSphere Dynamic Cache", "com.ibm.ws.cache.resources.dynacache");

    // Service activation ...
    //
    // The scheduler is implemented as a singleton, with the active scheduler being
    // stored as a static value within the scheduler class.

    public void activate() {
        setInstance(this);
    }

    public void deactivate() {
        setInstance(null);
    }

    //

    private static Scheduler activeScheduler;

    private static void setInstance(Scheduler value) {
        activeScheduler = value;
    }

    public static void createNonDeferrable(long sleepInterval, Object context, Runnable runnable) {
        if (activeScheduler == null) {
            return; // TODO: Is this safe?
        }

        try {
            activeScheduler.schedule(runnable, sleepInterval, TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            if (!(ex instanceof IllegalStateException)) {
                throw ex;
            } else {
                Tr.debug(tc, "IllegalStateException during createNonDeferrable, server likely shutting down");
            }
        }
    }

    public static Future<?> submit(Runnable runnable) {
        if (activeScheduler == null) {
            return null; // TODO: Is this safe?
        } else {
            return activeScheduler.submitTask(runnable);
        }
    }

    // Injected service reference: WsLocationAdmin

    private final AtomicReference<WsLocationAdmin> locationAdminRef =
        new AtomicReference<WsLocationAdmin>(null);

    @Reference(service = WsLocationAdmin.class)
    protected void setLocationAdmin(WsLocationAdmin vr) {
        if (tc.isDebugEnabled()) {
            Tr.debug(tc, "setWsLocationAdmin ", vr);
        }
        locationAdminRef.set(vr);
    }

    protected void unsetLocationAdmin(WsLocationAdmin vr) {
        locationAdminRef.compareAndSet(vr, null);
    }

    public static WsLocationAdmin getLocationAdmin() {
        return activeScheduler.locationAdminRef.get();
    }

    // Injected service reference: ExecutorService

    /** To schedule work "immediately */
    private final AtomicReference<ExecutorService> executorService =
        new AtomicReference<ExecutorService>(null);

    @Reference(service = ExecutorService.class, target = "(service.vendor=IBM)")
    protected void setExecutorService(ExecutorService es) {
        executorService.getAndSet(es);
    }

    protected void unsetExecutorService(ExecutorService es) {
        executorService.compareAndSet(es, null);
    }

    public ExecutorService getExecutorService() {
        ExecutorService eRef = executorService.get();
        if (eRef == null) {
            throw new IllegalStateException("ExecutorService service is unavailable");
        }
        return eRef;
    }

    private Future<?> submitTask(final Runnable runnable) {
        Runnable wrappedRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception ex) {
                    FFDCFilter.processException(ex, "com.ibm.ws.cache.Scheduler.submitTask(Runnable)", "77", this);
                    Tr.debug(tc, "exception during submitTask", ex.getCause());
                }
            }
        };
        return getExecutorService().submit(wrappedRunnable);
    }

    // Injected service reference: ScheduledExecutorService

    /** Injected Scheduled executor service Replacement for AlarmManager */
    private final AtomicReference<ScheduledExecutorService> scheduledExecutorService =
        new AtomicReference<ScheduledExecutorService>(null);

    /**
     * Inject a ServiceReference for the required/dynamic ScheduledExecutorService.
     *
     * If the service is different than the previous value, we need to queue established
     * monitors to cancel their tasks with the old executor service, and reschedule them
     * with the new one.
     *
     * @param scheduler The scheduler which is to be injected.
     */

    @Reference(service = ScheduledExecutorService.class, target = "(deferrable=false)")
    protected void setScheduledExecutorService(ScheduledExecutorService scheduler) {
        scheduledExecutorService.getAndSet(scheduler);
    }

    /**
     * Remove the reference to the required/dynamic ScheduledExecutorService service.
     *
     * Take care with removal: This is a dynamic reference. Should the instance of the
     * bound location service go away _and_ there is a replacement service already registered,
     * DS will bind to the replacement first (via setLocation) before calling unset to unbind
     * the old one.
     *
     * @param scheduler The scheduler which is to be removed.
     */
    protected void unsetScheduledExecutorService(ScheduledExecutorService scheduler) {
        scheduledExecutorService.compareAndSet(scheduler, null);
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        ScheduledExecutorService sRef = scheduledExecutorService.get();
        if (sRef == null) {
            throw new IllegalStateException("ScheduledExecutorService service is unavailable");
        }
        return sRef;
    }

    private void schedule(final Runnable runnable, long sleepInterval, TimeUnit timeUnit) {
        Runnable wrappedRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception ex) {
                    FFDCFilter.processException(ex, "com.ibm.ws.cache.Scheduler.schedule(Runnable, long, TimeUnit)", "77", this);
                    Tr.debug(tc, "exception during schedulre", ex.getCause());
                }
            }
        };

        getScheduledExecutorService().schedule(wrappedRunnable, sleepInterval, timeUnit);
    }
}
//@formatter:on