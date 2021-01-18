/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.was.cxfsample.sei.echo;

import java.util.logging.Logger;
// import javax.jws.WebMethod;
// import javax.jws.WebParam;
// import javax.jws.WebResult;
// import javax.jws.WebService;
// import javax.jws.soap.SOAPBinding;
// import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * This class was generated by Apache CXF 2.6.2
 * 2013-02-27T11:59:38.678-06:00
 * Generated source version: 2.6.2
 * 
 */

@javax.jws.WebService(
                      serviceName = "EchoService",
                      portName = "EchoServicePort",
                      targetNamespace = "http://com/ibm/was/cxfsample/sei/echo/",
                      wsdlLocation = "WEB-INF/Echo.wsdl",
                      endpointInterface = "com.ibm.was.cxfsample.sei.echo.EchoServicePortType")
                      
public class EchoServicePortTypeImpl implements EchoServicePortType {

    private static final Logger LOG = Logger.getLogger(EchoServicePortTypeImpl.class.getName());

    /* (non-Javadoc)
     * @see com.ibm.was.cxfsample.sei.echo.EchoServicePortType#echoOperation(com.ibm.was.cxfsample.sei.echo.EchoStringInput  parameter )*
     */
    public com.ibm.was.cxfsample.sei.echo.EchoStringResponse echoOperation(EchoStringInput parameter) { 
        LOG.info("Executing operation echoOperation");
        String strInput = (parameter == null ? "input_is_null" : parameter.getEchoInput() );
        System.out.println("EchoServicePortTypeImpl:"+parameter+":"+ strInput);
        try {
            com.ibm.was.cxfsample.sei.echo.EchoStringResponse _return = new EchoStringResponse();
            String strPrincipal = CallerUtil.getPrincipalUserID();
            if(strPrincipal == null ){
                strPrincipal = "ID_UNKNOWN";
            }
            // Echo back
            _return.setEchoResponse( "ID:" + strPrincipal + ":" + strInput );
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

}
