// 1.1, 2/5/08
// IBM Confidential OCO Source Material
// 5724-J08, 5724-I63, 5724-H88, 5724-H89, 5655-N02, 5733-W70 (C) COPYRIGHT International Business Machines Corp. 1997, 2007
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache;

import java.io.*;
import com.ibm.websphere.cache.Sizeable;;

public class MyObjectSizer implements Sizeable, Externalizable {

	private static final long serialVersionUID = -8721488840155173252L;
	
    public String name;
    public byte[] value;
    public int count = 0;

    public MyObjectSizer() {
        this.name = "dummy";
        this.value = null;
    }

    public MyObjectSizer(String name, int length)  { 
        this.name = name;
        this.value = new byte[length];
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(name);
        if (this.value == null) {
        	out.writeInt(-1);
        } else {
        	out.writeInt(this.value.length);
        	out.write(this.value);
        }
    }

    public void readExternal(ObjectInput in) throws IOException {
        try {
            name = (String) in.readObject();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
		int length = in.readInt();	//SKS-O
		if (length > 0) {
			this.value = new byte[length];
			in.readFully(this.value);
		} else {
			this.value = null;
		}						//SKS-O
}

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof MyObjectSizer) {
            MyObjectSizer object = (MyObjectSizer)obj;

            if (this.name == object.name) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        if (name != null) {
            return value.hashCode();
        }
        return 0;
    }

    public long getObjectSize() {
    	return this.name.length() + this.value.length; 
    }
    

    public String toString() {
        return this.name;
    }

}

