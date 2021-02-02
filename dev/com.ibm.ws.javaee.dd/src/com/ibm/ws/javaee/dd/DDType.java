/*******************************************************************************
 * Copyright (c) 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.javaee.dd;

/**
 * Type information for all JavaEE / Jakarta deployment descriptors.
 */
public enum DDType {
    // com.ibm.ws.javaee.ddmodel.app.ApplicationAdapter
    // com.ibm.ws.javaee.ddmodel.app.ApplicationDDParser
    App( "META-INF/application.xml", "application.xml",
         new int[] { 12, 13, 14, 50, 60, 70, 80, 90 } ),
    // com.ibm.ws.javaee.ddmodel.appbnd.ApplicationBndAdapter
    // com.ibm.ws.javaee.ddmodel.appbnd.ApplicationBndDDParser
    AppBnd( new String[] { "META-INF/application-bnd.xmi",
                           "META-INF/application-bnd.xml" },
            new String[] { "application-bnd.xmi",
                           "application-bnd.xml" },
            new int[] { 10, 11, 12, -1 } ),
    // com.ibm.ws.javaee.ddmodel.appext.ApplicationExtAdapter
    // com.ibm.ws.javaee.ddmodel.appext.ApplicationExtDDParser
    AppExt( new String[] { "META-INF/application-ext.xmi",
                           "META-INF/application-ext.xml" },
            new String[] { "application-ext.xmi",
                           "application-ext.xml" },
            new int[] { 10, 11, -1 } ),

    // com.ibm.ws.javaee.ddmodel.client.ApplicationClientAdapter
    // com.ibm.ws.javaee.ddmodel.client.ApplicationClientEntryAdapter
    // com.ibm.ws.javaee.ddmodel.client.ApplicationClientDDParser
    AppClient( "META-INF/application-client.xml", "application-client.xml",
               new int[] { 12, 13, 14, 50, 60, 70, 80, 90 } ),
    // com.ibm.ws.javaee.ddmodel.clientbnd.ApplicationClientBndAdapter
    // com.ibm.ws.javaee.ddmodel.clientbnd.ApplicationClientBndDDParser
    AppClientBnd( new String[] { "META-INF/application-client-bnd.xmi",
                                 "META-INF/application-client-bnd.xml" },
                 new String[] { "application-client-bnd.xmi",
                                "application-client-bnd.xml" },
                 new int[] { 10, 11, 12, -1 } ),

    // com.ibm.ws.javaee.ddmodel.ejb.EJBJarAdapter
    // com.ibm.ws.javaee.ddmodel.ejb.EJBJarEntryAdapter
    // com.ibm.ws.javaee.ddmodel.ejb.EJBJarDDParser
    EJBJar( "META-INF/ejb-jar.xml", "ejb-jar.xml",
            new int[] { 11, 20, 21, 30, 31, 32, 40 } ),
    // com.ibm.ws.javaee.ddmodel.ejbbnd.EJBJarBndAdapter
    // com.ibm.ws.javaee.ddmodel.ejbbnd.EJBJarBndDDParser    
    EJBJarBnd( new String[] { "META-INF/ejb-jar-bnd.xmi",
                              "WEB-INF/ejb-jar-bnd.xmi",
                              "META-INF/ejb-jar-bnd.xml",
                              "WEB-INF/ejb-jar-bnd.xml" },
               new String[] { "ejb-jar-bnd.xmi",
                              "ejb-jar-bnd.xml" },
               new int[] { 10, 11, 12, -1 } ),
    // com.ibm.ws.javaee.ddmodel.ejbext.EJBJarExtAdapter
    // com.ibm.ws.javaee.ddmodel.ejbext.EJBJarExtDDParser
    EJBJarExt( new String[] { "META-INF/ejb-jar-ext.xmi",
                              "WEB-INF/ejb-jar-ext.xmi",
                              "META-INF/ejb-jar-ext.xml",
                              "WEB-INF/ejb-jar-ext.xml" },
               new String[] { "ejb-jar-ext.xmi",
                              "ejb-jar-ext.xml" },
               new int[] { 10, 11, -1 } ),

    // com.ibm.ws.javaee.ddmodel.jsf.FacesConfigAdapter
    // com.ibm.ws.javaee.ddmodel.jsf.FacesConfigEntryAdapter
    // com.ibm.ws.javaee.ddmodel.jsf.FacesConfigDDParser
    FacesConfig( "WEB-INF/faces-config.xml", "faces-config.xml",
                 new int[] { 10, 11, 12, 20, 21, 22, 23, 30 } ),

    // com.ibm.ws.javaee.ddmodel.managedbean.ManagedBeanBndAdapter
    // com.ibm.ws.javaee.ddmodel.managedbean.ManagedBeanBndDDParser
    ManagedBeanBnd( new String[] { "META-INF/ibm-managed-bean-bnd.xml",
                                   "WEB-INF/ibm-managed-bean-bnd.xml" },
                    new String[] { "ibm-managed-bean-bnd.xml" },
                    new int[] { 10, 11 } ),
    
    // com.ibm.ws.javaee.ddmodel.permissions.PermissionsAdapter
    // com.ibm.ws.javaee.ddmodel.permissions.PermissionsConfigDDParser
    PermissionsConfig( "META-INF/permissions.xml", "permissions.xml",
                       new int[] { 70, 90 } ),

    // com.ibm.ws.javaee.ddmodel.bval.ValidationConfigAdapter
    // com.ibm.ws.javaee.ddmodel.bval.ValidationConfigDDParser
    ValidationConfig( new String[] { "META-INF/validation.xml",
                                     "WEB-INF/validation.xml",
                                     "WEB-INF/classes/META-INF/validation.xml" },
                      new String[] { "validation.xml" },
                      new int[] { 10, 11 } ),

    // com.ibm.ws.javaee.ddmodel.web.WebAppAdapter
    // com.ibm.ws.javaee.ddmodel.web.WebAppEntryAdapter
    // com.ibm.ws.javaee.ddmodel.web.WebAppDDParser
    WebApp( "WEB-INF/web.xml", "web.xml",
            new int[] { 22, 23, 24, 25, 30, 31, 40, 50 } ),
    // com.ibm.ws.javaee.ddmodel.webbnd.WebBndAdapter
    // com.ibm.ws.javaee.ddmodel.webbnd.WebBndDDParser
    WebAppBnd( new String[] { "WEB-INF/web-bnd.xmi",
                              "WEB-INF/web-bnd.xml" },
               new String[] { "web-bnd.xmi",
                              "web-bnd.xml" },
               new int[] { 10, 11, 12, -1 } ),
    // com.ibm.ws.javaee.ddmodel.webext.WebExtAdapter
    // com.ibm.ws.javaee.ddmodel.webext.WebExtDDParser
    WebAppExt( new String[] { "WEB-INF/web-ext.xmi",
                              "WEB-INF/web-ext.xml" },
               new String[] { "web-ext.xmi",
                              "web-ext.xml" },
               new int[] { 10, 11, -1 } ),

    // com.ibm.ws.javaee.ddmodel.web.WebFragmentAdapter
    // com.ibm.ws.javaee.ddmodel.web.WebFragmentDDParser
    WebFragment( "META-INF/web-fragment.xml", "web-fragment.xml",
                 new int[] { 30, 31, 40, 50 } ),

    // com.ibm.ws.javaee.ddmodel.ws.WebservicesAdapter
    // com.ibm.ws.javaee.ddmodel.ws.WebServicesDDParser
    WebServices( new String[] { "META-INF/webservices.xml",
                                "WEB-INF/webservices.xml" },
                 new String[] { "webservices.xml" },
                 new int[] { 11, 12, 13, 14, 20 } ),
    // com.ibm.ws.javaee.ddmodel.wsbnd.adapter.WebservicesBndAdapter
    WebServicesBnd( new String[] { "META-INF/ibm-ws-bnd.xml",
                                   "WEB-INF/ibm-ws-bnd.xml" },
                    new String[] { "ibm-ws-bnd.xml" },
                    new int[] { 10 } );
    
    private DDType(String path, String name, int[] versions) {
        this( new String[] { path },
              new String[] { name },
              versions );
    }

    private DDType(String[] paths, String[] names, int[] versions) {
        this.paths = paths;
        this.names = names;
        this.versions = versions;
    }    

    private final String[] paths;
    private final String[] names;

    /**
     * Answer the allowed paths to the descriptor.
     *
     * @return The allowed paths to the descriptor.
     */
    public String[] getPaths() {
        return paths;
    }

    /**
     * Answer the simple names of the descriptor.
     * 
     * Multiple names are possible, because of the change
     * of extension of bindings and extensions files.
     *
     * @return The simple names of the descriptor.
     */
    public String[] getNames() {
        return names;
    }

    private final int[] versions;

    /**
     * Answer the integer versions of this descriptor type.
     * 
     * These are multiplied by ten, for example, "2.5" is answered
     * as "25".
     *
     * @return The supported versions of this descriptor type.
     */
    public int[] getVersions() {
        return versions;
    }

    /**
     * Answer the type versions which are associated with this type.
     *
     * @return The type versions which are associated with this type.
     */
    public DDTypeVersion[] getTypeVersions() {
        return DDTypeVersion.getVersions(this);
    }

    protected static final boolean USES_SCHEMA = true;
    
    /**
     * Tell if the specified version of this descriptor type uses
     * a schema.
     *
     * @param version A version of this descriptor type.
     *
     * @return True or false telling if the descriptor version uses a schema. 
     */
    public boolean usesSchema(int version) {
        DDTypeVersion typeVersion = DDTypeVersion.getVersion(this, version);
        return ( (typeVersion == null) ? false : typeVersion.usesSchema() );
    }

    /**
     * Answer the type referent: Either, the public ID, for a descriptor which
     * uses a DOCTYPE, or a namespace, for a descriptor which uses a schema.
     *
     * See {@link #usesSchema(int)}.
     *
     * @return The type referent of a version of this descriptor type.
     */
    public String getReferent(int version) {
        DDTypeVersion typeVersion = DDTypeVersion.getVersion(this, version);
        return ( (typeVersion == null) ? null : typeVersion.getReferent() );        
    }
}
