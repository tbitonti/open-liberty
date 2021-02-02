package com.ibm.ws.javaee.ddmodel.wsbnd.adapter;

import com.ibm.ws.javaee.ddmodel.DDParser;
import com.ibm.ws.javaee.ddmodel.wsbnd.WebservicesBnd;
import com.ibm.ws.javaee.ddmodel.wsbnd.impl.WebservicesBndType;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;

public class WebServicesBndDDParser extends DDParser {
    public WebServicesBndDDParser(Container ddRootContainer, Entry ddEntry)
        throws ParseException {
        super(ddRootContainer, ddEntry);
    }

    @Override
    public WebservicesBnd parse() throws ParseException {
        super.parseRootElement();
        return (WebservicesBnd) rootParsable;
    }

    public static final String WEBSERVICES_BND_ELEMENT_NAME = "webservices-bnd";
    
    @Override
    protected ParsableElement createRootParsable() throws ParseException {
        requireRootElement(WEBSERVICES_BND_ELEMENT_NAME);
        requireNamespace("http://websphere.ibm.com/xml/ns/javaee");
        version = 10;
        return new WebservicesBndType(getDeploymentDescriptorPath());
    }
}
