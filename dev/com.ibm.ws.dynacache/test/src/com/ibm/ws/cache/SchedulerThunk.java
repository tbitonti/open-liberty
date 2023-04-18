/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package com.ibm.ws.cache;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

public class SchedulerThunk {
    public static void setExecutorService(Scheduler scheduler, ExecutorService executorService) {
        scheduler.setExecutorService(executorService);
    }

    public static void unsetExecutorService(Scheduler scheduler, ExecutorService executorService) {
        scheduler.unsetExecutorService(executorService);
    }

    public static void setScheduledExecutorService(Scheduler scheduler, ScheduledExecutorService scheduledExecutorService) {
        scheduler.setExecutorService(scheduledExecutorService);
    }

    public static void unsetScheduledExecutorService(Scheduler scheduler, ScheduledExecutorService scheduledExecutorService) {
        scheduler.unsetExecutorService(scheduledExecutorService);
    }
}
