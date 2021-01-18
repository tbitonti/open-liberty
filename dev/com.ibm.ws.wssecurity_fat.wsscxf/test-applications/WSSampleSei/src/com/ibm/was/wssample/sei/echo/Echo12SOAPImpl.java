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

package com.ibm.was.wssample.sei.echo;

import java.util.logging.Logger;

@javax.jws.WebService(endpointInterface = "com.ibm.was.wssample.sei.echo.EchoServicePortType",
                      targetNamespace = "http://com/ibm/was/wssample/sei/echo/",
                      serviceName = "Echo12Service",
                      wsdlLocation = "WEB-INF/wsdl/EchoBsp.wsdl",
                      portName = "Echo12ServicePort")
public class Echo12SOAPImpl {

    private static final Logger LOG = Logger.getLogger(Echo12SOAPImpl.class.getName());

    public EchoStringResponse echoOperation(EchoStringInput parameter) {
        LOG.info("(Echo12SoapImpl)Executing operation echoOperation");
        String strInput = (parameter == null ? "input_is_null" : parameter.getEchoInput());
        System.out.println("(WSSampleSei)Echo12SOAPImpl:" + parameter + ":" + strInput);
        try {
            com.ibm.was.wssample.sei.echo.EchoStringResponse _return = new EchoStringResponse();
            // Echo back
            _return.setEchoResponse("Echo12SOAPImpl>>" + strInput);
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

}