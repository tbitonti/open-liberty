/*******************************************************************************
 * Copyright (c) 2011, 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.javaee.ddmodel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.ibm.websphere.ras.Tr;
import com.ibm.websphere.ras.TraceComponent;
import com.ibm.websphere.ras.annotation.Sensitive;
import com.ibm.websphere.ras.annotation.Trivial;
import com.ibm.ws.container.service.app.deploy.ModuleInfo;
import com.ibm.ws.ffdc.FFDCFilter;
import com.ibm.ws.ffdc.annotation.FFDCIgnore;
import com.ibm.ws.kernel.service.util.DesignatedXMLInputFactory;
import com.ibm.wsspi.adaptable.module.Container;
import com.ibm.wsspi.adaptable.module.Entry;
import com.ibm.wsspi.adaptable.module.NonPersistentCache;
import com.ibm.wsspi.adaptable.module.UnableToAdaptException;

public abstract class DDParser {
    private static final TraceComponent tc = Tr.register(DDParser.class);   

    // Basic XML structure XML errors:

    // com.ibm.ws.javaee.ddmodel.DDParser.createReader(InputStream)
    //     XMLInputFactory.createXMLStreamReader
    // com.ibm.ws.javaee.ddmodel.DDParser.parse(ParsableElement)
    //    XMLStreamReader hasNext, next, close
    // com.ibm.ws.javaee.ddmodel.DDParser.parseToRootElement()
    //    XMLStreamReader hasNext, next
    // com.ibm.ws.javaee.ddmodel.DDParser.skipSubtree()
    //    XMLStreamReader hasNext, next

    private String xmlStreamError() {
        // xml.error=
        // CWWKC2272E: An error occurred
        // while parsing the {0} deployment descriptor on line {1}.
        // The error message was: {2}
        
        // TODO:
        // xml.error=
        // CWWKC2272E: An error occurred
        // while parsing the {0} deployment descriptor on line {1}.

        return Tr.formatMessage(tc, "xml.error",
            ddEntryPath, getLineNumber());
    }

    // com.ibm.ws.javaee.ddmodel.DDParser.parseRootElement()
    //     UnableToAdaptException: Entry -> InputStream

    // TODO:
    private String openError() {
        // xml.open.error=
        // CWWKCXXXXE: An error occurred
        // while attempting to open an input stream
        // to be used to parse the {0} deployment descriptor.

        return Tr.formatMessage(tc, "xml.open.error", ddEntryPath);
    }
    
    // Starting element errors ...

    // com.ibm.ws.javaee.ddmodel.app.ApplicationDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.client.ApplicationClientDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.ejb.EJBJarDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.jsf.FacesConfigDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.web.WebAppDDParser.createRootParsable()

    /**
     * Require a version attribute, or require that a public ID has
     * been set.
     *
     * @return The value of the version attribute.
     *
     * @throws ParseException Thrown in case an illegal combination of
     *     version and public ID.
     */
    protected String requireVersionOrDTD() throws ParseException {
        String vers = getAttributeValue("", "version");
        if (vers == null ) {
            // A namespace is not allowed without a version.
            if (namespace != null) {
                throw new ParseException(errorBareNamespace());
            }
            // Either a version or a public ID must be provided.
            if (dtdPublicId == null) {
                throw new ParseException(errorMissingHeader());
            }
            // TODO: We could check for there being a version and
            //       a public ID.
        }
        return vers;
    }

    // TODO:    
    protected String errorMissingHeader() {
        // invalid.deployment.descriptor.missing.header=
        // CWWKCXXXXE: No version or public ID were supplied
        // in the {0} deployment descriptor.

        return Tr.formatMessage(tc, "invalid.deployment.descriptor.missing.header", 
            ddEntryPath);
    }

    // TODO:
    protected String errorBareNamespace() {
        // invalid.deployment.descriptor.namespace.without.version=
        // CWWKCXXXXE: No version was supplied with the {2} namespace
        // in the {0} deployment descriptor on line {1}.        
        
        return Tr.formatMessage(tc, "invalid.deployment.descriptor.namespace.without.version", 
            ddEntryPath, getLineNumber(),
            namespace);         
    }

    // com.ibm.ws.javaee.ddmodel.app.ApplicationDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.client.ApplicationClientDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.ejb.EJBJarDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.jsf.FacesConfigDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.web.WebAppDDParser.createRootParsable()

    // TODO:
    protected String errorInvalidPublicId() {
        // invalid.deployment.descriptor.public.id=
        // CWWKCXXXXE: A non-valid {2} public ID is present
        // in the {0} deployment descriptor on line {1}.

        return Tr.formatMessage(tc, "invalid.deployment.descriptor.public.id",
            ddEntryPath, getLineNumber(),
            dtdPublicId);
    }

    // com.ibm.ws.javaee.ddmodel.permissions.PermissionsConfigDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.web.WebFragmentDDParser.createRootParsable()
    
    // com.ibm.ws.javaee.ddmodel.appbnd.ApplicationBndDDParser.createXMLRootParsable()
    // com.ibm.ws.javaee.ddmodel.appext.ApplicationExtDDParser.createXMLRootParsable()
    // com.ibm.ws.javaee.ddmodel.clientbnd.ApplicationClientBndDDParser.createXMLRootParsable()
    // com.ibm.ws.javaee.ddmodel.ejbbnd.EJBJarBndDDParser.createXMLRootParsable()
    // com.ibm.ws.javaee.ddmodel.ejbext.EJBJarExtDDParser.createXMLRootParsable()
    // com.ibm.ws.javaee.ddmodel.managedbean.ManagedBeanBndDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.webbnd.WebBndDDParser.createXMLRootParsable()
    // com.ibm.ws.javaee.ddmodel.webext.WebExtDDParser.createXMLRootParsable()

    protected String requireVersion() throws ParseException {
        String vers = getAttributeValue("", "version");
        if (vers == null) {
            throw new ParseException(errorMissingVersion());
        }
        return vers;
    }

    protected String errorMissingVersion() {
        // missing.deployment.descriptor.namespace=
        // CWWKC2264E: An error occurred while trying to determine the namespace
        // of the {0} deployment descriptor on line {1}.

        // TODO: 
        // No version is present in the {0} deployment descriptor.

        return Tr.formatMessage(tc, "missing.deployment.descriptor.version",
            ddEntryPath, getLineNumber());
    }

    // com.ibm.ws.javaee.ddmodel.app.ApplicationDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.bval.ValidationConfigDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.client.ApplicationClientDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.ejb.EJBJarDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.jsf.FacesConfigDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.permissions.PermissionsConfigDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.web.WebAppDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.web.WebFragmentDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.appbnd.ApplicationBndDDParser.createXMLRootParsable()
    // com.ibm.ws.javaee.ddmodel.appext.ApplicationExtDDParser.createXMLRootParsable()
    // com.ibm.ws.javaee.ddmodel.clientbnd.ApplicationClientBndDDParser.createXMLRootParsable()
    // com.ibm.ws.javaee.ddmodel.ejbbnd.EJBJarBndDDParser.createXMLRootParsable()
    // com.ibm.ws.javaee.ddmodel.ejbext.EJBJarExtDDParser.createXMLRootParsable()
    // com.ibm.ws.javaee.ddmodel.managedbean.ManagedBeanBndDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.webbnd.WebBndDDParser.createXMLRootParsable()
    // com.ibm.ws.javaee.ddmodel.webext.WebExtDDParser.createXMLRootParsable()
    // com.ibm.ws.javaee.ddmodel.ws.WebServicesDDParser.createRootParsable()

    protected String errorInvalidVersion(String vers) {
        // invalid.deployment.descriptor.version=
        // CWWKC2263E: The version {2} attribute
        // specified in the {0} deployment descriptor on line {1} is not valid.

        // TODO:
        // A non-valid {2} version is present
        // in the {0} deployment descriptor on line {1}.

        return Tr.formatMessage(tc, "invalid.deployment.descriptor.version",
            ddEntryPath, getLineNumber(),
            vers);
    }

    // TODO: No longer used
    protected String unknownDeploymentDescriptorVersion(String vers) {
        // unknown.deployment.descriptor.version=
        // CWWKC2261E: An error occurred while trying to determine
        // the version of the {0} deployment descriptor.

        return Tr.formatMessage(tc, "unknown.deployment.descriptor.version",
            ddEntryPath, getLineNumber(),
            vers);
    }

    // com.ibm.ws.javaee.ddmodel.bval.ValidationConfigDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.appbnd.ApplicationBndDDParser.createXMIRootParsable()
    // com.ibm.ws.javaee.ddmodel.appbnd.ApplicationBndDDParser.createXMLRootParsable()
    // com.ibm.ws.javaee.ddmodel.appext.ApplicationExtDDParser.createXMIRootParsable()
    // com.ibm.ws.javaee.ddmodel.appext.ApplicationExtDDParser.createXMLRootParsable()
    // com.ibm.ws.javaee.ddmodel.clientbnd.ApplicationClientBndDDParser.createXMIRootParsable()
    // com.ibm.ws.javaee.ddmodel.clientbnd.ApplicationClientBndDDParser.createXMLRootParsable()
    // com.ibm.ws.javaee.ddmodel.ejbbnd.EJBJarBndDDParser.createXMIRootParsable()
    // com.ibm.ws.javaee.ddmodel.ejbbnd.EJBJarBndDDParser.createXMLRootParsable()
    // com.ibm.ws.javaee.ddmodel.ejbext.EJBJarExtDDParser.createXMIRootParsable()
    // com.ibm.ws.javaee.ddmodel.ejbext.EJBJarExtDDParser.createXMLRootParsable()
    // com.ibm.ws.javaee.ddmodel.managedbean.ManagedBeanBndDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.webbnd.WebBndDDParser.createXMIRootParsable()
    // com.ibm.ws.javaee.ddmodel.webbnd.WebBndDDParser.createXMLRootParsable()
    // com.ibm.ws.javaee.ddmodel.webext.WebExtDDParser.createXMIRootParsable()
    // com.ibm.ws.javaee.ddmodel.webext.WebExtDDParser.createXMLRootParsable()
    
    protected void requireNamespace(String requiredNamespace) throws ParseException {
        if (namespace == null) {
            throw new ParseException(errorMissingNamespace());
        }
        if (!requiredNamespace.equals(namespace)) {
            throw new ParseException(errorRequireNamespace(requiredNamespace));
        }
    }

    // com.ibm.ws.javaee.ddmodel.app.ApplicationDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.bval.ValidationConfigDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.client.ApplicationClientDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.ejb.EJBJarDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.jsf.FacesConfigDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.permissions.PermissionsConfigDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.web.WebAppDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.web.WebFragmentDDParser.createRootParsable()

    protected void requireNamespace(String vers, String requiredNamespace) throws ParseException {
        if (namespace == null) {
            throw new ParseException(errorMissingNamespace());
        }
        if (!requiredNamespace.equals(namespace)) {
            throw new ParseException(errorRequireNamespace(vers, requiredNamespace));
        }
    }
    
    protected String errorMissingNamespace() {
        // missing.deployment.descriptor.namespace=
        // CWWKC2264E: An error occurred while trying to determine the namespace
        // of the {0} deployment descriptor on line {1}.

        // TODO:
        // No namespace is present in the {0} deployment descriptor.

        return Tr.formatMessage(tc, "missing.deployment.descriptor.namespace",
            ddEntryPath, getLineNumber());
    }

    // TODO:
    protected String errorRequireNamespace(String requiredNamespace) {
        // invalid.deployment.descriptor.require.namespace=
        // CWWKCXXXXE: The {2} namespace does not match the required {3} namespace,
        // in the {0} deployment descriptor on line {1}.

        return Tr.formatMessage(tc, "invalid.deployment.descriptor.require.namespace",
            ddEntryPath, getLineNumber(),
            namespace, requiredNamespace);
    }

    // TODO:
    protected String errorRequireNamespace(String vers, String requiredNamespace) {
        // invalid.deployment.descriptor.require.namespace.for-version=
        // CWWKCXXXXE: The {2} namespace does not match the required {3} namespace
        // for the {4} version,
        // in the {0} deployment descriptor on line {1}.

        return Tr.formatMessage(tc, "invalid.deployment.descriptor.require.namespace.for.version",
            ddEntryPath, getLineNumber(),
            namespace, requiredNamespace, vers);
    }

    // TODO: No longer used.
    protected String errorInvalidNamespace(String vers) {
        // invalid.deployment.descriptor.namespace=
        // CWWKC2262E: The server is unable to process
        // the {3} version and the {2} namespace
        // in the {0} deployment descriptor on line {1}.

        return Tr.formatMessage(tc, "invalid.deployment.descriptor.namespace",
            ddEntryPath, getLineNumber(),
            namespace, vers);
    }

    // com.ibm.ws.javaee.ddmodel.app.ApplicationDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.client.ApplicationClientDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.ejb.EJBJarDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.jsf.FacesConfigDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.web.WebAppDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.web.WebFragmentDDParser.createRootParsable()
    
    protected void requireProvisioning(int requireVersion, String actualVersion) throws ParseException {
        if (maxVersion < requireVersion) { 
            throw new ParseException(errorUnprovisionedVersion(requireVersion, actualVersion));
        }
    }

    // TODO
    protected String errorUnprovisionedVersion(int requireVersion, String actualVersion) {
        // invalid.deployment.descriptor.unprovisioned.version
        // CWWKCXXXXE: Unprovisioned {2} version;
        // at least the {3} version is required to be provisioned,
        // but the {4} version is provisioned;
        // of the {0} deployment descriptor on line {1}.

        return Tr.formatMessage(tc, "invalid.deployment.descriptor.unprovisioned.version",
            ddEntryPath, getLineNumber(),
            actualVersion, Integer.toString(requireVersion), Integer.toString(maxVersion));
    }

    // Root element errors ...

    // com.ibm.ws.javaee.ddmodel.DDParser.parseToRootElement()

    private String errorMissingRootElement() {
        // root.element.not.found=
        // CWWKC2253E: Unable to locate the root element
        // of the {0} deployment descriptor on line {1}.
        
        // TODO:
        // CWWKC2253E: Unable to locate the root element
        // of the {0} deployment descriptor. 

        return Tr.formatMessage(tc, "root.element.not.found",
            ddEntryPath, getLineNumber());
    }

    // com.ibm.ws.javaee.ddmodel.app.ApplicationDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.client.ApplicationClientDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.ejb.EJBJarDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.jsf.FacesConfigDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.permissions.PermissionsConfigDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.web.WebAppDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.web.WebFragmentDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.appbnd.ApplicationBndDDParser.createXMIRootParsable()
    // com.ibm.ws.javaee.ddmodel.appbnd.ApplicationBndDDParser.createXMLRootParsable()
    // com.ibm.ws.javaee.ddmodel.appext.ApplicationExtDDParser.createXMIRootParsable()
    // com.ibm.ws.javaee.ddmodel.appext.ApplicationExtDDParser.createXMLRootParsable()
    // com.ibm.ws.javaee.ddmodel.clientbnd.ApplicationClientBndDDParser.createXMIRootParsable()
    // com.ibm.ws.javaee.ddmodel.clientbnd.ApplicationClientBndDDParser.createXMLRootParsable()
    // com.ibm.ws.javaee.ddmodel.ejbbnd.EJBJarBndDDParser.createXMIRootParsable()
    // com.ibm.ws.javaee.ddmodel.ejbbnd.EJBJarBndDDParser.createXMLRootParsable()
    // com.ibm.ws.javaee.ddmodel.ejbext.EJBJarExtDDParser.createXMIRootParsable()
    // com.ibm.ws.javaee.ddmodel.ejbext.EJBJarExtDDParser.createXMLRootParsable()
    // com.ibm.ws.javaee.ddmodel.managedbean.ManagedBeanBndDDParser.createRootParsable()
    // com.ibm.ws.javaee.ddmodel.webbnd.WebBndDDParser.createXMIRootParsable()
    // com.ibm.ws.javaee.ddmodel.webbnd.WebBndDDParser.createXMLRootParsable()
    // com.ibm.ws.javaee.ddmodel.webext.WebExtDDParser.createXMIRootParsable()
    // com.ibm.ws.javaee.ddmodel.webext.WebExtDDParser.createXMLRootParsable()
    
    protected void requireRootElement(String requiredName) throws ParseException {
        if (!requiredName.equals(rootElementLocalName)) {
            throw new ParseException(errorInvalidRootElement(requiredName));
        }
    }

    protected String errorInvalidRootElement(String expectedElement) {
        // invalid.root.element=
        // CWWKC2252E: Invalid root local name {2}
        // in the {0} deployment descriptor on line {1}.

        // TODO:
        // CWWKC2252E: Non-valid root local name {2}, expected {3},
        // in the {0} deployment descriptor on line {1}.

        return Tr.formatMessage(tc, "invalid.root.element",
            ddEntryPath, getLineNumber(),
            rootElementLocalName);
    }

    // Parse errors ...
    
    // Reference errors ...

    // com.ibm.ws.javaee.ddmodel.DDParser.adaptRootContainer(Class<T>)
    //     UnableToAdaptException: Container -> T

    // TODO:
    private String referenceError(Class<?> targetClass) {
        // xml.reference.error=
        // CWWKCXXXXE: An error occurred
        // while attempting to obtain reference object of type {2}
        // while parsing the {0} deployment descriptor on line {1}.

        return Tr.formatMessage(tc, "xml.reference.error",
            ddEntryPath, getLineNumber(),
            targetClass.getName());
    }

    // com.ibm.ws.javaee.ddmodel.appbnd.AuthorizationTableXMIType.AuthorizationXMIType.handleChild(DDParser, String)
    // com.ibm.ws.javaee.ddmodel.appbnd.RunAsMapXMIType.RunAsBindingXMIType.handleChild(DDParser, String)
    // com.ibm.ws.javaee.ddmodel.appext.ModuleExtensionType.handleChild(DDParser, String)
    // com.ibm.ws.javaee.ddmodel.commonbnd.EJBRefType.handleChild(DDParser, String)
    // com.ibm.ws.javaee.ddmodel.commonbnd.MessageDestinationRefType.handleChild(DDParser, String)
    // com.ibm.ws.javaee.ddmodel.commonbnd.ResourceEnvRefType.handleChild(DDParser, String)
    // com.ibm.ws.javaee.ddmodel.commonbnd.ResourceRefType.handleChild(DDParser, String)
    // com.ibm.ws.javaee.ddmodel.commonext.ResourceRefType.handleChild(DDParser, String)
    // com.ibm.ws.javaee.ddmodel.ejbbnd.EnterpriseBeanType.handleChild(DDParser, String)
    // com.ibm.ws.javaee.ddmodel.ejbext.EnterpriseBeanType.handleChild(DDParser, String)
    // com.ibm.ws.javaee.ddmodel.ejbext.ExtendedMethodType.handleChild(DDParser, String)
    // com.ibm.ws.javaee.ddmodel.webext.ServletExtensionType.handleChild(DDParser, String)

    public static void unresolvedReference(String elementName, String href) {
        // TODO: Make this a warning.
        if (tc.isDebugEnabled()) {
            Tr.debug(tc, "Unable to resolve reference from element {0} to {1}.",
                elementName, href);
        }
    }

    // com.ibm.ws.javaee.ddmodel.CrossComponentReferenceType.resolveReferent(DDParser, Class<R>)

    public String invalidHRefPrefix(String hrefElementName, String hrefPrefix) {
        // invalid.href.prefix=
        // CWWKC2260E: The href attribute value of the {2} element
        // does not start with {3}
        // in the {0} deployment descriptor on line {1}.

        return Tr.formatMessage(tc, "invalid.href.prefix",
            ddEntryPath, getLineNumber(),
            hrefElementName, hrefPrefix);
    }

    // Attribute errors ...

    // com.ibm.ws.javaee.ddmodel.DDParser.parse(ParsableElement)

    private String incorrectIDAttrNamespace(String attrNS) {
        // incorrect.id.attr.namespace=
        // CWWKC2255E: The namespace of the id attribute of element {2}
        // was {3} when it should have been {4}
        // in the {0} deployment descriptor on line {1}.

        return Tr.formatMessage(tc, "incorrect.id.attr.namespace",
            ddEntryPath, getLineNumber(),
            currentElementLocalName, attrNS, XMI_ID_NAMESPACE);
    }

    // com.ibm.ws.javaee.ddmodel.app.ApplicationType.finish(DDParser)
    // com.ibm.ws.javaee.ddmodel.bval.ValidationConfigType.PropertyType.finish(DDParser)
    // com.ibm.ws.javaee.ddmodel.client.ApplicationClientType.finish(DDParser)
    // com.ibm.ws.javaee.ddmodel.ejb.EJBJarType.finish(DDParser)
    // com.ibm.ws.javaee.ddmodel.jsf.FacesConfigType.finish(DDParser)
    // com.ibm.ws.javaee.ddmodel.web.common.WebAppType.finish(DDParser)
    // com.ibm.ws.javaee.ddmodel.ejbbnd.EnterpriseBeanType.finish(DDParser)
    // com.ibm.ws.javaee.ddmodel.ejbbnd.InterfaceType.finish(DDParser)
    // com.ibm.ws.javaee.ddmodel.ejbbnd.JCAAdapterType.finish(DDParser)
    // com.ibm.ws.javaee.ddmodel.ejbbnd.ListenerPortType.finish(DDParser)
    // com.ibm.ws.javaee.ddmodel.ejbext.EJBJarExtType.handleChild(DDParser, String)
    // com.ibm.ws.javaee.ddmodel.ejbext.EnterpriseBeanType.finish(DDParser)
    // com.ibm.ws.javaee.ddmodel.ejbext.RunAsModeBaseType.finish(DDParser)
    // com.ibm.ws.javaee.ddmodel.ejbext.SpecifiedIdentityType.finish(DDParser)
    // com.ibm.ws.javaee.ddmodel.ejbext.StartAtAppStartType.finish(DDParser)
    // com.ibm.ws.javaee.ddmodel.ejbext.TimeOutType.finish(DDParser)
    // com.ibm.ws.javaee.ddmodel.managedbean.ManagedBeanType.finish(DDParser)
    
    public String requiredAttributeMissing(String attrLocal) {
        // required.attribute.missing=
        // CWWKC2251E: The {2} element is missing the required {3} attribute
        // in the {0} deployment descriptor on line {1}.        
        
        return Tr.formatMessage(tc, "required.attribute.missing",
            ddEntryPath, getLineNumber(),
            currentElementLocalName, attrLocal);
    }

    // com.ibm.ws.javaee.ddmodel.DDParser.parse(ParsableElement)

    private String unexpectedAttribute(String attrLocal) {
        // unexpected.attribute=
        // CWWKC2256E: Unexpected attribute {3}
        // encountered parsing element {2}
        // in the {0} deployment descriptor on line {1}.
        
        return Tr.formatMessage(tc, "unexpected.attribute", ddEntryPath, getLineNumber(), currentElementLocalName, attrLocal);
    }

    // Content errors ...

    // com.ibm.ws.javaee.ddmodel.DDParser.parse(ParsableElement)
    // com.ibm.ws.javaee.ddmodel.web.common.WebCommonType.handleChild(DDParser, String)
    
    public String unexpectedContent() {
        // unexpected.content=
        // CWWKC2257E: Unexpected content encountered in element {2}
        // in the {0} deployment descriptor on line {1}.

        return Tr.formatMessage(tc, "unexpected.content",
            ddEntryPath, getLineNumber(),
            currentElementLocalName);
    }

    // Element structure ...

    // com.ibm.ws.javaee.ddmodel.DDParser.parse(ParsableElement)

    private String unexpectedChildElement(String elementLocal) {
        // unexpected.child.element=
        // CWWKC2259E: Unexpected child element {3}
        // of parent element {2}
        // encountered in the {0} deployment descriptor on line {1}.

        return Tr.formatMessage(tc, "unexpected.child.element",
            ddEntryPath, getLineNumber(),
            currentElementLocalName, elementLocal);
    }

    // com.ibm.ws.javaee.ddmodel.DDParser.parse(ParsableElement)

    private String incorrectChildElementNamespace(String elementNS, String elementLocal) {
        // incorrect.child.element.namespace=
        // CWWKC2258E: The namespace of child element {3} of parent element {2}
        // was {4} not {5}
        // in the {0} deployment descriptor on line {1}.

        return Tr.formatMessage(tc, "incorrect.child.element.namespace",
            ddEntryPath, getLineNumber(),
            currentElementLocalName, elementLocal,
            elementNS, namespace);
    }

    // com.ibm.ws.javaee.ddmodel.web.common.WebAppType.handleChild(DDParser, String)
    // com.ibm.ws.javaee.ddmodel.web.common.WebCommonType.handleChild(DDParser, String)
    // com.ibm.ws.javaee.ddmodel.web.common.WebFragmentType.handleChild(DDParser, String)

    public String tooManyElements(String element) {
        // at.most.one.occurrence=
        // CWWKC2266E: There should be at most one {3} child element
        // of the {2} parent element
        // in the {0} deployment descriptor on line {1}.

        return Tr.formatMessage(tc, "at.most.one.occurrence",
            ddEntryPath, getLineNumber(),
            currentElementLocalName, element);
    }

    // com.ibm.ws.javaee.ddmodel.ejbext.RunAsModeType.finish(DDParser)

    public String missingElement(String element) {
        // required.method.element.missing=
        // CWWKC2267E: The {2} element
        // must have a least one {3} child element defined
        // in the {0} deployment descriptor on line {1}.

        return Tr.formatMessage(tc, "required.method.element.missing",
            ddEntryPath, getLineNumber(),
            currentElementLocalName, element);
    }

    // com.ibm.ws.javaee.ddmodel.DDParser.parse(ParsableElement)
    // com.ibm.ws.javaee.ddmodel.DDParser.skipSubtree()
    
    private String endElementNotFound() {
        // end.element.not.found=
        // CWWKC2254E: The end element tag of the {2} element was not found
        // in the {0} deployment descriptor on line {1}.

        return Tr.formatMessage(tc, "end.element.not.found",
            ddEntryPath, getLineNumber(),
            currentElementLocalName);
    }

    // Non-valid values ...

    // com.ibm.ws.javaee.ddmodel.BooleanType.setValueFromLexical(DDParser, String)
    // com.ibm.ws.javaee.ddmodel.DDParser.parseEnum(String, Class<T>)
    // com.ibm.ws.javaee.ddmodel.app.ApplicationType.GenericBooleanType.setValueFromLexical(DDParser, String)
    // com.ibm.ws.javaee.ddmodel.ejb.EJBJarType.MessageDrivenBeanType.handleChild(DDParser, String)
    // com.ibm.ws.javaee.ddmodel.ejb.EJBJarType.MessageDrivenDestinationType.handleChild(DDParser, String)
    // com.ibm.ws.javaee.ddmodel.commonext.LocalTransactionType.parseXMIBoundaryEnumAttributeValue(DDParser, int)
    // com.ibm.ws.javaee.ddmodel.commonext.LocalTransactionType.parseXMIResolverEnumAttributeValue(DDParser, int)
    // com.ibm.ws.javaee.ddmodel.commonext.LocalTransactionType.parseXMIUnresolvedActionEnumAttributeValue(DDParser, int)
    // com.ibm.ws.javaee.ddmodel.commonext.MethodType.parseXMIMethodTypeEnumAttributeValue(DDParser, int)
    // com.ibm.ws.javaee.ddmodel.commonext.ResourceRefType.parseXMIBranchCouplingEnumAttributeValue(DDParser, int)
    // com.ibm.ws.javaee.ddmodel.commonext.ResourceRefType.parseXMIConnectionManagementPolicyEnumAttributeValue(DDParser, int)
    // com.ibm.ws.javaee.ddmodel.ejbext.EnterpriseBeanType.InternationalizationXMIIgnoredType.parseXMIStringAttributeValue(DDParser, int)
    
    public String invalidEnumValue(String value, Object... values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            builder.append(values[i]);
        }
        
        // invalid.enum.value=
        // CWWKC2273E: The {2} value
        // in the {0} deployment descriptor on line {1} is not valid.
        // The valid values are: {3}

        return Tr.formatMessage(tc, "invalid.enum.value",
            ddEntryPath, getLineNumber(),
            value, builder);
    }

    // com.ibm.ws.javaee.ddmodel.IntegerType.setValueFromLexical(DDParser, String)

    public String invalidIntValue(String value) {
        // invalid.int.value=
        // CWWKC2274E: The {2} value
        // in the {0} deployment descriptor on line {1}
        // is not a valid integer number.

        return Tr.formatMessage(tc, "invalid.int.value",
            ddEntryPath, getLineNumber(),
            value);
    }

    // com.ibm.ws.javaee.ddmodel.LongType.setValueFromLexical(DDParser, String)

    public String invalidLongValue(String value) {
        // invalid.long.value=
        // CWWKC2275E: The {2} value
        // in the {0} deployment descriptor on line {1}
        // is not a valid long number.

        return Tr.formatMessage(tc, "invalid.long.value",
            ddEntryPath, getLineNumber(),
            value);
    }
    
    // Parsing helper types and classes ...

    public static class ParseException extends Exception {
        private static final long serialVersionUID = -2437543890927284677L;

        public ParseException(String translatedMessage) {
            super(translatedMessage);
        }

        public ParseException(String translatedMessage, Throwable t) {
            super(translatedMessage, t);
        }
    }

    /**
     * Type for the root elements of descriptors.
     * 
     * Used to isolate the root element from using {@link Diagnostics}.
     * A print string is obtained through a string builder.  The root
     * element {@link RootParsable#describe(StringBuilder)} implementation
     * creates a a diagnostics object using the string builder and
     * provides that to child elements.
     */
    public interface RootParsable {
        void describe(StringBuilder sb);
    }

    /**
     * Type for non-root elements of descriptors.
     * 
     * The name is misleading: The type is for non-root elements,
     * which include nullable elements ({@link ParsableElement},
     * and includes non-nullable list elements (of which there are
     * many annonymous sub-types).
     */
    public interface Parsable {
        void describe(Diagnostics diag);
    }

    /**
     * Type of nullable non-root descriptor elements.
     */
    public interface ParsableElement extends Parsable {
        void setNil(boolean nilled);
        boolean isNil();

        boolean isIdAllowed();

        /**
         * Handle an attribute.  Answer true or false telling if
         * the attribute was handled.  Answering false usually
         * causes parsing to fail with an exception.
         * 
         * @param parser The parser which is presenting the attribute
         *     which is to be handled.
         * @param nsURI The namespace from the attribute.
         * @param localNmae The local name of the attribute.
         * @param index The index of the attribute.
         * 
         * @return True or false telling if the attribute was handled.
         *     
         * @throws ParseException Thrown if there was an error handling the
         *     attribute.
         */        
        boolean handleAttribute(DDParser parser, String nsURI, String localName, int index) throws ParseException;
        
        /**
         * Handle a child element.  Answer true or false telling
         * if the child element was handled.  Answer false usually
         * causes aprsing to fail with an exception.
         * 
         * @param parser The parser which is presenting the child
         *     element which is to be handled.
         * @param localNmae The local name of the child element.
         * 
         * @return True or false telling if the child element was handled.
         *     
         * @throws ParseException Thrown if there was an error handling the
         *     child element.
         */
        boolean handleChild(DDParser parser, String localName) throws ParseException;
        
        /**
         * Handle element content. 
         * 
         * Answer true or false telling if the content was handled.
         * Answering false with will usually cause parsing to fail
         * with an exception.
         * 
         * @param parser The parser for which to handle content.
         * 
         * @return True or false, telling if the content was handled.
         *     
         * @throws ParseException Thrown in case of an error handling
         *     the content.
         */        
        boolean handleContent(DDParser parser) throws ParseException;

        void finish(DDParser parser) throws ParseException;
    }

    /**
     * Common implementation of {@link ParsableELement}.
     */
    public static abstract class ElementContentParsable implements ParsableElement {
        private boolean nilled;

        @Trivial
        @Override
        public final void setNil(boolean nilled) {
            this.nilled = nilled;
        }

        @Trivial
        @Override
        public final boolean isNil() {
            return nilled;
        }

        @Trivial
        @Override
        public boolean isIdAllowed() {
            return false;
        }

        /**
         * Default attribute handler.
         * 
         * This implementation always answers false.  By default,
         * no attributes are handled.
         * 
         * Elements which support attributes must override this
         * default implementation.
         * 
         * @param parser The parser which is presenting the attribute
         *     which is to be handlded.
         * @param nsURI The namespace from the attribute.
         * @param localNmae The local name of the attribute.
         * @param index The index of the attribute.
         * 
         * @return True or false telling if the attribute was handled.
         *     This implementation always answers false.
         *     
         * @throws ParseException Thrown if there was an error handling the
         *     attribute.  This implementation never throws an exception.
         */
        @Trivial
        @Override
        public boolean handleAttribute(DDParser parser, String nsURI, String localName, int index) throws ParseException {
            return false;
        }

        /**
         * Default content handler:
         * 
         * Answer true if the content is all whitespace, in which case
         * the content is ignored.  Answer false if the content is not
         * whitespace.  This will usually cause parsing to fail.  Elements
         * which support non-whitespace content must override this
         * default implementation.
         * 
         * @param parser The parser for which to handle content.
         * 
         * @return True or false, telling if the content was handled.
         *     This implementation answers true if and only if the
         *     content is all whitespace.
         *     
         * @throws ParseException Thrown in case of an error handling
         *     the content.  This implementation never throws an
         *     exception.
         */
        @Trivial
        @Override
        public boolean handleContent(DDParser parser) throws ParseException {
            return parser.isWhiteSpace();
        }

        @Trivial
        @Override
        public void finish(DDParser parser) throws ParseException {
            // Empty
        }

        @Trivial
        protected String toTracingSafeString() {
            return super.toString();
        }

        @Trivial
        @Override
        public final String toString() {
            return toTracingSafeString();
        }
    }

    /**
     * Parsable list type of parsable elements.
     */
    public static class ParsableList<T extends ParsableElement>
        implements Parsable, Iterable<T> {

        protected List<T> list = new ArrayList<T>();

        /**
         * Required subclass method.  Strangely, this is not abstract.
         *
         * @param parser The parser which is creating this list instance.
         *
         * @return A new parsable list.  This implementation always throws
         *     an {@link UnsupportedOperationException}.
         */
        public T newInstance(DDParser parser) {
        	throw new UnsupportedOperationException();
        }

        /**
         * Strange unchecked method.  Cast a parsable element to
         * the element type of this list, then proceed with a usual
         * call to {@link #add}.
         *
         * @param element The parsable element which is to be added.
         *     Must be of the element type of this list, or a runtime
         *     exception will occur.
         */
        @Trivial
        @SuppressWarnings("unchecked")
        public void addElement(ParsableElement element) {
            add((T) element);
        }

        @Trivial
        public void add(T t) {
            list.add(t);
        }

        @Trivial
        @Override
        public Iterator<T> iterator() {
            return list.iterator();
        }

        @Trivial
        @Override
        public void describe(Diagnostics diag) {
            String prefix = "";
            for (T t : list) {
                diag.sb.append(prefix);
                diag.describeWithIdIfSet(t);
                prefix = ",";
            }
        }
    }

    public static class ParsableListImplements<T extends ParsableElement, I>
        extends ParsableList<T> {

        @Trivial
        @SuppressWarnings("unchecked")
        public List<I> getList() {
            return (List<I>) list;
        }
    }

    public static class ComponentIDMap {
        public static final Object DUPLICATE = new Object();

        private final Map<String, Object> idToComponentMap =
            new HashMap<String, Object>();

        @Trivial
        private void put(String key, Object parsable, String elementName) {
            Object oldValue = get(key);
            if (oldValue == parsable) {
                return; // Strange and unexpected; ignore.
            }

            if (oldValue != null) {
                parsable = ComponentIDMap.DUPLICATE;
            }
            put(key, parsable);

            if (tc.isDebugEnabled()) {
                Tr.warning(tc, "The element {0} duplicates id {1}", elementName, key);
            }
        }        
        
        @Trivial
        Object get(String id) {
            return idToComponentMap.get(id);
        }

        @Trivial
        Object put(String id, Object ddComponent) {
            return idToComponentMap.put(id, ddComponent);
        }

        @Trivial
        public Object getComponentForId(String id) {
            Object comp = idToComponentMap.get(id);
            if (comp == DUPLICATE) {
                if (tc.isDebugEnabled()) {
                    Tr.warning(tc, "Null component lookup for id {0}: Duplicate referents were found.");
                }
                return null;
            } else if (comp == null) {
                if (tc.isDebugEnabled()) {
                    Tr.warning(tc, "Null component lookup for id {0}", id);
                }           
                return comp;
            } else {
                return comp;
            }
        }

        // Mark as trivial to turn off logging as a fix for defect 53155
        @Trivial
        public String getIdForComponent(Object ddComponent) {
            for (Map.Entry<String, Object> entry : idToComponentMap.entrySet()) {
                if (entry.getValue() == ddComponent) {
                    return entry.getKey();
                }
            }
            return null;
        }
    }

    public static class Diagnostics {
        private final ComponentIDMap idMap;
        private final StringBuilder sb;

        @Trivial
        public Diagnostics(ComponentIDMap idMap) {
            this(idMap, new StringBuilder());
        }

        @Trivial
        public Diagnostics(ComponentIDMap idMap, StringBuilder sb) {
            this.idMap = idMap;
            this.sb = sb;
        }

        @Trivial
        void describeWithIdIfSet(Parsable parsable) {
            String id = idMap != null ? idMap.getIdForComponent(parsable) : null;
            if (id != null) {
                sb.append("[id<\"" + id + "\">]");
            }
            parsable.describe(this);
        }

        @Trivial
        public <T> void describeEnum(T enumValue) {
            if (enumValue != null) {
                sb.append(enumValue);
            }
        }

        @Trivial
        public <T> void describeEnum(String name, T enumValue) {
            sb.append(name + "<");
            if (enumValue != null) {
                sb.append(enumValue);
            } else {
                sb.append("null");
            }
            sb.append(">");
        }

        @Trivial
        public <T> void describeEnumIfSet(String name, T enumValue) {
            if (enumValue != null) {
                sb.append("[" + name + "<");
                sb.append(enumValue);
                sb.append(">]");
            }
        }

        @Trivial
        public void describe(String name, ParsableElement parsable) {
            sb.append(name + "<");
            if (parsable != null) {
                describeWithIdIfSet(parsable);
            } else {
                sb.append("null");
            }
            sb.append(">");
        }

        @Trivial
        public void describe(String name, ParsableList<? extends ParsableElement> parsableList) {
            sb.append(name + "(");
            if (parsableList != null) {
                parsableList.describe(this);
            } else {
                sb.append("null");
            }
            sb.append(")");
        }

        @Trivial
        public void describeIfSet(String name, ParsableElement parsable) {
            if (parsable != null) {
                sb.append("[" + name + "<");
                describeWithIdIfSet(parsable);
                sb.append(">]");
            }
        }

        @Trivial
        public void describeIfSet(String name, ParsableList<? extends ParsableElement> parsableList) {
            if (parsableList != null) {
                sb.append("[" + name + "(");
                parsableList.describe(this);
                sb.append(")]");
            }
        }

        @Trivial
        public void append(String string) {
            sb.append(string);
        }

        @Trivial
        public String getDescription() {
            return sb.toString();
        }
    }

    // Top: Constructors in several forms ...

    // ValidationsConfig, PermissionsConfig,
    // ManagedBeanBnd,
    // WebServices, WebServicesBnd
    public DDParser(Container rootContainer, Entry ddEntry) throws ParseException {
        this(rootContainer, ddEntry,
             UNUSED_PRIMARY_TYPE, !IS_XMI,
             !TRIM_SIMPLE_CONTENT,
             UNUSED_VERSION, UNUSED_VERSION);
    }

    // ApplicationBnd, ApplicationExt,
    // ClientBnd,
    // EJBJarBnd, EJBJarExt,
    // WebBnd, WebExt
    public DDParser(Container rootContainer, Entry ddEntry,
                    Class<?> primaryDescriptorType,
                    boolean xmi)
        throws ParseException {

        this(rootContainer, ddEntry,
             primaryDescriptorType, xmi,
             !TRIM_SIMPLE_CONTENT,
             UNUSED_VERSION, UNUSED_VERSION);             
    }
    
    // Application, ApplicationClient, EJBJar, FacesConfig
    public DDParser(Container rootContainer, Entry ddEntry,
                    int provisionedDescriptorVersion)
        throws ParseException {

        this(rootContainer, ddEntry,
             UNUSED_PRIMARY_TYPE, !IS_XMI,
             !TRIM_SIMPLE_CONTENT,
             provisionedDescriptorVersion, UNUSED_VERSION);
    }

    // WebApp, WebFragment
    public DDParser(Container rootContainer, Entry ddEntry,
                    boolean trimSimpleContent,
                    int provisionedDescriptorVersion, int provisionedEEVersion)
        throws ParseException {

        this(rootContainer, ddEntry,
             UNUSED_PRIMARY_TYPE, !IS_XMI,
             trimSimpleContent,
             provisionedDescriptorVersion, provisionedEEVersion);
    }
    
    /**
     * Construct a parser for a specified container and container entry.  The
     * container entry contains descriptor text which is to be parsed.
     * 
     * Optionally, a primary descriptor type be supplied.  This is used
     * to resolve descriptor-to-descriptor references, in particular, the
     * reference from a bindings or extensions descriptor to a primary
     * descriptor.
     * 
     * @param ddRootContainer The root container of the descriptor entry. 
     * @param ddEntry The entry which contains the serialized descriptor.
     * @param primaryDescriptorType Optional.  The type of the associated
     *     primary descriptor.  Used to resolve references between descriptors.
     *     In particular, used to resolve the reference from bindings and
     *     extensions descriptors to the primary descriptor.
     * @param xmi Control parameter for bindings and extensions.  Use XMI
     *     or XML parsing rules.
     * @param trimSimpleContent Control parameter for web-application and
     *     web-fragment descriptors.  Tells if simple content is to be
     *     trimmed.
     * @param provisionedDescriptorVersion The maximum specification version of the descriptor
     *     which will be parsed.
     * @param provisionedEEVersion The runtime version (a JavaEE version) controlling
     *     what parse rules are to be used.
     *
     * @throws ParseException Thrown if the parser cannot be created.
     *     Not thrown by this root implementation.
     */
    public DDParser(
        Container ddRootContainer, Entry ddEntry,
        Class<?> primaryDescriptorType, boolean xmi,
        boolean trimSimpleContent,
        int provisionedDescriptorVersion, int provisionedEEVersion) throws ParseException {

        // What is being parsed ...
        this.ddRootContainer = ddRootContainer;
        this.ddEntry = ddEntry;
        this.ddEntryPath = ddEntry.getPath(); // Starts with '/'.

        // Parse parameters ...
        this.primaryDescriptorType = primaryDescriptorType;
        this.xmi = xmi; // For parsing a bindings or extension.
        this.trimSimpleContent = trimSimpleContent; // For parsing web-app and web-fragment.

        // Provisioned versions ...
        this.maxVersion = provisionedDescriptorVersion;
        this.runtimeVersion = provisionedEEVersion;

        // Parse state ...
        this.adaptCache = new HashMap<Class<?>, Object>();
        this.idMap = new ComponentIDMap();
        this.contentBuilder = new StringBuilder();
    }

    // What is being parsed ...

    /**
     * The root container which contains the entry which is being
     * parsed.
     * 
     * This is usually the module or application root container,
     * which contains a descriptor entry at a standard location.
     * 
     * However, this may also be an alternate container.  Primary
     * descriptors support alternate descriptors.  See
     * {@link com.ibm.ws.container.service.app.deploy.extended.AltDDEntryGetter#getAltDDEntry(Type)},
     */
    protected final Container ddRootContainer;
    
    /**
     * The entry which contains the descriptor which is to be parsed.
     * 
     * This is usually an entry within a module or an application
     * container.  However, this is not always the case.  See
     * {@link com.ibm.ws.container.service.app.deploy.extended.AltDDEntryGetter#getAltDDEntry(Type)}.
     */
    protected final Entry ddEntry;
    protected final String ddEntryPath; // Starts with '/'.

    /**
     * Answer a module name which may be used for error and
     * warning messages.
     *
     * @return A module name which may be used for error and
     *     warning messages.
     */
    protected String getModuleName() {
        String moduleName = null;

        NonPersistentCache cache = null;
        try {
            cache = ddRootContainer.adapt(NonPersistentCache.class);
        } catch (UnableToAdaptException e) {
            // FFDC
        }

        if (cache != null) {
            ModuleInfo moduleInfo = (ModuleInfo)
                cache.getFromCache(ModuleInfo.class);
            if (moduleInfo != null) {
                moduleName = moduleInfo.getName();
            }
        }
    
        if ( moduleName == null ) {
            moduleName = ddRootContainer.getName();
        }
        
        return moduleName;
    }
    
    @Trivial
    public Entry getAdaptableEntry() {
        return ddEntry;
    }

    public String getPath() {
        return ddEntryPath;
    }

    public String getDeploymentDescriptorPath() {
        return ddEntryPath.substring(1);
    }

    //
    
    // Cross-component support ...

    /** Cache of adapted values obtained from the root container. */
    private final Map<Class<?>, Object> adaptCache;

    /**
     * Used by {@link com.ibm.ws.javaee.ddmodel.CrossComponentReferenceType.resolveReferent(DDParser, Class)}
     * to resolve references between bindings and extensions files and their associated
     * deployment descriptor.
     *
     * The result is cached in this parser instance.  Calls look first to the cache,
     * and store to the cache the first time a non-null result is obtained. 
     *
     * @param <T> The type of the associated deployment descriptor. 
     * @param adaptTarget The class of the type of the associated descriptor.
     *
     * @return This parser's root container adapted as the specified type.
     *
     * @throws ParseException Thrown if the parse of the associated descriptor
     *     failed.
     */
    @Trivial
    public <T> T adaptRootContainer(Class<T> adaptTarget) throws ParseException {
        @SuppressWarnings("unchecked")
        T result = (T) adaptCache.get(adaptTarget);
        if (result != null) {
            return result;
        }

        try {
            result = ddRootContainer.adapt(adaptTarget);
        } catch (UnableToAdaptException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ParseException) {
                throw (ParseException) cause;
            }
            throw new ParseException(referenceError(adaptTarget), e);
        }

        adaptCache.put(adaptTarget, result);

        return result;
    }
    
    // Parse control parameters:

    /**
     * Control parameter: Used to indicate that no primary
     * descriptor type is available.
     */
    public static final Class<?> UNUSED_PRIMARY_TYPE = null;

    /**
     * Type of the primary descriptor.  Used by bindings and extension
     * descriptors to link to their primary descriptor.
     */
    public final Class<?> primaryDescriptorType;
   
    /**
     * Control parameter: Used when constructing a DD parser
     * to indicate that XMI parsing is to be performed.  XMI
     * parsing is an older format for bindings and extensions
     * deployment descriptors.
     */
    protected static final boolean IS_XMI = true;
    
    /**
     * Control parameter used by most bindings and extensions descriptors.
     * Tells if XMI or XML parsing rules are to be used.
     */
    protected final boolean xmi;

    /**
     * Special namespace used when parsing XMI format bindings and
     * extensions descriptors.
     */
    public static final String XMI_ID_NAMESPACE = "http://www.omg.org/XMI";

    /**
     * Control value: Used when constructing a DD parser to
     * indicate that simple context is to be trimmed.
     */
    public static final boolean TRIM_SIMPLE_CONTENT = true;
    
    /**
     * Control parameter used by web-application and web-fragment descriptor
     * parsing.  Tells if simple content is to be trimmed.
     */
    protected final boolean trimSimpleContent;

    // Parse allowed version parameters ...

    /**
     * The maximum specification version which may be parsed.
     *
     * An attempt to parse a descriptor which has a higher specification
     * version results in a parse exception.
     */
    public final int maxVersion;
    
    /**
     * Control parameter.  Used when constructing descriptor
     * parsers.  Indicates for unused version values.
     */
    public static final int UNUSED_VERSION = -1;
    
    /**
     * The JavaEE version as determined by feature provisioning.
     * 
     * Used when parsing web-application and web-fragment descriptors:
     * The particular provisioned JavaEE version sets particular
     * rules for valid descriptors.
     * 
     * See {@link com.ibm.ws.javaee.ddmodel.web.common.WebAppType#handleChild(DDParser, String)},
     * {@link com.ibm.ws.javaee.ddmodel.web.WebAppDDParser#setRuntimeVersion()},
     * {@link com.ibm.ws.javaee.ddmodel.web.common.WebFragmentType.handleChild(DDParser, String)},
     * and {@link com.ibm.ws.javaee.ddmodel.web.WebFragmentDDParser#setRuntimeVersion()}.
     */
    public int runtimeVersion;
    
    //

    /**
     * Parse lifecycle and main API: Perform the full steps of parsing
     * a deployment descriptor from the bound entry.
     *
     * The return type is {@link Object}, not {@link DeploymentDescriptor}:
     * {@link com.ibm.ws.javaee.dd.ejb.EJBJar} is not a subtype of
     * deployment descriptor.
     *
     * @return The root object parsed as a deployment descriptor.
     *
     * @throws ParseException Thrown if parsing failed.
     */
    public abstract Object parse() throws ParseException;    

    /**
     * Core parse implementation: Parse data obtained from the
     * bound entry into a deployment descriptor.
     * 
     * Parsing follows a workflow: First create an XML reader 
     * on the stream obtained from the bound entity and parse
     * the starting element of the descriptor.  Scrape values
     * from the starting element and use them to create the
     * root element of the descriptor.  The values from the
     * starting element set the specification version which
     * is being used by the descriptor.  Finally, parse the
     * body of the descriptor into the root element.
     *  
     * @throws ParseException Thrown if parsing failed, either
     *     because of a read failure, or because of an XML
     *     formatting failure, or because the starting element
     *     of the descriptor is not valid, or because the
     *     body of the descriptor is not valid.
     */
    protected void parseRootElement() throws ParseException {
        try ( InputStream stream = ddEntry.adapt(InputStream.class) ) {
            try {
                // Main parse lifecycle ...
                //   Create the XML reader
                //   Parse the starting element of the descriptor
                //     which sets the version and root parse element
                //   (Usually) parse the body of the descriptor.

                // Alone among the several descriptor types, validation
                // configuration descriptors are allowed to be missing
                // their root element.
                //
                // See com.ibm.ws.javaee.ddmodel.bval.ValidationConfigDDParser.
                //     createRootParsable().

                xsr = createReader(stream);
                parseToRootElement();
                if (rootParsable != null) {
                    parse(rootParsable);
                }

            } finally {
                if (xsr != null) {
                    try {
                        xsr.close();
                    } catch (XMLStreamException e) {
                        // FFDC, but otherwise ignore
                    }
                }
            }

        } catch (UnableToAdaptException e) { // thrown by 'ddEntry.adapt'.
            throw new ParseException(openError(), e);
        } catch (IOException e) { // thrown by 'stream.close'.
            // FFDC, but otherwise ignore
        }
    }

    /**
     * Main parsing step: Parse an element of the descriptor.
     * 
     * Used to parse both the root parse element and nested parse
     * elements.  Entry for parsing the root element should only
     * occur from {@link #parseRootElement()}.
     * 
     * @param parsable An element of the descriptor.
     * 
     * @throws ParseException Thrown in case of a parse failure.
     */
    @FFDCIgnore(XMLStreamException.class)
    public void parse(ParsableElement parsable) throws ParseException {
        // Change the parse state to the next element of the
        // xml reader.

        QName elementName = xsr.getName();
        String elementLocalName = xsr.getLocalName();
        currentElementLocalName = elementLocalName;

        // Handle attributes ...
        //   ID attributes, schema attributes, and normal attributes

        int attrCount = xsr.getAttributeCount();
        for (int attrNo = 0; attrNo < attrCount; attrNo++) {
            String attrNS = xsr.getAttributeNamespace(attrNo);
            String attrLocal = xsr.getAttributeLocalName(attrNo);

            if (parsable.isIdAllowed() && "id".equals(attrLocal)) {
                if ( (xmi && !XMI_ID_NAMESPACE.equals(attrNS)) ||
                     (!xmi && (attrNS != null)) ) {
                    throw new ParseException(incorrectIDAttrNamespace(attrNS));
                }
                IDType idKey = parseIDAttributeValue(attrNo);
                idMap.put( idKey.getValue(), parsable, currentElementLocalName );
                continue;
            }

            if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI.equals(attrNS)) {
                if ("nil".equals(attrLocal)) {
                    parsable.setNil(parseBooleanAttributeValue(attrNo).getBooleanValue());
                    continue;
                }
                if ("schemaLocation".equals(attrLocal)) {
                    continue; // no action needed
                }
            }

            if (parsable.handleAttribute(this, attrNS, attrLocal, attrNo)) {
                continue;
            }

            throw new ParseException(unexpectedAttribute(attrLocal));
        }

        // Handle non-attribute cases:
        //   characters and character data;
        //   start and end elements;
        //   comments and processing instructions

        try {
            while (xsr.hasNext()) {
                switch (xsr.next()) {
                    case XMLStreamConstants.CHARACTERS:
                    case XMLStreamConstants.CDATA:
                        if (parsable.handleContent(this)) {
                            break;
                        }
                        throw new ParseException(unexpectedContent());

                    case XMLStreamConstants.END_ELEMENT:
                        parsable.finish(this);
                        if (parsable == rootParsable) {
                            // Check that the document is well-formed after the end
                            // of the root element.
                            // TODO: Do we do this check?
                            while (xsr.hasNext()) {
                                xsr.next();
                            }
                            xsr.close();
                        }
                        return;
                    case XMLStreamConstants.START_ELEMENT:
                        String localName = xsr.getLocalName();
                        String localNamespace = xsr.getNamespaceURI();
                        if ( ((namespace != null) && !namespace.equals(localNamespace)) ||
                             ((namespace == null) && (localNamespace != null)) ) {
                            throw new ParseException(incorrectChildElementNamespace(localNamespace, localName));
                        }
                        boolean handledChild = parsable.handleChild(this, localName); // Recurse
                        currentElementLocalName = elementLocalName; // Restore
                        if (!handledChild) {
                            throw new ParseException(unexpectedChildElement(localName));
                        }
                        break;

                    case XMLStreamConstants.COMMENT:
                    case XMLStreamConstants.PROCESSING_INSTRUCTION:
                        break; // ignored

                    default:
                        int eventType = xsr.getEventType();
                        RuntimeException re = new RuntimeException(
                            "unexpected event " + eventType +
                            " while processing element \"" + elementName + "\".");
                        FFDCFilter.processException(re, DDParser.class.getName(), "410", this);
                        break; // Log an FFDC, but otherwise ignore.
                }
            }
        } catch (XMLStreamException e) {
            throw new ParseException(xmlStreamError(), e);
        }

        // Note the return in the END_ELEMENT case.
        throw new ParseException(endElementNotFound());
    }
    
    // Runtime state ...

    /** The raw reader used by the parser. */
    private XMLStreamReader xsr;
    
    @Trivial
    public String getNamespaceURI(String prefix) {
        return xsr.getNamespaceURI(prefix);
    }

    public int getLineNumber() {
        return ( (xsr == null) ? -1 : xsr.getLocation().getLineNumber() );
    }

    // Parse lifecycle ...

    /**
     * Parse lifecycle: Before parsing, create the raw stream reader
     * used by the parser.
     * 
     * The reader is given a custom {@link XMLResolver} which has
     * a callback to {@link DDParser#setPublicID(String)}.  The
     * resolver resolves all entities to empty streams.
     *
     * @param resolver The entity resolver used by the reader.
     * @param stream The input stream which is to be parsed.
     *
     * @return A new XML stream reader.
     *
     * @throws ParseException Thrown if an error occurs while
     *     creating the reader.
     */
    @FFDCIgnore({ IllegalArgumentException.class, XMLStreamException.class })
    private XMLStreamReader createReader(InputStream stream)
        throws ParseException {

        try {
            XMLInputFactory inputFactory = DesignatedXMLInputFactory.newInstance();

            // IBM XML parser requires a special property to enable line numbers.
            try {
                inputFactory.setProperty("javax.xml.stream.isSupportingLocationCoordinates", true);
            } catch (IllegalArgumentException e) {
                // Do not FFDC; Ignore
            }

            XMLResolver resolver = new XMLResolver() {
                @Override
                public Object resolveEntity(
                    String publicID, String systemID,
                    String baseURI, String namespace) throws XMLStreamException {

                    DDParser.this.setPublicID(publicID);

                    return new ByteArrayInputStream(new byte[0]);
                }
            };
            inputFactory.setXMLResolver(resolver);

            return inputFactory.createXMLStreamReader(stream);

        } catch (XMLStreamException e) {
            throw new ParseException(xmlStreamError(), e);
        }
    }

    /**
     * Parse lifecycle: After creating the xml reader, parse the starting
     * element of the descriptor.  Create the root parse element.
     * 
     * The starting element contains version information which drives
     * the parse of the body of the descriptor.  Scrape that information
     * from the reader in preparation for parsing the body of the
     * descriptor, then create the root parse element.
     *
     * @throws ParseException Thrown if the read of the root element
     *     failed.  Thrown if the input stream does not contain a
     *     start element, or if the root parse element could not
     *     be created.
     */
    @FFDCIgnore(XMLStreamException.class)
    private void parseToRootElement() throws ParseException {
        try {
            while (!xsr.isStartElement()) {
                if (!xsr.hasNext()) {
                    // Need to find a start element before running
                    // out of elements.
                    throw new ParseException(errorMissingRootElement());
                }
                xsr.next();
            }
        } catch (XMLStreamException e) {
            throw new ParseException(xmlStreamError(), e);
        }

        namespace = xsr.getNamespaceURI();

        rootElementLocalName = xsr.getLocalName();
        currentElementLocalName = rootElementLocalName;

        rootParsable = createRootParsable();
    }

    // Parse values: 

    public String dtdPublicId;
    
    /**
     * Callback used by the XML reader.  Called when handling
     * an external entity.
     *
     * @param publicId The public ID value of an external entity.
     */
    protected void setPublicID(String publicId) {
        if ( this.dtdPublicId != null ) {
            // TODO: Emit a warning.
        }

        this.dtdPublicId = publicId;
    }

    /** Namespace obtained from the XML reader after parsing the starting element. */
    public String namespace;

    /**
     * The specification version used by the descriptor.
     *
     * Set by examining the DTD public ID or the namespace, both which are
     * obtained when parsing the starting element of the descriptor.
     */
    public int version;

    /**
     * The minimum Java EE version which supports the descriptor version.
     * 
     * Set based on the specification version of the descriptor.
     * 
     * The platform version provides an alternative way to control
     * parsing.  The specification version is useful for API specific
     * parsing steps, while the platform version is useful for parsing
     * steps which are common across specifications.
     */
    public int eePlatformVersion;

    protected ParsableElement rootParsable;

    @Trivial
    public ParsableElement getRootParsable() {
        return rootParsable;
    }

    // TODO: Not sure what this is for ... it doesn't seem
    //       to be used.

    protected final Object describeRootParsable = new Object() {
        @Trivial
        @Override
        public String toString() {
            DDParser.Diagnostics diag = new DDParser.Diagnostics(idMap);
            diag.describe( (rootParsable != null ? rootParsable.toString() : "DDParser"),
                           rootParsable );
            return diag.getDescription();
        }
    };

    /**
     * Parse lifecycle: Create the root parse element.  This follows the parse
     * of the starting element.
     *
     * @return The new root parse element.
     *
     * @throws ParseException Thrown if the root parse element could not be
     *     created.  Most often because invalid values were obtained from
     *     the starting element.
     */
    protected abstract ParsableElement createRootParsable() throws ParseException;

    // Parse state ...
    
    public final ComponentIDMap idMap;

    public String rootElementLocalName;
    protected String currentElementLocalName;
    
    protected final StringBuilder contentBuilder;

    @Trivial
    public void appendTextToContent() {
        contentBuilder.append(xsr.getText());
    }

    @Trivial
    public String getContentString(boolean untrimmed) {
        String lexical = contentBuilder.toString();
        if (!untrimmed && trimSimpleContent) {
            lexical = lexical.trim();
        }
        contentBuilder.setLength(0);
        return lexical;
    }

    @Trivial
    public String getContentString() {
        return getContentString(false);
    }

    //

    protected void fillRootElement() {
        if (rootParsable == null) {
            throw new IllegalStateException();
        }
    }    

    // Parsing helpers ...

    /**
     * Retrieve an attribute value from the current parse element.
     * 
     * Answer the value of the attribute which matches a target
     * namespace and which matches a target name.
     * 
     * Two modes are supported: If the target namespace is null,
     * the namespace of the current parse element is ignored.
     * 
     * If the target namespace is not null, the target namespace
     * is matched against the namespace of the current parse elemnt.
     * 
     * @param targetNs The namespace of the target attribute.
     * @param targetName The name of the target attribute.
     *
     * @return The value of the attribute which matches the target
     *     namespace and which matches the target name.  Null if no
     *     match is found. 
     */
    @Trivial
    public String getAttributeValue(String targetNs, String targetName) {
        boolean checkNS;
        if ( targetNs != null ) {
            checkNS = true;
            if (targetNs.isEmpty()) {
                targetNs = null;
            }
        } else {
            checkNS = false;
        }

        int attrCount = xsr.getAttributeCount();
        for (int attrNo = 0; attrNo < attrCount; ++attrNo) {
            String attrName = xsr.getAttributeLocalName(attrNo);
            if (!targetName.equals(attrName)) {
                continue;
            }

            if (checkNS) {
                String attrNs = xsr.getAttributeNamespace(attrNo);
                if ((attrNs != null) && attrNs.isEmpty()) {
                    attrNs = null;
                }
                if (attrNs != targetNs) {
                    continue;
                }
            }
            
            return xsr.getAttributeValue(attrNo);
        }

        return null;
    }

    @Trivial
    public String getAttributeValue(int index) {
        return xsr.getAttributeValue(index);
    }

    public BooleanType parseBooleanAttributeValue(int index) throws ParseException {
        return parseBoolean(getAttributeValue(index));
    }

    public IDType parseIDAttributeValue(int index) throws ParseException {
        return parseID(getAttributeValue(index));
    }

    public IntegerType parseIntegerAttributeValue(int index) throws ParseException {
        return parseInteger(getAttributeValue(index));
    }

    public LongType parseLongAttributeValue(int index) throws ParseException {
        return parseLong(getAttributeValue(index));
    }

    public QNameType parseQNameAttributeValue(int index) throws ParseException {
        return parseQName(getAttributeValue(index));
    }

    public StringType parseStringAttributeValue(int index) throws ParseException {
        return parseString(getAttributeValue(index));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ParsableListImplements<StringType, String> parseStringListAttributeValue(int index) throws ParseException {
        return (ParsableListImplements) parseTokenAttributeValue(index).split(this, " ");
    }

    public ProtectedStringType parseProtectedStringAttributeValue(int index) throws ParseException {
        return parseProtectedString(getAttributeValue(index));
    }

    public TokenType parseTokenAttributeValue(int index) throws ParseException {
        return parseToken(getAttributeValue(index));
    }

    public <T extends Enum<T>> T parseEnumAttributeValue(int index, Class<T> valueClass) throws ParseException {
        return parseEnum(getAttributeValue(index), valueClass);
    }

    @Trivial
    public boolean isWhiteSpace() {
        return xsr.isWhiteSpace();
    }

    // TODO: This does not seem to be in use.
    @Trivial
    @FFDCIgnore(XMLStreamException.class)
    public void skipSubtree() throws ParseException {
        try {
            int depth = 0;
            while (xsr.hasNext()) {
                if (xsr.isStartElement()) {
                    depth++;
                } else if (xsr.isEndElement()) {
                    if (--depth == 0) {
                        return;
                    }
                }
                xsr.next();
            }
            throw new ParseException(endElementNotFound());
        } catch (XMLStreamException e) {
            throw new ParseException(xmlStreamError(), e);
        }
    }

    // Parse helpers ...

    public BooleanType parseBoolean(String lexical) throws ParseException {
        return BooleanType.wrap(this, lexical);
    }

    public IDType parseID(String lexical) throws ParseException {
        return IDType.wrap(this, lexical);
    }

    public IntegerType parseInteger(String lexical) throws ParseException {
        return IntegerType.wrap(this, lexical);
    }

    public LongType parseLong(String lexical) throws ParseException {
        return LongType.wrap(this, lexical);
    }

    public QNameType parseQName(String lexical) throws ParseException {
        return QNameType.wrap(this, lexical);
    }

    public StringType parseString(String lexical) throws ParseException {
        return StringType.wrap(this, lexical);
    }

    public ProtectedStringType parseProtectedString(@Sensitive String lexical) throws ParseException {
        return ProtectedStringType.wrap(lexical);
    }

    public TokenType parseToken(String lexical) throws ParseException {
        return TokenType.wrap(this, lexical);
    }

    // Version dependent enum type ...

    /**
     * A mix-in interface for enums that allows values to be used only if
     * the parser version is at the correct level.
     */
    public interface VersionedEnum {
        /**
         * The minimum value for {@link DDParser#version} that is required for
         * this constant to be valid.
         */
        int getMinVersion();
    }

    /**
     * Return an array of the constants for the enum that are valid based on the
     * version of this parser.
     */
    private <T extends Enum<T>> Object[] getValidEnumConstants(Class<T> valueClass) {
        T[] constants = valueClass.getEnumConstants();
        if (!VersionedEnum.class.isAssignableFrom(valueClass)) {
            return constants;
        }

        List<T> valid = new ArrayList<T>(constants.length);
        for (T value : constants) {
            VersionedEnum versionedValue = (VersionedEnum) value;
            if (version >= versionedValue.getMinVersion()) {
                valid.add(value);
            }
        }

        return valid.toArray();
    }

    @FFDCIgnore({ IllegalArgumentException.class, IncompatibleClassChangeError.class })
    public <T extends Enum<T>> T parseEnum(String value, Class<T> valueClass) throws ParseException {
        T constant;
        try {
            try {
                constant = Enum.valueOf(valueClass, value);
            } catch (IncompatibleClassChangeError e) {
                if (TraceComponent.isAnyTracingEnabled() && tc.isDebugEnabled())
                    Tr.debug(tc, "rethrowing IncompatibleClassChangeError as IllegalArgumentException", e);
                // FIXME: IBM Java 7 Enum.valueOf uses Class.getCanonicalName,
                // but the JVM has a bug in the processing of the InnerClasses
                // class attribute that causes that method to to fail erroneously.
                throw new IllegalArgumentException(e);
            }
        } catch (IllegalArgumentException e) {
            throw new ParseException(invalidEnumValue(value, getValidEnumConstants(valueClass)), e);
        }

        if (constant instanceof VersionedEnum) {
            VersionedEnum versionedConstant = (VersionedEnum) constant;
            if (version < versionedConstant.getMinVersion()) {
                throw new ParseException(invalidEnumValue(value, getValidEnumConstants(valueClass)));
            }
        }

        return constant;
    }
}
