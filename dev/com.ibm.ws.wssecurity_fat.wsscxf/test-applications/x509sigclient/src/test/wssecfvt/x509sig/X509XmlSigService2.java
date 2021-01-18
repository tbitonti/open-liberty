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

package test.wssecfvt.x509sig;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.6.2
 * 2012-12-10T17:58:03.259-06:00
 * Generated source version: 2.6.2
 * 
 */
@WebServiceClient(name = "X509XmlSigService2", 
                  wsdlLocation = "X509XmlSig.wsdl",
                  targetNamespace = "http://x509sig.wssecfvt.test") 
public class X509XmlSigService2 extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://x509sig.wssecfvt.test", "X509XmlSigService2");
    public final static QName UrnX509Sig2 = new QName("http://x509sig.wssecfvt.test", "UrnX509Sig2");
    static {
        URL url = X509XmlSigService2.class.getResource("X509XmlSig.wsdl");
        if (url == null) {
            java.util.logging.Logger.getLogger(X509XmlSigService2.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "X509XmlSig.wsdl");
        }       
        WSDL_LOCATION = url;
    }

    public X509XmlSigService2(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public X509XmlSigService2(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public X509XmlSigService2() {
        super(WSDL_LOCATION, SERVICE);
    }
    
    /**
     *
     * @return
     *     returns X509XmlSig
     */
    @WebEndpoint(name = "UrnX509Sig2")
    public X509XmlSig getUrnX509Sig2() {
        return super.getPort(UrnX509Sig2, X509XmlSig.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns X509XmlSig
     */
    @WebEndpoint(name = "UrnX509Sig2")
    public X509XmlSig getUrnX509Sig2(WebServiceFeature... features) {
        return super.getPort(UrnX509Sig2, X509XmlSig.class, features);
    }

}
