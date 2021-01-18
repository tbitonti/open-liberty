package test.wssecfvt.x509crl;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.6.2
 * 2013-01-30T13:27:45.275-06:00
 * Generated source version: 2.6.2
 * 
 */
@WebServiceClient(name = "X509CrlInvCertService", 
                  wsdlLocation = "X509Crl.wsdl",
                  targetNamespace = "http://x509crl.wssecfvt.test") 
public class X509CrlInvCertService extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://x509crl.wssecfvt.test", "X509CrlInvCertService");
    public final static QName X509CrlInvCert = new QName("http://x509crl.wssecfvt.test", "X509CrlInvCert");
    static {
        URL url = X509CrlInvCertService.class.getResource("X509Crl.wsdl");
        if (url == null) {
            java.util.logging.Logger.getLogger(X509CrlInvCertService.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "X509Crl.wsdl");
        }       
        WSDL_LOCATION = url;
    }

    public X509CrlInvCertService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public X509CrlInvCertService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public X509CrlInvCertService() {
        super(WSDL_LOCATION, SERVICE);
    }

    /**
     *
     * @return
     *     returns X509Crl
     */
    @WebEndpoint(name = "X509CrlInvCert")
    public X509Crl getX509CrlInvCert() {
        return super.getPort(X509CrlInvCert, X509Crl.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns X509Crl
     */
    @WebEndpoint(name = "X509CrlInvCert")
    public X509Crl getX509CrlInvCert(WebServiceFeature... features) {
        return super.getPort(X509CrlInvCert, X509Crl.class, features);
    }

}
