// 1.1.1.2, 3/25/04
// IBM Confidential OCO Source Material
// 5630-A36 (C) COPYRIGHT International Business Machines Corp. 1997, 2003
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.servlet;

import java.io.*;

    //SKS-O
    public class MySerializableObjectKey implements Externalizable {

        public String name;

        public MySerializableObjectKey() {
            name = null;
        }
    public MySerializableObjectKey(String key)  {  // CPF_MERGE  - Sajan aaded this CTOR
		name = key;
	}
    public MySerializableObjectKey(String key, boolean b){ // CPF_MERGE - what is this?
            name = key;
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(name);
        }

        public void readExternal(ObjectInput in) throws IOException {
            try {
                name = (String) in.readObject();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public boolean equals(Object obj) {
            if (this == obj) {
	        return true;
            }
	    if (obj instanceof MySerializableObjectKey) {
                MySerializableObjectKey object = (MySerializableObjectKey)obj;

			if ((this.name == object.name)
				|| ((this.name != null) && (this.name.equals(object.name)))) {
                    return true;
                }
			//System.out.println(
			//	"---------------->>>>>>>>> didn't match:: name:"
			//		+ name
			//		+ " obj.name:"
			//		+ object.name);
		}
	    return false;
        }

        public int hashCode() {
            if (name != null) {
                return name.hashCode();
            }
	    return 0;
        }

        public String toString() {
            return name;
        }

    }
