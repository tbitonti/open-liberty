//
// Generated By:JAX-WS RI IBM 2.2.1-11/28/2011 08:27 AM(foreman)- (JAXB RI IBM 2.2.3-11/28/2011 06:17 AM(foreman)-)
//


package com.ibm.was.wssample.sei.echo;

import java.io.Serializable;
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
 *         &lt;element name="echoInput" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "echoInput"
})
@XmlRootElement(name = "echoStringInput")
public class EchoStringInput
    implements Serializable
{

    @XmlElement(required = true)
    protected String echoInput;

    /**
     * Gets the value of the echoInput property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEchoInput() {
        return echoInput;
    }

    /**
     * Sets the value of the echoInput property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEchoInput(String value) {
        this.echoInput = value;
    }

}
