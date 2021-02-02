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
package com.ibm.ws.javaee.ddmodel;

import com.ibm.websphere.ras.Tr;
import com.ibm.websphere.ras.TraceComponent;
import com.ibm.ws.container.service.app.deploy.ApplicationInfo;
import com.ibm.ws.container.service.app.deploy.ContainerInfo;
import com.ibm.ws.container.service.app.deploy.ModuleInfo;
import com.ibm.ws.container.service.app.deploy.extended.AltDDEntryGetter;
import com.ibm.wsspi.adaptable.module.Entry;
import com.ibm.wsspi.adaptable.module.NonPersistentCache;
import com.ibm.wsspi.artifact.overlay.OverlayContainer;

/**
 * Common type for all deployment descriptor adaptors.
 */
public interface DDAdapter {
    public static final TraceComponent tc = Tr.register(DDAdapter.class);   

    /**
     * Log information about an adapt request.  The three parameters
     * are the adapter which is being used, the overlay container which
     * provides data, and the path to the root container.
     *
     * Application and module information is expected, either in the
     * immediate overlay container, or in one of the parent overlay
     * containers.
     *
     * @param adapter The adapter which is being used.
     * @param rootOverlay The root overlay container which is being adapted.
     * @param appPath The path to the root overlay container.
     */
    public static void logInfo(DDAdapter adapter, OverlayContainer rootOverlay, String appPath) {
        String prefix = adapter.getClass().getSimpleName() + ": ";

        System.out.println(prefix + "Path [ " + appPath + " ]");

        int parentNo = 0;
        for ( OverlayContainer overlay = rootOverlay;
              overlay != null;
              parentNo++, overlay = overlay.getParentOverlay() ) {

            System.out.println(prefix + "Overlay [ " + parentNo + " ] [ " + overlay.getPath() + " ] [ " + overlay + " ]");

            String moduleInfoSource;
            ModuleInfo moduleInfo = (ModuleInfo)
                overlay.getFromNonPersistentCache(appPath, ModuleInfo.class);
            if ( moduleInfo != null ) {
                moduleInfoSource = "container";
            } else {
                moduleInfoSource = "**unavailable**";
            }

            String appInfoSource;

            ApplicationInfo appInfo = (ApplicationInfo)
                overlay.getFromNonPersistentCache(appPath, ApplicationInfo.class);
            if ( appInfo != null ) {
                appInfoSource = "Container";
            } else {
                if ( moduleInfo != null ) {
                    appInfo = moduleInfo.getApplicationInfo();
                    if ( appInfo != null ) {
                        appInfoSource = "Module info";
                    } else {
                        appInfoSource = "**unavailable(module info)**";
                    }
                } else {
                    appInfoSource = "**unavailable(no module info)**";
                }
            }

            if ( appInfo != null ) {
                System.out.println(prefix + "App    [ " + appInfo + " ] [ " + appInfoSource + " ]");
                System.out.println(prefix + "  Name   [ " + appInfo.getName() + " ]");
                System.out.println(prefix + "  Dep    [ " + appInfo.getDeploymentName() + " ]");
                System.out.println(prefix + "  Helper [ " + appInfo.getConfigHelper() + " ]");
            }

            if ( moduleInfo != null ) {
                System.out.println(prefix + "Module [ " + moduleInfo + " ] [ " + moduleInfoSource + " ]");
                System.out.println(prefix + "  Name   [ " + moduleInfo.getName() + " ]");
                System.out.println(prefix + "  URI    [ " + moduleInfo.getURI() + " ]");
            }
        }
    }
    
    public static void logInfoTr(DDAdapter adapter, OverlayContainer rootOverlay, String appPath) {
        if ( !tc.isDebugEnabled() ) {
            return;
        }

        Tr.debug(tc, "Adapter [ {0} ]", adapter);
        Tr.debug(tc, "Path    [ {0} ]", appPath);

        int parentNo = 0;
        for ( OverlayContainer overlay = rootOverlay;
              overlay != null;
              parentNo++, overlay = overlay.getParentOverlay() ) {

            Tr.debug(tc, "Overlay [ {0} ] [ {1} ] [ {2} ]", parentNo, overlay.getPath(), overlay);

            String moduleInfoSource;
            ModuleInfo moduleInfo = (ModuleInfo)
                overlay.getFromNonPersistentCache(appPath, ModuleInfo.class);
            if ( moduleInfo != null ) {
                moduleInfoSource = "container";
            } else {
                moduleInfoSource = "**unavailable**";
            }

            String appInfoSource;

            ApplicationInfo appInfo = (ApplicationInfo)
                overlay.getFromNonPersistentCache(appPath, ApplicationInfo.class);
            if ( appInfo != null ) {
                appInfoSource = "Container";
            } else {
                if ( moduleInfo != null ) {
                    appInfo = moduleInfo.getApplicationInfo();
                    if ( appInfo != null ) {
                        appInfoSource = "Module info";
                    } else {
                        appInfoSource = "**unavailable(module info)**";
                    }
                } else {
                    appInfoSource = "**unavailable(no module info)**";
                }
            }

            if ( appInfo != null ) {
                Tr.debug(tc, "Application [ {0} ] [ {1} ]", appInfo, appInfoSource);
                Tr.debug(tc, "  Name        [ {0} ]", appInfo.getName());
                Tr.debug(tc, "  Deployment  [ {0} ]", appInfo.getDeploymentName());
                Tr.debug(tc, "  Helper      [ {0} ]", appInfo.getConfigHelper());
            }

            if ( moduleInfo != null ) {
                Tr.debug(tc, "Module      [ {0} ] [ {1} ]", moduleInfo, moduleInfoSource);
                Tr.debug(tc, "  Name        [ {0} ]", moduleInfo.getName());
                Tr.debug(tc, "  URI         [ {0} ]", moduleInfo.getURI());
            }
        }
    }

    /** Tag marking that an invalid module name was detected. */    
    String MODULE_NAME_INVALID = "module.name.invalid";

    /**
     * Mark that a module name was absent on module override
     * information -- module bindings and extensions.
     * 
     * Use the mark {@link #MODULE_NAME_INVALID}.
     *
     * The mark is placed on the parent overlay container, which will
     * be the container of the module's enclosing application.
     *
     * @param moduleOverlay The overlay container of the module.  Note
     *     that the mark is recorded to the parent container.
     * @param c The class which is placing the mark.
     *
     * @return True or false telling if the mark was placed.  A false
     *     result means that the mark was already present.
     */
    public static boolean markInvalidModuleName(OverlayContainer moduleOverlay, Class<?> c) {
        return markError(moduleOverlay.getParentOverlay(), c, MODULE_NAME_INVALID);
    }

    /** Tag marking that an missing module name was detected. */    
    String MODULE_NAME_NOT_SPECIFIED = "module.name.not.specified";
  
    /**
     * Mark that a module name was not specified on module override
     * information -- module bindings and extensions.
     * 
     * Use the mark {@link #MODULE_NAME_NOT_SPECIFIED}.
     * 
     * The mark is placed on the parent overlay container, which will
     * be the container of the module's enclosing application.
     * 
     * @param moduleOverlay The overlay container of the module.  Note
     *     that the mark is recorded to the parent container.
     * @param c The class which is placing the mark.
     *
     * @return True or false telling if the mark was placed.  A false
     *     result means that the mark was already present.
     */
    public static boolean markUnspecifiedModuleName(OverlayContainer moduleOverlay, Class<?> c) {
        return markError(moduleOverlay.getParentOverlay(), c, MODULE_NAME_NOT_SPECIFIED);
    }

    /**
     * Mark an error condition to an overlay container.
     *
     * The tag is used as both the overlay key and the overlay value.
     *
     * Each mark is not placed relative to particular data. Each mark
     * may be placed at most once.
     *
     * @param overlay The overlay in which to place the mark.
     * @param c The class with which to associate the error value.
     * @param errorTag The tag used as the mark.
     *
     * @return True or false telling if this is the first placement
     *     of the tag to the overlay.
     */
    public static boolean markError(OverlayContainer overlay, Class<?> c, String errorTag) {
        if ( overlay.getFromNonPersistentCache(errorTag, c) == null ) {
            overlay.addToNonPersistentCache(errorTag, c, errorTag);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Do a typed get on a non-persistent cache.  The result type matches
     * the key type.
     * 
     * (Although, the non-persistent cache more often than not stored a value
     * which is of the type of the associated key, this is not required to
     * be the case.)
     *
     * @param <T> Type of value which is to be retrieved.  Used both as the
     *     key type (as a class type) and as the value type (as an instance
     *     type).
     * @param cache The cache from which to retrieve a value.
     * @param typeKey The key for which to retrieve a value.
     *
     * @return The value of the cache stored under the specified key.
     */
    @SuppressWarnings("unchecked")
    public static <T> T typedGet(NonPersistentCache cache, Class<? extends T> typeKey) {
        return (T) cache.getFromCache(typeKey);
    }

    /**
     * Retrieve the alternate entry getter from the non-persistent cache, and
     * if one is available, attempt to get the alternate entry of the specified
     * type.
     *
     * @param cache The cache from which to retrieve the alternate entry getter.
     *     See {@link AltDDEntryGetter}.
     * @param entryType The type of entry to get using the alternate entry getter.
     *
     * @return The entry of the specified type.  Null if no entry is available,
     *     or if no alternate getter is available.
     */
    public static Entry getAltEntry(NonPersistentCache cache, ContainerInfo.Type entryType) {
        AltDDEntryGetter altDDGetter = DDAdapter.typedGet(cache, AltDDEntryGetter.class);
        if ( altDDGetter == null ) {
            return null;
        }
        return altDDGetter.getAltDDEntry(entryType);
    }
    
    
    /**
     * Strip the extension from a module name.
     *
     * Remove only ".war" or ".jar", and only if these are present in lower case.
     *
     * The module name is as obtained from an application descriptor, or from a web
     * extension override, or from module information.  Module names are usually,
     * but not always, simple file names.  Module names can be relative paths.
     *
     * @param moduleName The module name to strip.
     *
     * @return The module name with its extension removed.
     */
    public static String stripExtension(String moduleName) {
        if ( moduleName.endsWith(".war") || moduleName.endsWith(".jar") ) {
            return moduleName.substring(0, moduleName.length() - 4);
        }
        return moduleName;
    }
}
