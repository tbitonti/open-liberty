/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.artifact.lrucache.util;

public class ArrayUtil {
    public static boolean IS_ADD = true;

    public static int[] update(int[] values, long value, boolean isAdd) {
        boolean didAlloc;
        
        int log2 = (value == 0) ? 0 : (log2(value) + 1);

        if ( (didAlloc = (log2 >= values.length)) ) {        
            if ( isAdd ) {
                values = realloc(values, log2);
            } else {
                throw new IllegalArgumentException("Value [ " + value + " ] not currently stored");
            }
        }

        if ( isAdd ) {
            values[log2]++;
        } else {
            values[log2]--;
        }

        return didAlloc ? values : null;
    }    

    public static int[] alloc(int length) {
        return new int[length];
    }

    public static int[] realloc(int[] values, int newLength) {
        int[] newValues = new int[ newLength ];
        System.arraycopy(values, 0, newValues, 0, values.length);
        return newValues;
    }    
    
    public static int log2(long value){
        if ( value <= 0 ) {
            throw new IllegalArgumentException();
        } else {
            return 31 - Long.numberOfLeadingZeros(value);
        }
    }     
}
