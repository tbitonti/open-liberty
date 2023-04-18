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
 *         &lt;element name="reset2Return" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
                                  "reset2Return"
})
@XmlRootElement(name = "reset2Response")
public class Reset2Response {

    @XmlElement(required = true, nillable = true)
    protected String reset2Return;

    /**
     * Gets the value of the reset2Return property.
     *
     * @return
     *         possible object is
     *         {@link String }
     *
     */
    public String getReset2Return() {
        return reset2Return;
    }

    /**
     * Sets the value of the reset2Return property.
     *
     * @param value
     *            allowed object is
     *            {@link String }
     *
     */
    public void setReset2Return(String value) {
        this.reset2Return = value;
    }

}
