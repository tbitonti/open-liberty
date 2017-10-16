/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WLP Copyright IBM Corp. 2015, 2017
 *
 * The source code for this program is not published or otherwise divested 
 * of its trade secrets, irrespective of what has been deposited with the 
 * U.S. Copyright Office.
 */
package com.ibm.ws.artifact.fat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.ibm.wsspi.artifact.ArtifactContainer;
import com.ibm.wsspi.artifact.ArtifactEntry;
import com.ibm.wsspi.artifact.ArtifactNotifier;
import com.ibm.wsspi.artifact.factory.contributor.ArtifactContainerFactoryContributor;

/**
 * A Really really simple example of a custom container implementation.
 * <p>
 * Creates a container from a property file.. each key becomes an entry, each keys value becoming the entries data.
 * <p>
 * This is made MUCH simpler because, it does not implement <br>
 * <ul>
 * <li>Notifiers</li>
 * <li>Nested entries</li>
 * <li>Nested containers</li>
 * <li>Opening properties files as nested containers</li>
 * <li>Any physicalpath/url references</li>
 * <li>fastmode</li>
 * </ul>
 * Other than that, it'll open .custom files as containers. Data in .custom to be as .properties.
 * <p>
 * <pre>
 * Service-Component: \
 * com.ibm.ws.test.custom.container;\
 * implementation:=com.ibm.ws.artifact.fat.CustomContainerHelper;\
 * provide:=com.ibm.wsspi.artifact.factory.contributor.ArtifactContainerFactoryContributor;\
 * configuration-policy:=ignore;\
 * properties:="service.vendor=IBM,category=CUSTOM,handlesType=java.io.File,handlesEntries=.custom"
 * </pre>
 */
public class CustomContainerHelper implements ArtifactContainerFactoryContributor {

    private boolean hasCorrectExtension(String name) {
        return name.matches("(?i:(.*)\\.(CUSTOM))");
    }

    private boolean isValid(File f) {
        boolean valid = false;
        if (hasCorrectExtension(f.getName())) {
            // Opening the file to ensure it really is a valid data..
            InputStream is = null;
            try {
                is = new FileInputStream(f);
                Properties p = new Properties();
                p.load(is);
                valid = true;
            } catch (Exception e) {
                valid = false;
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e) {
                        //ignore.
                    }
                }
            }
        }
        return valid;
    }

    private static boolean isFile(final File target) {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run() {
                return target.isFile();
            }

        });
    }

    @Override
    public ArtifactContainer createContainer(File cacheDir, Object o) {
        ArtifactContainer pfc = null;
        if (o instanceof File && isFile(((File) o))) {
            File f = (File) o;
            if (isValid(f)) {
                pfc = new PropertyFileContainer(f);
            }
        }
        return pfc;
    }

    @Override
    public ArtifactContainer createContainer(File cacheDir, ArtifactContainer parent, ArtifactEntry e, Object o) {
        //we wont handle nested containers.
        return null;
    }

    private static final class PropertyFileContainer implements ArtifactContainer {
        Properties pf = null;
        Map<String, ArtifactEntry> entries = new HashMap<String, ArtifactEntry>();
        static PropertyFileNotifier notifier = new PropertyFileNotifier();

        PropertyFileContainer(File propertyFile) {
            // Opening the file to ensure it really is a valid data..
            InputStream is = null;
            try {
                is = new FileInputStream(propertyFile);
                pf = new Properties();
                pf.load(is);
            } catch (Exception e) {
                //bad.. since we already checked this.. 
                //but this is only a test..                 
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e) {
                        //ignore.
                    }
                }
            }

            for (Entry<Object, Object> e : pf.entrySet()) {
                PropertyFileEntry pfe = new PropertyFileEntry(this, e.getKey().toString(), e.getValue().toString());
                entries.put("/" + e.getKey().toString(), pfe);
            }
        }

        private static class PropertyFileEntry implements ArtifactEntry {
            PropertyFileContainer parent;
            String name;
            String content;

            PropertyFileEntry(PropertyFileContainer parent, String name, String content) {
                this.parent = parent;
                this.name = name;
                this.content = content;
            }

            @Override
            public ArtifactContainer getEnclosingContainer() {
                return parent;
            }

            @Override
            public String getPath() {
                return "/" + name;
            }

            @Override
            public ArtifactContainer getRoot() {
                return parent;
            }

            @Override
            public String getPhysicalPath() {
                return null;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public ArtifactContainer convertToContainer() {
                return null;
            }

            @Override
            public ArtifactContainer convertToContainer(boolean localOnly) {
                return null;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(content.getBytes());
            }

            @Override
            public long getSize() {
                return content.getBytes().length;
            }

            @Override
            public long getLastModified() {
                return 1337;
            }

            @Override
            public URL getResource() {
                return null;
            }

        }

        private static class PropertyFileNotifier implements ArtifactNotifier {
            /** {@inheritDoc} */
            @Override
            public boolean registerForNotifications(ArtifactNotification targets, ArtifactListener callbackObject) throws IllegalArgumentException {
                return false;
            }

            /** {@inheritDoc} */
            @Override
            public boolean removeListener(ArtifactListener listenerToRemove) {
                return false;
            }

            /** {@inheritDoc} */
            @Override
            public boolean setNotificationOptions(long interval, boolean useMBean) {
                return false;
            }
        }

        @Override
        public Iterator<ArtifactEntry> iterator() {
            return entries.values().iterator();
        }

        @Override
        public ArtifactContainer getEnclosingContainer() {
            return null;
        }

        @Override
        public String getPath() {
            return "/";
        }

        @Override
        public ArtifactContainer getRoot() {
            return this;
        }

        @Override
        public String getPhysicalPath() {
            return null;
        }

        @Override
        public String getName() {
            return "/";
        }

        @Override
        public void useFastMode() {}

        @Override
        public void stopUsingFastMode() {}

        @Override
        public boolean isRoot() {
            return true;
        }

        @Override
        public ArtifactEntry getEntry(String pathAndName) {
            if (!pathAndName.startsWith("/")) {
                pathAndName = "/" + pathAndName;
            }
            return entries.get(pathAndName);
        }

        @Override
        public Collection<URL> getURLs() {
            return Collections.<URL> emptySet();
        }

        @Override
        public ArtifactEntry getEntryInEnclosingContainer() {
            return null;
        }

        @Override
        public ArtifactNotifier getArtifactNotifier() {
            return notifier;
        }
    }
}
