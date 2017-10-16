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

import java.util.ArrayList;

/**
 *
 */
public class FsUtils {
    /**
     * add all the stuff in overlay, ontop of base, to make a new Fs to be returned.
     * 
     * @param base
     * @param overlay
     * @return
     */
    public static Fs merge(Fs node, Fs overlay) {
        Fs newNode = null;
        if (node.getType() == Fs.ROOT) {
            ArrayList<Fs> children = null;
            if (node.getChildren() != null) {
                children = new ArrayList<Fs>();
                //add the base set children, overriding where needed.
                for (Fs child : node.getChildren()) {
                    String name = child.isRoot() ? child.getNameAsEntry() : child.getName();
                    Fs nodeFromOverlay = overlay.getChildByName(name);
                    if (nodeFromOverlay == null) {
                        //overlay didnt know about this node, so clone the base one.
                        children.add(cloneNode(child));
                    } else {
                        //overlay did know about this node, so merge downward.
                        children.add(merge(child, nodeFromOverlay));
                    }
                }
            }
            if (overlay.getChildren() != null) {
                if (children == null) {
                    children = new ArrayList<Fs>();
                }
                //add in stuff that was only in overlay.
                for (Fs child : overlay.getChildren()) {
                    String name = child.isRoot() ? child.getNameAsEntry() : child.getName();
                    Fs nodeFromBase = node.getChildByName(name);
                    if (nodeFromBase == null) {
                        children.add(cloneNode(child));
                    }
                }
            }
            if (children != null) {
                Fs[] fs = children.toArray(new Fs[] {});
                newNode = Fs.Root(node.getName(), node.getPath(), node.getNameAsEntry(), node.getPathAsEntry(), node.getResource(), node.hasData(), node.getData(), node.getSize(),
                                  node.getPhysicalPath(),
                                  node.getUrlCount(), node.getURLs(),
                                  fs);
            } else {
                newNode = Fs.Root(node.getName(), node.getPath(), node.getNameAsEntry(), node.getPathAsEntry(), node.getResource(), node.hasData(), node.getData(), node.getSize(),
                                  node.getPhysicalPath(),
                                  node.getUrlCount(), node.getURLs()
                                  );
            }
        } else if (node.getType() == Fs.DIR) {
            ArrayList<Fs> children = null;
            if (node.getChildren() != null) {
                children = new ArrayList<Fs>();
                //add the base set children, overriding where needed.
                for (Fs child : node.getChildren()) {
                    String name = child.isRoot() ? child.getNameAsEntry() : child.getName();
                    Fs nodeFromOverlay = overlay.getChildByName(name);
                    if (nodeFromOverlay == null) {
                        //overlay didnt know about this node, so clone the base one.
                        children.add(cloneNode(child));
                    } else {
                        //overlay did know about this node, so merge downward.
                        children.add(merge(child, nodeFromOverlay));
                    }
                }
            }
            if (overlay.getChildren() != null) {
                if (children == null) {
                    children = new ArrayList<Fs>();
                }
                //add in stuff that was only in overlay.
                for (Fs child : overlay.getChildren()) {
                    String name = child.isRoot() ? child.getNameAsEntry() : child.getName();
                    Fs nodeFromBase = node.getChildByName(name);
                    if (nodeFromBase == null) {
                        children.add(cloneNode(child));
                    }
                }
            }
            if (children != null) {
                Fs[] fs = children.toArray(new Fs[] {});
                newNode = Fs.Dir(node.getName(), node.getPath(), node.getResource(), node.getPhysicalPath(), node.getUrlCount(), node.getURLs(), fs);
            } else {
                newNode = Fs.Dir(node.getName(), node.getPath(), node.getResource(), node.getPhysicalPath(), node.getUrlCount(), node.getURLs());
            }
        } else if (node.getType() == Fs.FILE) {
            newNode = Fs.File(node.getName(), node.getPath(), node.hasData(), node.getData(), node.getSize(), node.getResource(), node.getPhysicalPath());
        }
        return newNode;
    }

    public static Fs cloneNode(Fs node) {
        Fs newNode = null;
        if (node.getType() == Fs.ROOT) {
            if (node.getChildren() != null) {
                ArrayList<Fs> children = new ArrayList<Fs>();
                for (Fs child : node.getChildren()) {
                    children.add(cloneNode(child));
                }
                Fs[] fs = children.toArray(new Fs[] {});
                newNode = Fs.Root(node.getName(), node.getPath(), node.getNameAsEntry(), node.getPathAsEntry(), node.getResource(), node.hasData(), node.getData(), node.getSize(),
                                  node.getPhysicalPath(), node.getUrlCount(), node.getURLs(),
                                  fs);
            } else {
                newNode = Fs.Root(node.getName(), node.getPath(), node.getNameAsEntry(), node.getPathAsEntry(), node.getResource(), node.hasData(), node.getData(), node.getSize(),
                                  node.getPhysicalPath(), node.getUrlCount(), node.getURLs()
                                  );
            }
        } else if (node.getType() == Fs.DIR) {
            if (node.getChildren() != null) {
                ArrayList<Fs> children = new ArrayList<Fs>();
                for (Fs child : node.getChildren()) {
                    children.add(cloneNode(child));
                }
                Fs[] fs = children.toArray(new Fs[] {});
                newNode = Fs.Dir(node.getName(), node.getPath(), node.getResource(), node.getPhysicalPath(), node.getUrlCount(), node.getURLs(), fs);
            } else {
                newNode = Fs.Dir(node.getName(), node.getPath(), node.getResource(), node.getPhysicalPath(), node.getUrlCount(), node.getURLs());
            }
        } else if (node.getType() == Fs.FILE) {
            newNode = Fs.File(node.getName(), node.getPath(), node.hasData(), node.getData(), node.getSize(), node.getResource(), node.getPhysicalPath());
        }
        return newNode;
    }
}
