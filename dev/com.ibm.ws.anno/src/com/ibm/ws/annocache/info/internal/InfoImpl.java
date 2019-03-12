/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * Copyright IBM Corp. 2011, 2018
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 */
package com.ibm.ws.annocache.info.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.asm.Type;

import com.ibm.wsspi.annocache.info.AnnotationInfo;
import com.ibm.wsspi.annocache.info.Info;

public abstract class InfoImpl implements Info {
    private static final String CLASS_NAME = InfoImpl.class.getSimpleName();

    protected final String hashText;

    @Override
    public String getHashText() {
        return hashText;
    }

    @Override
    public String toString() {
        return getHashText();
    }

    // Top O' the world ...

    public InfoImpl(String name, int modifiers, InfoStoreImpl infoStore, String hashSuffix) {
        this.hashText = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + "(" + hashSuffix + ")";

        this.infoStore = infoStore;

        this.name = internName(name);
        this.modifiers = modifiers;
        // log in the concrete constructors
    }

    // Typing ...

    protected ClassCastException createClassCastException(Class<?> targetClass) {
        return new ClassCastException(getClassCastExceptionText(targetClass));
    }

    protected String getClassCastExceptionText(Class<?> targetClass) {
        return ("Cannot convert [ " + getClass() + " ] named [ " + getQualifiedName() + " ] to [ " + targetClass + " ]");
    }

    // Context ...

    protected InfoStoreImpl infoStore;

    @Override
    public InfoStoreImpl getInfoStore() {
        return infoStore;
    }

    protected abstract String internName(String infoName);

    // TODO: Should this be allowed??
    //       The current implementation will cause the
    //       addition of a new class info, and visitor calls on that,
    //       possibly extending the class info data base for
    //       indirect access requests.

    protected ClassInfoImpl getDelayableClassInfo(String className) {
        return getInfoStore().getDelayableClassInfo(className);
    }

    protected ClassInfoImpl getDelayableClassInfo(Type type) {
        return getInfoStore().getDelayableClassInfo(type);
    }

    // Basic state ...

    protected int modifiers;

    @Override
    public int getModifiers() {
        return modifiers;
    }

    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public boolean isPublic() {
        return Modifier.isPublic(getModifiers());
    }

    @Override
    public boolean isProtected() {
        return Modifier.isProtected(getModifiers());
    }

    @Override
    public boolean isPrivate() {
        return Modifier.isPrivate(getModifiers());
    }

    @Override
    public boolean isPackagePrivate() {
        return (getModifiers() & (Modifier.PRIVATE | Modifier.PROTECTED | Modifier.PUBLIC)) == 0;
    }

    /**
     * Tell if the info is flagged as bridge.  This is currently meaningful only for
     * methods.  See {@link Info#ACC_BRIDGE}.
     * 
     * @return True or false telling if the info is flagged as bridge.
     */
    public boolean isBridge() {
        return ( (getModifiers() & ACC_BRIDGE) != 0 );
    }

    /**
     * Tell if the info is flagged as synthetic.  This is currently meaningful only for
     * methods.  See {@link Info#ACC_SYNTHETIC}.
     *
     * @return True or false telling if the info is flagged as synthetic.
     */
    public boolean isSynthetic() {
        return ( (getModifiers() & ACC_SYNTHETIC) != 0 );
    }

    
    
    // Typing ...

    protected String name;

    @Override
    public String getName() {
        return name;
    }

    // Annotation storage ...

    protected List<AnnotationInfoImpl> declaredAnnotations = Collections.emptyList();

    @Override
    public boolean isDeclaredAnnotationPresent() {
        return isAnnotationPresent(getDeclaredAnnotations());
    }

    private static boolean isAnnotationPresent(Collection<AnnotationInfoImpl> annos) {
        return (annos != null && !annos.isEmpty());
    }

    @Override
    public List<AnnotationInfoImpl> getDeclaredAnnotations() {
        return declaredAnnotations;
    }

    public void setDeclaredAnnotations(AnnotationInfoImpl[] annos) {
        declaredAnnotations = Arrays.asList(annos);
    }

    @Override
    public boolean isDeclaredAnnotationPresent(String annotationName) {
        return getDeclaredAnnotation(annotationName) != null;
    }

    @Override
    public AnnotationInfoImpl getDeclaredAnnotation(String annotationClassName) {
        return getAnnotation(getDeclaredAnnotations(), annotationClassName);
    }

    @Override
    public AnnotationInfoImpl getDeclaredAnnotation(Class<? extends Annotation> clazz) {
        return getDeclaredAnnotation(clazz.getName());
    }

    private static AnnotationInfoImpl getAnnotation(Collection<AnnotationInfoImpl> annoInfos, String annotationClassName) {
        if (annoInfos != null) {
            for (AnnotationInfoImpl aii : annoInfos) {
                if (aii.getAnnotationClassName().equals(annotationClassName)) {
                    return aii;
                }
            }
        }

        return null;
    }

    // Optimized to iterate across the declared annotations first.
    @Override
    public boolean isDeclaredAnnotationWithin(Collection<String> annotationNames) {
        if (declaredAnnotations != null) {
            for (AnnotationInfo annotation : declaredAnnotations) {
                if (annotationNames.contains(annotation.getAnnotationClassName())) {
                    return true;
                }
            }
        }

        return false;
    }

    //
    @Override
    public boolean isAnnotationPresent() {
        return isAnnotationPresent(getAnnotations());
    }

    @Override
    public boolean isAnnotationPresent(String annotationName) {
        return getAnnotation(annotationName) != null;
    }

    @Override
    public boolean isAnnotationWithin(Collection<String> annotationNames) {
        for (AnnotationInfo annotation : getAnnotations()) {
            if (annotationNames.contains(annotation.getAnnotationClassName())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<AnnotationInfoImpl> getAnnotations() {
        return getDeclaredAnnotations();
    }

    @Override
    public AnnotationInfoImpl getAnnotation(String annotationName) {
        AnnotationInfoImpl anno = getDeclaredAnnotation(annotationName);
        if (anno == null) {
            anno = getAnnotation(getAnnotations(), annotationName);
        }
        return anno;
    }

    @Override
    public AnnotationInfoImpl getAnnotation(Class<? extends Annotation> clazz) {
        return getAnnotation(clazz.getName());
    }

    //

    @Override
    public abstract void log(Logger logger);

    public void logAnnotations(Logger logger) {
        String methodName = "logAnnotations";

        boolean firstAnnotation = true;
        for (AnnotationInfoImpl nextAnnotation : getAnnotations()) {
            if (firstAnnotation) {
                logger.logp(Level.FINER, CLASS_NAME, methodName, "  Annotations:");
                firstAnnotation = false;
            }

            nextAnnotation.log(logger);
        }

        if (firstAnnotation) {
            logger.logp(Level.FINER, CLASS_NAME, methodName, "  No Annotations");
        }
    }
}
