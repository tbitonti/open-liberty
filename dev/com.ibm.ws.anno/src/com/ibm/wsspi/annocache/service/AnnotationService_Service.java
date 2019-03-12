/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * Copyright IBM Corporation 2011, 2018
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 */

package com.ibm.wsspi.annocache.service;

import com.ibm.wsspi.annocache.classsource.ClassSource_Factory;
import com.ibm.wsspi.annocache.info.InfoStoreFactory;
import com.ibm.wsspi.annocache.targets.AnnotationTargets_Factory;
import com.ibm.wsspi.annocache.targets.cache.TargetCache_Factory;
import com.ibm.wsspi.annocache.util.Util_Factory;

/**
 * <p>Core annotation service.  Annotation services are accessed
 * through four root factory types: {@link Util_Factory}, {@link ClassSource_Factory},
 * {@link AnnotationTargets_Factory}, and {@link InfoStoreFactory}.</p>
 */
public interface AnnotationService_Service {
    String getHashText();

    Util_Factory getUtilFactory();

    ClassSource_Factory getClassSourceFactory();

    TargetCache_Factory getTargetCacheFactory();
    AnnotationTargets_Factory getAnnotationTargetsFactory();

    InfoStoreFactory getInfoStoreFactory();
}
