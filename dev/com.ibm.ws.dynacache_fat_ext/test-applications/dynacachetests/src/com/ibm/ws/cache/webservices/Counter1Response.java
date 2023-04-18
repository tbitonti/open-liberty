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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="counter1Return" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
                                  "counter1Return"
})
@XmlRootElement(name = "counter1Response")
public class Counter1Response {

    @XmlElement(required = true, nillable = true)
    protected String counter1Return;

    /**
     * Gets the value of the counter1Return property.
     *
     * @return
     *         possible object is
     *         {@link String }
     *
     */
    public String getCounter1Return() {
        return counter1Return;
    }

    /**
     * Sets the value of the counter1Return property.
     *
     * @param value
     *            allowed object is
     *            {@link String }
     *
     */
    public void setCounter1Return(String value) {
        this.counter1Return = value;
    }

}
