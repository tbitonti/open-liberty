/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * Copyright IBM Corp. 2011, 2017
 *
 * The source code for this program is not published or otherwise divested 
 * of its trade secrets, irrespective of what has been deposited with the 
 * U.S. Copyright Office.
 */
package com.ibm.ws.artifact.fat;

/**
 * class to assist in building verification for artifact api.
 */
public class Fs {

    private Fs parent;

    private final String name;
    private final String path;
    private final String nameAsEntry;
    private final String pathAsEntry;
    private final String resource;
    private final String physicalPath;
    private final int urlCount;
    private final String[] urls;

    private final boolean isRoot;
    private final boolean isContainer;
    private final boolean hasData;
    private final String data;
    private final long size;
    private final int type;

    private final Fs[] children;

    public final static int ROOT = 0;
    public final static int DIR = 1;
    public final static int FILE = 2;

    private Fs(int type, String name, String path, String nameAsEntry, String pathAsEntry, String resource, String physicalPath, int urlCount, String[] urls, boolean isRoot,
               boolean isContainer,
               boolean hasData, String data, long size, Fs... children) {
        this.type = type;
        this.parent = null;
        this.name = name;
        this.path = path;
        this.nameAsEntry = nameAsEntry;
        this.pathAsEntry = pathAsEntry;
        this.physicalPath = physicalPath;
        this.urlCount = urlCount;
        this.urls = urls;
        this.isRoot = isRoot;
        this.isContainer = isContainer;
        this.hasData = hasData;
        this.data = data;
        this.size = size;
        this.resource = resource;

        this.children = children;
        if (children != null) {
            for (Fs child : children) {
                child.setParent(this);
            }
        }
    }

    /**
     * 
     * @param name name of root (should be /)
     * @param path path of root (should be /)
     * @param nameAsEntry name of root, as entry in its parent container, or null if no parent
     * @param pathAsEntry path of root, as entry in its parent container, or null if no parent
     * @param resourceAsEntry value of getresource of corresponding entry in parent container, or null if no parent
     * @param hasData true if as an entry, this root had data
     * @param data 1st 64 chars of data, not required for .jar entries, only required if hasData is true, null otherwise
     * @param size size of entry in parent container, set to 0 if no parent
     * @param physicalPath path on disk for this container (matches for root container, and root-container-as-entry-in-parent)
     * @param uriCount no of uris on this container, set to -1 if geturi returned null
     * @param uris string array containing uris, use null if -1 uricount
     * @param children use to declare more children..
     * @return
     */
    public static Fs Root(String name, String path, String nameAsEntry, String pathAsEntry, String resourceAsEntry, boolean hasData, String data, long size, String physicalPath,
                          int uriCount,
                          String[] uris,
                          Fs... children) {

        //the test data needs fixing.. but this is quicker for now. 
        if (!("/".equals(name) && "/".equals(path))) {
            name = "/";
            path = "/";
        }

        return new Fs(ROOT, name, path, nameAsEntry, pathAsEntry, null, physicalPath, uriCount, uris, true, true, hasData, data, size, children);
    }

    public static Fs Dir(String name, String path, String resource, String physicalPath, int uriCount, String[] uris, Fs... children) {
        return new Fs(DIR, name, path, null, null, resource, physicalPath, uriCount, uris, false, true, false, null, 0, children);
    }

    public static Fs File(String name, String path, boolean hasData, String data, long size, String resource, String physicalPath) {
        return new Fs(FILE, name, path, null, null, resource, physicalPath, -1, null, false, false, hasData, data, size);
    }

    public Fs getChildByName(String name) {
        if (children != null) {
            for (Fs child : children) {
                String cname;
                if (child.isRoot()) {
                    cname = child.getNameAsEntry();
                } else {
                    cname = child.getName();
                }
                if (cname.equals(name))
                    return child;
            }
        }
        return null;
    }

    public String getDebugPath() {
        String path = getPath();
        Fs node = this;
        while (node != null) {
            while (!node.isRoot()) {
                node = node.getParent();
            }
            if (node.getPathAsEntry() != null) {
                path = node.getPathAsEntry() + "#" + path;
            } else {
                path = "#" + path;
            }
            node = node.getParent();
        }
        return path;
    }

    public Fs[] getChildren() {
        return this.children;
    }

    public String getName() {
        return this.name;
    }

    /**
     * @return the parent
     */
    public Fs getParent() {
        return parent;
    }

    private void setParent(Fs parent) {
        this.parent = parent;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @return the isRoot
     */
    public boolean isRoot() {
        return isRoot;
    }

    /**
     * @return the isContainer
     */
    public boolean isContainer() {
        return isContainer;
    }

    /**
     * @return the hasData
     */
    public boolean hasData() {
        return hasData;
    }

    /**
     * @return the data
     */
    public String getData() {
        return data;
    }

    /**
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * @return the nameAsEntry
     */
    public String getNameAsEntry() {
        return nameAsEntry;
    }

    /**
     * @return the pathAsEntry
     */
    public String getPathAsEntry() {
        return pathAsEntry;
    }

    /**
     * @return the physicalPath
     */
    public String getPhysicalPath() {
        return physicalPath;
    }

    /**
     * @return the uriCount
     */
    public int getUrlCount() {
        return urlCount;
    }

    /**
     * @return the uris
     */
    public String[] getURLs() {
        return urls;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @return the resource
     */
    public String getResource() {
        return resource;
    }
}
