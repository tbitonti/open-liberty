// 1.6, 10/9/07
// IBM Confidential OCO Source Material
// 5724-J08, 5724-I63, 5724-H88, 5724-H89, 5655-N02, 5733-W70 (C) COPYRIGHT International Business Machines Corp. 1997, 2007
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

public class InvalidationListenerInfo {
    public Object m_id;
    public Object m_value;
    public int m_cause;
    public int m_source;
    public String m_cacheName;

    public InvalidationListenerInfo(Object id, Object value, int cause, int source, String cacheName) {
        m_id = id;
        m_value = value;
        m_cause = cause;
        m_source = source;
        m_cacheName = cacheName;
    }

    public String compare(InvalidationListenerInfo info) {
        if (!(m_id.equals(info.m_id)))
            return "Id expected=" + info.m_id + " but received=" + m_id;
        if (m_value != null) {
            if (m_value instanceof byte[]) {
                try {
                    ByteArrayInputStream is = new ByteArrayInputStream((byte[]) m_value);
                    ObjectInputStream objInputStream = new ObjectInputStream(is);
                    m_value = objInputStream.readObject();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        if (info.m_value == null) {
            if (m_value != null) {
                return "Value expected=" + info.m_value + " but received=" + m_value;
            }
        } else {
            if (!(m_value.equals(info.m_value))) {
                return "Value expected=" + info.m_value + " className=" + info.m_value.getClass().getName() +
                       " but received=" + m_value + " className=" + m_value.getClass().getName();
            }
        }
        if (m_cause != info.m_cause)
            return "Cause expected=" + info.m_cause + " but received=" + m_cause;
        if (m_source != info.m_source)
            return "Source expected=" + info.m_source + " but received=" + m_source;
        if (!(m_cacheName.equals(info.m_cacheName))) {
            return "CacheName expected=" + info.m_cacheName + " but received=" + m_cacheName;
        }
        return "";
    }

    @Override
    public String toString() {
        String s = "Id=" + m_id + " value=" + m_value + " Cause=" + m_cause + " source=" + m_source + " cacheName=" + m_cacheName;
        return s;
    }
}
