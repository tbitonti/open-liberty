/*
* IBM Confidential
*
* OCO Source Materials
*
* WLP Copyright IBM Corp. 2018
*
* The source code for this program is not published or otherwise divested
* of its trade secrets, irrespective of what has been deposited with the
* U.S. Copyright Office.
*/
package com.ibm.ws.cache.webservices;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jws.WebService;

/**
 *
 */
@WebService(endpointInterface = "com.ibm.ws.cache.webservices.CounterService1", serviceName = "CounterService1")
public class CounterService1Impl implements CounterService1 {

    Map<String, Integer> counts = new ConcurrentHashMap<String, Integer>();

    @Override
    public synchronized String counter1(String in) {
        System.out.println(getClass().getName() + ".counter1 called with " + in);
        Integer val = counts.get(in);
        if (val == null) {
            val = 0;
        }
        val = val + 1;
        counts.put(in, val);
        System.out.println("Returning " + val);
        return in + "-" + val.toString();
    }

    @Override
    public String reset1(String in) {
        Integer existingValue = counts.remove(in);
        return existingValue == null ? null : existingValue.toString();
    }

    @Override
    public String reset2(String in) {
        return null;
    }

    @Override
    public String counter2(String in) {
        return null;
    }
}
