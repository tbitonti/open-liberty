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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ibm.ws.javaee.dd.DDTypeConstants.*;

/**
 * Type for version specific {@link DDType} values.
 */
public enum DDTypeVersion {
    App_12(DDType.App, 12, !DDType.USES_SCHEMA, APP_12_DTD),
    App_13(DDType.App, 13, !DDType.USES_SCHEMA, APP_13_DTD),
    App_14(DDType.App, 14, DDType.USES_SCHEMA, SUN_J2EE_NS),
    App_50(DDType.App, 50, DDType.USES_SCHEMA, SUN_JAVAEE_NS),
    App_60(DDType.App, 60, DDType.USES_SCHEMA, SUN_JAVAEE_NS),
    App_70(DDType.App, 70, DDType.USES_SCHEMA, JCP_JAVAEE_NS),
    App_80(DDType.App, 80, DDType.USES_SCHEMA, JCP_JAVAEE_NS),
    App_90(DDType.App, 90, DDType.USES_SCHEMA, JAKARTAEE_NS),

    AppBnd_10(DDType.AppBnd, 10, DDType.USES_SCHEMA, WAS_JAVAEE_NS),
    AppBnd_11(DDType.AppBnd, 11, DDType.USES_SCHEMA, WAS_JAVAEE_NS),
    AppBnd_12(DDType.AppBnd, 12, DDType.USES_SCHEMA, WAS_JAVAEE_NS),
    AppBnd_None(DDType.AppBnd, -1, DDType.USES_SCHEMA, APP_BND_XMI),
    
    AppExt_10(DDType.AppExt, 10, DDType.USES_SCHEMA, WAS_JAVAEE_NS),
    AppExt_11(DDType.AppExt, 11, DDType.USES_SCHEMA, WAS_JAVAEE_NS),
    AppExt_None(DDType.AppExt, 12, DDType.USES_SCHEMA, APP_EXT_XMI),
    
    AppClient_12(DDType.App, 12, !DDType.USES_SCHEMA, APP_CLIENT_12_DTD),
    AppClient_13(DDType.App, 13, !DDType.USES_SCHEMA, APP_CLIENT_13_DTD),
    AppClient_14(DDType.App, 14, DDType.USES_SCHEMA, SUN_J2EE_NS),
    AppClient_50(DDType.App, 50, DDType.USES_SCHEMA, SUN_JAVAEE_NS),
    AppClient_60(DDType.App, 60, DDType.USES_SCHEMA, SUN_JAVAEE_NS),
    AppClient_70(DDType.App, 70, DDType.USES_SCHEMA, JCP_JAVAEE_NS),
    AppClient_80(DDType.App, 80, DDType.USES_SCHEMA, JCP_JAVAEE_NS),
    AppClient_90(DDType.App, 90, DDType.USES_SCHEMA, JAKARTAEE_NS),
    
    AppClientBnd_10(DDType.AppClientBnd, 10, !DDType.USES_SCHEMA, WAS_JAVAEE_NS),
    AppClientBnd_11(DDType.AppClientBnd, 11, !DDType.USES_SCHEMA, WAS_JAVAEE_NS),
    AppClientBnd_12(DDType.AppClientBnd, 12, !DDType.USES_SCHEMA, WAS_JAVAEE_NS),
    AppClientBnd_None(DDType.AppClientBnd, -1, !DDType.USES_SCHEMA, APP_CLIENT_BND_XMI),

    EJBJar_11(DDType.EJBJar, 11, !DDType.USES_SCHEMA, EJBJAR_11_DTD),
    EJBJar_20(DDType.EJBJar, 20, !DDType.USES_SCHEMA, EJBJAR_20_DTD),
    EJBJar_21(DDType.EJBJar, 21, DDType.USES_SCHEMA, SUN_J2EE_NS),
    EJBJar_30(DDType.EJBJar, 30, DDType.USES_SCHEMA, SUN_JAVAEE_NS),
    EJBJar_31(DDType.EJBJar, 31, DDType.USES_SCHEMA, SUN_JAVAEE_NS),
    EJBJar_32(DDType.EJBJar, 32, DDType.USES_SCHEMA, JCP_JAVAEE_NS),
    EJBJar_40(DDType.EJBJar, 40, DDType.USES_SCHEMA, JAKARTAEE_NS),

    EJBJarBnd_10(DDType.EJBJarBnd, 10, DDType.USES_SCHEMA, WAS_JAVAEE_NS),
    EJBJarBnd_11(DDType.EJBJarBnd, 11, DDType.USES_SCHEMA, WAS_JAVAEE_NS),
    EJBJarBnd_12(DDType.EJBJarBnd, 12, DDType.USES_SCHEMA, WAS_JAVAEE_NS),
    EJBJarBnd_None(DDType.EJBJarBnd, -1, DDType.USES_SCHEMA, EJBJAR_BND_XMI),

    EJBJarExt_10(DDType.EJBJarExt, 10, DDType.USES_SCHEMA, WAS_JAVAEE_NS),
    EJBJarExt_11(DDType.EJBJarExt, 11, DDType.USES_SCHEMA, WAS_JAVAEE_NS),
    EJBJarExt_None(DDType.EJBJarExt, -1, DDType.USES_SCHEMA, EJBJAR_EXT_XMI),

    FacesConfig_10(DDType.FacesConfig, 10, !DDType.USES_SCHEMA, FACES_10_DTD),    
    FacesConfig_11(DDType.FacesConfig, 11, !DDType.USES_SCHEMA, FACES_11_DTD),
    FacesConfig_12(DDType.FacesConfig, 12, DDType.USES_SCHEMA, SUN_JAVAEE_NS),   
    FacesConfig_20(DDType.FacesConfig, 20, DDType.USES_SCHEMA, SUN_JAVAEE_NS),
    FacesConfig_21(DDType.FacesConfig, 21, DDType.USES_SCHEMA, SUN_JAVAEE_NS),
    FacesConfig_22(DDType.FacesConfig, 22, DDType.USES_SCHEMA, JCP_JAVAEE_NS),
    FacesConfig_23(DDType.FacesConfig, 23, DDType.USES_SCHEMA, JCP_JAVAEE_NS),
    FacesConfig_30(DDType.FacesConfig, 30, DDType.USES_SCHEMA, JAKARTAEE_NS),

    ManagedBeanBnd_10(DDType.ManagedBeanBnd, 10, DDType.USES_SCHEMA, WAS_JAVAEE_NS),
    ManagedBeanBnd_11(DDType.ManagedBeanBnd, 11, DDType.USES_SCHEMA, WAS_JAVAEE_NS),

    PermissionsConfig_70(DDType.PermissionsConfig, 70, DDType.USES_SCHEMA, JCP_JAVAEE_NS),
    PermissionsConfig_90(DDType.PermissionsConfig, 90, DDType.USES_SCHEMA, JAKARTAEE_NS),

    ValidationConfig_10(DDType.ValidationConfig, 10, DDType.USES_SCHEMA, JBOSS_VALIDATION_NS),
    ValidationConfig_11(DDType.ValidationConfig, 11, DDType.USES_SCHEMA, JBOSS_VALIDATION_NS),

    WebApp_22(DDType.WebApp, 22, !DDType.USES_SCHEMA, WEBAPP_22_DTD),
    WebApp_23(DDType.WebApp, 23, !DDType.USES_SCHEMA, WEBAPP_23_DTD),
    WebApp_24(DDType.WebApp, 24, DDType.USES_SCHEMA, SUN_J2EE_NS),
    WebApp_25(DDType.WebApp, 25, DDType.USES_SCHEMA, SUN_JAVAEE_NS),
    WebApp_30(DDType.WebApp, 30, DDType.USES_SCHEMA, SUN_JAVAEE_NS),
    WebApp_31(DDType.WebApp, 31, DDType.USES_SCHEMA, JCP_JAVAEE_NS),
    WebApp_40(DDType.WebApp, 40, DDType.USES_SCHEMA, JCP_JAVAEE_NS),
    WebApp_50(DDType.WebApp, 50, DDType.USES_SCHEMA, JAKARTAEE_NS),
        
    WebAppBnd_10(DDType.WebAppBnd, 10, DDType.USES_SCHEMA, WAS_JAVAEE_NS),
    WebAppBnd_11(DDType.WebAppBnd, 11, DDType.USES_SCHEMA, WAS_JAVAEE_NS),
    WebAppBnd_12(DDType.WebAppBnd, 12, DDType.USES_SCHEMA, WAS_JAVAEE_NS),
    WebAppBnd_None(DDType.WebAppBnd, -1, DDType.USES_SCHEMA, WEBAPP_BND_XMI),

    WebAppExt_10(DDType.WebAppExt, 10, DDType.USES_SCHEMA, WAS_JAVAEE_NS),
    WebAppExt_11(DDType.WebAppExt, 11, DDType.USES_SCHEMA, WAS_JAVAEE_NS),
    WebAppExt_None(DDType.WebAppExt, -1, DDType.USES_SCHEMA, WEBAPP_EXT_XMI),    

    WebFragment_30(DDType.WebFragment, 30, DDType.USES_SCHEMA, SUN_JAVAEE_NS),
    WebFragment_31(DDType.WebFragment, 31, DDType.USES_SCHEMA, JCP_JAVAEE_NS),
    WebFragment_40(DDType.WebFragment, 40, DDType.USES_SCHEMA, JCP_JAVAEE_NS),
    WebFragment_50(DDType.WebFragment, 50, DDType.USES_SCHEMA, JAKARTAEE_NS),

    // The referent is unused by the Liberty WebServices parse code.
    WebServices_11(DDType.WebServices, 11, DDType.USES_SCHEMA, null),
    WebServices_12(DDType.WebServices, 12, DDType.USES_SCHEMA, null),
    WebServices_13(DDType.WebServices, 13, DDType.USES_SCHEMA, null),
    WebServices_14(DDType.WebServices, 14, DDType.USES_SCHEMA, null),
    WebServices_20(DDType.WebServices, 20, DDType.USES_SCHEMA, null),

    WebServicesBnd(DDType.WebServicesBnd, 10, DDType.USES_SCHEMA, WAS_JAVAEE_NS);

    private DDTypeVersion() {
        this.ddType = null;
        this.version = 0;
        this.usesSchema = false;
        this.referent = null;
    }

    private DDTypeVersion(DDType ddType, int version, boolean usesSchema, String referent) {
        this.ddType = ddType;
        this.version = version;
        this.usesSchema = usesSchema;
        this.referent = referent;
    }
    
    private final DDType ddType;

    /**
     * Answer the DD type of this DD type version.
     *
     * @return The type of this type version.
     */
    public DDType getType() {
        return ddType;
    }

    private final int version;

    /**
     * Answer the version of this type.
     *
     * These are multiplied by ten, for example, "2.5" is answered
     * as "25".
     *
     * @return This version.
     */
    public int getVersion() {
        return version;
    }

    private final boolean usesSchema;

    /**
     * Tell if this version of the associated descriptor
     * type uses a schema. 
     *
     * @return True or false telling if this descriptor
     *     version uses a schema. 
     */
    public boolean usesSchema() {
        return usesSchema;
    }

    private final String referent;

    /**
     * Answer the type referent: Either, the public ID, for a descriptor which
     * uses a DOCTYPE, or a namespace, for a descriptor which uses a schema.
     *
     * See {@link #usesSchema()}.
     *
     * @return The type referent of this version of the associated
     *     descriptor type.
     */
    public String getReferent() {
        return referent;
    }
    
    /**
     * Table of all type versions.
     * 
     * Values are simple arrays of type versions.  Each type is
     * expected to have only a few type versions.  Simple list
     * lookup is expected to be fast, and using an array has a
     * lot less overhead.
     */
    private static final EnumMap<DDType, DDTypeVersion[]> ddVersions;
    
    static {
        Map<DDType, List<DDTypeVersion>> useVersions = new HashMap<>();

        for ( DDTypeVersion typeVersion : DDTypeVersion.values() ) {
            DDType ddType = typeVersion.getType();
            List<DDTypeVersion> versions = useVersions.get(ddType);
            if ( versions == null ) {
                versions = new ArrayList<DDTypeVersion>(5);
                useVersions.put(ddType, versions);
            }
            versions.add(typeVersion);
        }
        
        ddVersions = new EnumMap<DDType, DDTypeVersion[]>(DDType.class);

        for ( Map.Entry<DDType, List<DDTypeVersion>> typeEntry : useVersions.entrySet() ) {
            DDType ddType = typeEntry.getKey();
            List<DDTypeVersion> typeVersionsList = typeEntry.getValue();
            DDTypeVersion[] typeVersions =
                typeVersionsList.toArray( new DDTypeVersion[ typeVersionsList.size() ]);
            ddVersions.put(ddType, typeVersions);
        }
    }

    public static DDTypeVersion[] getVersions(DDType ddType) {
        return ddVersions.get(ddType);
    }
    
    public static DDTypeVersion getVersion(DDType ddType, int version) {
        DDTypeVersion[] versions = getVersions(ddType);
        if ( versions == null ) {
            return null;
        }
        for ( DDTypeVersion typeVersion : versions ) {
            if ( typeVersion.getVersion() == version ) {
                return typeVersion;
            }
        }
        return null;
    }
}
