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

import javax.xml.bind.annotation.XmlRegistry;

/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the com.ibm.ws.cache.webservices package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups. Factory methods for each of these are
 * provided in this class.
 *
 */
@XmlRegistry
public class ObjectFactory {

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.ibm.ws.cache.webservices
     *
     */
    public ObjectFactory() {}

    /**
     * Create an instance of {@link Counter2 }
     *
     */
    public Counter2 createCounter2() {
        return new Counter2();
    }

    /**
     * Create an instance of {@link Counter2Response }
     *
     */
    public Counter2Response createCounter2Response() {
        return new Counter2Response();
    }

    /**
     * Create an instance of {@link Counter1 }
     *
     */
    public Counter1 createCounter1() {
        return new Counter1();
    }

    /**
     * Create an instance of {@link Reset2Response }
     *
     */
    public Reset2Response createReset2Response() {
        return new Reset2Response();
    }

    /**
     * Create an instance of {@link Counter1Response }
     *
     */
    public Counter1Response createCounter1Response() {
        return new Counter1Response();
    }

    /**
     * Create an instance of {@link Reset1Response }
     *
     */
    public Reset1Response createReset1Response() {
        return new Reset1Response();
    }

    /**
     * Create an instance of {@link Reset2 }
     *
     */
    public Reset2 createReset2() {
        return new Reset2();
    }

    /**
     * Create an instance of {@link Reset1 }
     *
     */
    public Reset1 createReset1() {
        return new Reset1();
    }

}
