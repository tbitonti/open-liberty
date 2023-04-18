// 1.1, 10/24/06
// IBM Confidential OCO Source Material
// 5724-J08, 5724-I63, 5724-H88, 5655-N01, 5733-W61 (C) COPYRIGHT International Business Machines Corp. 1997, 2006
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache;

import java.io.*;
import com.ibm.websphere.cache.DistributedNioMapObject;

public class MyNioMapLongObject implements DistributedNioMapObject, Externalizable {

    public long[] value;
    public int count = 0;

    public MyNioMapLongObject() {
        this.value = null;
    }

    public MyNioMapLongObject(long[] value)  {
        this.value = value;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(value);
    }

    public void readExternal(ObjectInput in) throws IOException {
        try {
            value = (long[]) in.readObject();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof MyNioMapObject) {
            MyNioMapLongObject object = (MyNioMapLongObject)obj;

            if (this.value != null && value[0] == object.value[0] && value[1] == object.value[1]) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        if (value != null) {
            return (new Long(value[0] + value[1])).hashCode();
        }
        return 0;
    }

    /**
     * Release the proxy cache object(ByteBuffers/MetaData) to the Proxy's Buffer Management.
     */
    public void release() {
        //Thread.dumpStack();
        count++;
        System.out.println("***** Release ByteBuffers/MetaData to Buffer Management value=" + value[0] + " " + value[1] + " count=" + count);
    }

    public void clear() {
        count = 0;
    }

    public String toString() {
        return this.value[0] + " " + this.value[1];
    }

    public long getCacheValueSize() {
        return -1;
    }

}

