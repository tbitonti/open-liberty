// 1.3, 6/23/04
// IBM Confidential OCO Source Material
// 5630-A36 (C) COPYRIGHT International Business Machines Corp. 1997, 2004
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.

package com.ibm.ws.cache.servlet;



public class MyNonSerializableObjectKey {

    String name;

    MyNonSerializableObjectKey(String key){
        name = key;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof MyNonSerializableObjectKey) {
            MyNonSerializableObjectKey object = (MyNonSerializableObjectKey)obj;

            if ((this.name == object.name)
                || ((this.name != null) && (this.name.equals(object.name)))) {
                return true;
            }
        }
        return false;
    }


    public int hashCode() {
        if (name != null) {
            return name.hashCode();
        }
        return 0;
    }
}


