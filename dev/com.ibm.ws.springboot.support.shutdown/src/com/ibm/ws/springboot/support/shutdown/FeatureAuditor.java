/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.springboot.support.shutdown;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

import com.ibm.ws.app.manager.springboot.container.ApplicationError;
import com.ibm.ws.app.manager.springboot.container.ApplicationTr;

/**
 * Liberty environment verifier. Verify that the liberty environment
 * is provisioned to run the current spring version.
 */
//@formatter:off
public class FeatureAuditor implements EnvironmentPostProcessor {

<<<<<<< HEAD
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication app) {

        String sbVersion = SpringBootVersion.getVersion();
        checkJavaVersion(sbVersion);

        /*
         * Throw an Application error if the wrong version of spring boot feature is
         * enabled
         */
        try {
            Class.forName("org.springframework.boot.context.embedded.EmbeddedServletContainerFactory");
            Class.forName(
                          "com.ibm.ws.springboot.support.web.server.version20.container.LibertyConfiguration");
            checkSpringBootVersion15();
=======
    protected static String asResourceName(String className) {
        return className.replace('.', '/') + ".class";
    }
>>>>>>> Initial FeatureAuditor update.

    protected static boolean foundClass(String className) {
        return ( FeatureAuditor.class.getClassLoader().getResource( asResourceName(className) ) != null );
    }

    protected static class SpringFeatureRequirement {
        public static final String minVersion;
        public static final String maxVersion;
        public static final String trigger;
        public static final String required;
        public static final String messageId;

        public SpringFeatureRequirement(String minVersion, String maxVersion,
                                 String required,
                                 String messageId) {

            this.minVersion = minVersion;
            this.maxVersion = maxVersion;
            this.required = required;
            this.messageId = messageId;
        }

        public SpringFeatureRequirement(String ... requirementData) {
            this.minVersion = requirementData[0];
            this.maxVersion = requirementData[1];
            this.required = requirementData[2];
            this.messageId = requirementData[3];
        }

<<<<<<< HEAD
        try {
            Class.forName("org.springframework.boot.web.servlet.server.ServletWebServerFactory");
            Class.forName("io.openliberty.springboot.support.web.server.version30.container.LibertyConfiguration");
            checkSpringBootVersion30();

        } catch (ClassNotFoundException e) {

        }

        /* Throw an application error if servlet feature is not enabled */
        try {
            Class.forName("org.springframework.web.WebApplicationInitializer");
            checkServletPresent(sbVersion);
        } catch (ClassNotFoundException e) {

=======
        // Only run this test if the spring version is in range:
        //   min <= spring < max

        /**
         * Tell if this requirement is applicable to a specified
         * spring version.
         *
         * @param springBootVersion A spring version.
         *
         * @return True or false telling if this requirement applies
         *     to the specified version.
         */
        public boolean accept(String springBootVersion) {
            return ( ((minVersion == null) || (minVersion.compareTo(springBootVersion) >= 0)) &&
                     ((maxVersion == null) || (maxVersion.compareTo(springBootVersion) > 0)) );
        }

        /**
         * Verify this requirement.
         *
         * If the spring version is in the range handled by this requirement,
         * verify that the necessary liberty class is present.
         *
         * @param springBootVersion The current spring version.
         *
         * @return True or false telling if the requirement was verified.
         *     This implementation only ever returns true.  Instead of
         *     returning false, an exception is thrown.
         *
         * @throws ApplicationError Thrown if the requirement is not verified.
         */
        public boolean verify(String springBootVersion) throws ApplicationError {
            if ( !foundClass(required) ) {
                throw new ApplicationError(messageId, springBootVersion, required);
            }
>>>>>>> Initial FeatureAuditor update.
        }
    }

    protected static void warning(String msgId, Object...parms) {
        ApplicationTr.warning(msgId, parms);
    }

<<<<<<< HEAD
    private void checkJavaVersion(String sbVersion) {
        String javaVersion = System.getProperty("java.version");

        // java version isnt supported by sb version, upgrade to 2.x or higher
        if (!javaVersion.startsWith("1.")) {
            try {
                Class.forName("org.springframework.boot.context.embedded.EmbeddedServletContainerFactory");
                ApplicationTr.warning(Type.WARNING_UNSUPPORTED_JAVA_VERSION, javaVersion, sbVersion);
            } catch (ClassNotFoundException e) {

            }
        }
    }
=======
    protected static SpringFeatureRequirement[] featureRequirements = new SpringFeatureRequirement[] {
        new SpringFeatureRequirement( "1.5.0", "2.0.0",
                                      "com.ibm.ws.springboot.support.web.server.version15.container.LibertyConfiguration",
                                      Type.ERROR_NEED_SPRING_BOOT_VERSION_15 ),
        new SpringFeatureRequirement( "2.0.0", "3.0.0",
                                      "com.ibm.ws.springboot.support.web.server.version20.container.LibertyConfiguration",
                                      Type.ERROR_NEED_SPRING_BOOT_VERSION_20 ),
        new SpringFeatureRequirement( "3.0.0", null,
                                      "com.ibm.ws.springboot.support.web.server.version30.container.LibertyConfiguration",
                                      Type.ERROR_NEED_SPRING_BOOT_VERSION_30 )
    };

    /**
     * Verify that the liberty server is provisioned correctly for the current spring version.
     *
     * <ul><li>Verify that the java version is sufficient to run the spring version.</li>
     *     <li>Verify that the necessary liberty spring feature is provisioned.</li>
     *     <li>Verify that the necessary web featres are provisioned.</li>
     * </ul>
     *
     * Java checks are done by cross-checking the java version with the spring version.
     *
     * Provisioning checks are done by verifying that specific classes are available in the
     * server environment.
     *
     * Provisioning failures result in a thrown {@link ApplicationExcepption}.
     *
     * Java version failures result in a warning.
     *
     * @param env The configuration environment.  Currently ignored.
     * @param app The spring application.  Currently ignored.
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication app) {
        String springBootVersion = SpringBootVersion.getVersion();
        System.out.println("spring.boot.version = " + springBootVersion);
        boolean springIsAtLeast30 = ( springBootVersion.compareTo("3.0.0") >= 0 );

        String javaVersion = System.getProperty("java.version");
        System.out.println("java.version = " + javaVersion);

        String javaSpecVersion = System.getProperty("java.vm.specification.version");
        System.out.println("java.vm.specification.version = " + javaSpecVersion);

        // The java spec version must always be at least 10.
        // If the spring version is "3.0.0" or higher, the java version
        // must be at least 17.
        int javaSpecVersionNo = Integer.parseInt(javaSpecVersion);
        if ( springIsAtLeast30 ) {
            if ( javaSpecVersionNo < 17 ) {
                warning(Type.WARNING_UNSUPPORTED_JAVA_VERSION, javaVersion, springBootVersion, "java 17");
            } else {
                System.out.println("Validated java version requirement [ Java 17 ]");
            }
        } else {
            if ( javaSpecVersionNo < 10 ) {
                warning(Type.WARNING_UNSUPPORTED_JAVA_VERSION, javaVersion, springBootVersion, "java ??");
            } else {
                System.out.println("Validated java version requirement [ Java ?? ]");
            }
        }
>>>>>>> Initial FeatureAuditor update.

        // Verify that Spring is all there.  (This test is probably unnecessary: That
        // we were invoked probably means this test can never fail.)
        String factoryClassName = "org.springframework.boot.context.embedded.EmbeddedServletContainerFactory";
        if ( !foundClass(factoryClassName) ) {
            throw new ApplicationError("Failed to locate spring factory class [ " + factoryClassName + " ]");
        }

        // Cross-check the spring version against the provisioned classes.
        Boolean verified = null;
        for ( SpringFeatureRequirement requirement : featureRequirements ) {
            if ( requirement.accept(springBootVersion) ) {
                if ( !requirement.verify(springBootVersion) ) { // throws ApplicationException
                    verified = Boolean.FALSE;
                } else {
                    verified = Boolean.TRUE;
                }
            }
        }
        if ( verified == null ) {
            System.out.println("Strange: Unknown spring boot version [ " + springBootVersion + " ]")
        } else if ( !verified.booleanValue() ) {
            System.out.println("No spring feature is provisioned!");
        } else {
            System.out.println("The required spring feature is provisioned.")
        }

        // Make sure the servlet and web-socket features are present.
        // TODO: Will this project be transformed?

        String servletClassName =
            ( springIsAtLeast30 ? "jakarta.servlet.Servlet" : "javax.servlet.Servlet");
        String webSocketClassName =
            ( springIsAtLeast30 ? "jakarta.websocket.WebSocketContainer" : " javax.websocket.WebSocketContainer" );
        if ( !foundClass(servletClassName) ) {
            throw new ApplicationError(Type.ERROR_MISSING_SERVLET_FEATURE);
        }
<<<<<<< HEAD

    }

    private void checkSpringBootVersion30() {
        try {
            Class.forName(
                          "io.openliberty.springboot.support.web.server.version30.container.LibertyConfiguration");
        } catch (ClassNotFoundException e) {
            throw new ApplicationError(Type.ERROR_NEED_SPRING_BOOT_VERSION_30);
=======
        if ( !foundClass(webSocketClassName) ) {
            throw new ApplicationError(Type.ERROR_MISSING_WEBSOCKET_FEATURE);
>>>>>>> Initial FeatureAuditor update.
        }
        System.out.println("The necessary Servlet and WebSocket classes are provisioned.");
    }
}
//@formatter:on
