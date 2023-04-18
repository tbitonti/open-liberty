package com.ibm.ws.cache.command;

import java.io.Serializable;

public class CommandMap {
static final long serialVersionUID = 0x50002002;

public Serializable get(String key) {
    ObjectWrapperCommand owc = new ObjectWrapperCommand(key);
    try {
       owc.executeFromCache();
       return owc.getValue();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
}


public Serializable get(String key,int sharingPolicy) {
    ObjectWrapperCommand owc = new ObjectWrapperCommand(key);
    owc.setSharingPolicy(sharingPolicy);
    try {
       owc.executeFromCache();
       return owc.getValue();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
}

public void put(String key,Serializable value) {
    ObjectWrapperCommand owc = new ObjectWrapperCommand(key,value);
    owc.updateCache();
}


public void put(String key,Serializable value,int sharingPolicy) {
    ObjectWrapperCommand owc = new ObjectWrapperCommand(key,value);
    owc.setSharingPolicy(sharingPolicy);
    owc.updateCache();
}


public void putTTL(String key,Serializable value, int timeToLive) {
    ObjectWrapperCommand owc = new ObjectWrapperCommand(key,value);
    owc.setTTL(timeToLive);
    owc.updateCache();
}

public void put(String key,Serializable value, String[] groupKeys, int timeToLive) {
    ObjectWrapperCommand owc = new ObjectWrapperCommand(key,value);
    owc.setTTL(timeToLive);
    owc.setGroupKeys(groupKeys);
    owc.updateCache();
}

public void put(String key,Serializable value, String[] groupKeys, int timeToLive,int priority) {
    ObjectWrapperCommand owc = new ObjectWrapperCommand(key,value);
    owc.setTTL(timeToLive);
    owc.setGroupKeys(groupKeys);
    owc.setPriority(priority);
    owc.updateCache();
}

public void invalidate(String key) {
   com.ibm.websphere.servlet.cache.DynamicCacheAccessor.getCache().invalidateById(key,true);
}

public void clear() {
   com.ibm.websphere.servlet.cache.DynamicCacheAccessor.getCache().invalidateByTemplate(ObjectWrapperCommand.class.getName(),true);
}

}
