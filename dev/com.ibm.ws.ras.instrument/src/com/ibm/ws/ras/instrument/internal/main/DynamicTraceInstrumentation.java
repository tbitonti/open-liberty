/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.ras.instrument.internal.main;

import java.lang.instrument.Instrumentation;

/**
 * Hook to install a trace injection class transformer using java
 * supplied class instrumentation API.
 * 
 * See {@link Instrumentation#addTransformer(java.lang.instrument.ClassFileTransformer)}.
 *
 * This implementation uses a {@link StaticTransformer} instance which is initialized with
 * properties unpacked from a packed properties value.  See {@link #parse} for property
 * packing details. 
 */
public class DynamicTraceInstrumentation {
    public static void premain(String packedArgs, Instrumentation inst) {
        inst.addTransformer( new StaticTransformer(packedArgs, !StaticTransformer.IS_QUIET ) );
    }
}
