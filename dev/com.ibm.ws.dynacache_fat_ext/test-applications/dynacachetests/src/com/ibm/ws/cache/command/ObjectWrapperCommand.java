package com.ibm.ws.cache.command;

import java.io.Serializable;

import com.ibm.websphere.cache.EntryInfo;
import com.ibm.websphere.command.CacheableCommandImpl;
import com.ibm.websphere.command.TargetableCommand;

public class ObjectWrapperCommand extends CacheableCommandImpl {
    private String key;
    private Serializable value;
    private int ttl = -1; // infinite
    private String groupKeys[] = null;
    private int priority = 0;
    private int sharingPolicy = EntryInfo.NOT_SHARED;

    public ObjectWrapperCommand() {}

    public ObjectWrapperCommand(String key) {
        this.key = key;
    }

    public ObjectWrapperCommand(String key, Serializable value) {
        this.key = key;
        this.value = value;
    }

    public void setSharingPolicy(int sharingPolicy) {
        this.sharingPolicy = sharingPolicy;
    }

    @Override
    protected void prepareMetadata() {
        EntryInfo entryInfo = super.getEntryInfo();
        entryInfo.setId("ObjectWrapperCommand" + ":" + key);
        entryInfo.setSharingPolicy(sharingPolicy);
        if (groupKeys != null) {
            for (int i = 0; i < groupKeys.length; i++)
                entryInfo.addDataId(groupKeys[i]);
        }
        entryInfo.setTimeLimit(ttl);
        if (priority != 0)
            entryInfo.setPriority(priority);
    }

    // called to validate that command input parameters have been set
    @Override
    public boolean isReadyToCallExecute() {
        return (key != null);
    }

    // called by a cache-hit to copy output properties to this object
    @Override
    public void setOutputProperties(TargetableCommand fromCommand) {
        ObjectWrapperCommand f = (ObjectWrapperCommand) fromCommand;
        this.value = f.value;
    }

    @Override
    public void performExecute() throws Exception {}

    // methods to modify the entrie's metadata

    public void setTTL(int timeToLive) {
        ttl = timeToLive;
    }

    public int getTTL() {
        return ttl;
    }

    public void setGroupKeys(String groupKeys[]) {
        this.groupKeys = groupKeys;
    }

    public String[] getGroupKeys() {
        return groupKeys;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    //input parameters for the command
    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    //output parameters for the command
    public Serializable getValue() {
        return value;
    }

}
