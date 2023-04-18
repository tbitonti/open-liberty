// 1.2, 10/24/06
// IBM Confidential OCO Source Material
// 5724-J08, 5724-I63, 5724-H88, 5655-N01, 5733-W61 (C) COPYRIGHT International Business Machines Corp. 1997, 2006
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
package com.ibm.ws.cache.htod;

import com.ibm.websphere.cache.InvalidationEvent;
import com.ibm.websphere.cache.InvalidationListener;
import com.ibm.websphere.cache.ChangeEvent;
import com.ibm.websphere.cache.ChangeListener;

public class MyEventListener2Impl implements InvalidationListener, ChangeListener {
    private String name;
    private int win;
    public int invalidationLocal               = 0;
    public int invalidationLocalExplicit       = 0;
    public int invalidationLocalLRU            = 0;
    public int invalidationLocalTimeout        = 0;
    public int invalidationLocalDiskTimeout    = 0;
    public int invalidationLocalClearAll       = 0;
    public int invalidationLocalDiskGC         = 0;
    public int invalidationLocalDiskOverflow   = 0;
    public int invalidationRemote              = 0;
    public int invalidationRemoteExplicit      = 0;
    public int invalidationRemoteLRU           = 0;
    public int invalidationRemoteTimeout       = 0;
    public int invalidationRemoteDiskTimeout   = 0;
    public int invalidationRemoteClearAll      = 0;
    public int invalidationRemoteDiskGC        = 0;
    public int invalidationRemoteDiskOverflow  = 0;

    public Object invalidationLocalExplicitValue       = null;
    public Object invalidationLocalLRUValue            = null;
    public Object invalidationLocalTimeoutValue        = null;
    public Object invalidationLocalDiskTimeoutValue    = null;
    public Object invalidationLocalDiskGCValue         = null;
    public Object invalidationLocalDiskOverflowValue   = null;
    public Object invalidationRemoteExplicitValue      = null;
    public Object invalidationRemoteLRUValue           = null;
    public Object invalidationRemoteTimeoutValue       = null;
    public Object invalidationRemoteDiskTimeoutValue   = null;
    public Object invalidationRemoteDiskGCValue        = null;
    public Object invalidationRemoteDiskOverflowValue  = null;


    public MyEventListener2Impl(String name, int win) {
        this.name = name;
        this.win = win;
    }

    public void fireEvent(InvalidationEvent ie) {

        if (ie.getSourceOfInvalidation() == InvalidationEvent.LOCAL) {
            invalidationLocal++;
            switch (ie.getCauseOfInvalidation()) {
                case InvalidationEvent.EXPLICIT:
                if (invalidationLocalExplicit == 0) {
                    invalidationLocalExplicitValue = ie.getValue();
                } else {
                    if (invalidationLocalExplicitValue == null) {
                        if (ie.getValue() != null) {
                            System.out.println("*** Error: invalidatationLocalExplicitValue NOT Null"); 
                        }
                    } else {
                        if (ie.getValue() == null) {
                            System.out.println("*** Error: invalidatationLocalExplicitValue is Null"); 
                        }
                    }
                }
                invalidationLocalExplicit++;
                break;
            case InvalidationEvent.LRU:
                if (invalidationLocalLRU == 0) {
                    invalidationLocalLRUValue = ie.getValue();
                } else {
                    if (invalidationLocalLRUValue == null) {
                        if (ie.getValue() != null) {
                            System.out.println("*** Error: invalidationLocalLRUValue NOT Null"); 
                        }
                    } else {
                        if (ie.getValue() == null) {
                            System.out.println("*** Error: invalidationLocalLRUValue is Null"); 
                        }
                    }
                }
                invalidationLocalLRU++;
                break;
            case InvalidationEvent.TIMEOUT:
                if (invalidationLocalTimeout == 0) {
                    invalidationLocalTimeoutValue = ie.getValue();
                } else {
                    if (invalidationLocalTimeoutValue == null) {
                        if (ie.getValue() != null) {
                            System.out.println("*** Error: invalidationLocalTimeoutValue NOT Null"); 
                        }
                    } else {
                        if (ie.getValue() == null) {
                            System.out.println("*** Error: invalidationLocalTimeoutValue is Null"); 
                        }
                    }
                }
                invalidationLocalTimeout++;
                break;
            case InvalidationEvent.DISK_TIMEOUT:
                if (invalidationLocalDiskTimeout == 0) {
                    invalidationLocalDiskTimeoutValue = ie.getValue();
                } else {
                    if (invalidationLocalDiskTimeoutValue == null) {
                        if (ie.getValue() != null) {
                            System.out.println("*** Error: invalidationLocalDiskTimeoutValue NOT Null"); 
                        }
                    } else {
                        if (ie.getValue() == null) {
                            System.out.println("*** Error: invalidationLocalDiskTimeoutValue is Null"); 
                        }
                    }
                }
                invalidationLocalDiskTimeout++;
                break;
                case InvalidationEvent.CLEAR_ALL:
                if (!ie.getId().equals("*")) {
                    System.out.println("*** Error: id is not * for CLEAR_ALL"); 
                }
                if (ie.getValue() != null) {
                    System.out.println("*** Error: value is not null"); 
                }
                invalidationLocalClearAll++;
                break;
                case InvalidationEvent.DISK_GARBAGE_COLLECTOR:
                    if (invalidationLocalDiskGC == 0) {
                        invalidationLocalDiskGCValue = ie.getValue();
                    } else {
                        if (invalidationLocalDiskGCValue == null) {
                            if (ie.getValue() != null) {
                                System.out.println("*** Error: invalidationLocalDiskGCValue NOT Null"); 
                            }
                        } else {
                            if (ie.getValue() == null) {
                                System.out.println("*** Error: invalidationLocalDiskGCValue is Null"); 
                            }
                        }
                    }
                    invalidationLocalDiskGC++;
                    break;
                case InvalidationEvent.DISK_OVERFLOW:
                    if (invalidationLocalDiskOverflow == 0) {
                        invalidationLocalDiskOverflowValue = ie.getValue();
                    } else {
                        if (invalidationLocalDiskOverflowValue == null) {
                            if (ie.getValue() != null) {
                                System.out.println("*** Error: invalidationLocalDiskOverflowValue NOT Null"); 
                            }
                        } else {
                            if (ie.getValue() == null) {
                                System.out.println("*** Error: invalidationLocalDiskOverflowValue is Null"); 
                            }
                        }
                    }
                    invalidationLocalDiskOverflow++;
                    break;
            }
        } else {
            invalidationRemote++;
            switch (ie.getCauseOfInvalidation()) {
            case InvalidationEvent.EXPLICIT:
                if (invalidationRemoteExplicit == 0) {
                    invalidationRemoteExplicitValue = ie.getValue();
                } else {
                    if (invalidationRemoteExplicitValue == null) {
                        if (ie.getValue() != null) {
                            System.out.println("*** Error: invalidatationRemoteExplicitValue NOT Null"); 
                        }
                    } else {
                        if (ie.getValue() == null) {
                            System.out.println("*** Error: invalidatationRemoteExplicitValue is Null"); 
                        }
                    }
                }
                invalidationRemoteExplicit++;
                break;
            case InvalidationEvent.LRU:
                if (invalidationRemoteLRU == 0) {
                    invalidationRemoteLRUValue = ie.getValue();
                } else {
                    if (invalidationLocalLRUValue == null) {
                        if (ie.getValue() != null) {
                            System.out.println("*** Error: invalidationRemoteLRUValue NOT Null"); 
                        }
                    } else {
                        if (ie.getValue() == null) {
                            System.out.println("*** Error: invalidationRemoteLRUValue is Null"); 
                        }
                    }
                }
                invalidationRemoteLRU++;
                break;
            case InvalidationEvent.TIMEOUT:
                if (invalidationRemoteTimeout == 0) {
                    invalidationRemoteTimeoutValue = ie.getValue();
                } else {
                    if (invalidationRemoteTimeoutValue == null) {
                        if (ie.getValue() != null) {
                            System.out.println("*** Error: invalidationRemoteTimeoutValue NOT Null"); 
                        }
                    } else {
                        if (ie.getValue() == null) {
                            System.out.println("*** Error: invalidationRemoteTimeoutValue is Null"); 
                        }
                    }
                }
                invalidationRemoteTimeout++;
                break;
            case InvalidationEvent.DISK_TIMEOUT:
                if (invalidationRemoteDiskTimeout == 0) {
                    invalidationRemoteDiskTimeoutValue = ie.getValue();
                } else {
                    if (invalidationRemoteDiskTimeoutValue == null) {
                        if (ie.getValue() != null) {
                            System.out.println("*** Error: invalidationRemoteDiskTimeoutValue NOT Null"); 
                        }
                    } else {
                        if (ie.getValue() == null) {
                            System.out.println("*** Error: invalidationRemoteDiskTimeoutValue is Null"); 
                        }
                    }
                }
                invalidationRemoteDiskTimeout++;
                break;
            case InvalidationEvent.CLEAR_ALL:
                if (!ie.getId().equals("*")) {
                    System.out.println("*** Error: id is not * for CLEAR_ALL"); 
                }
                if (ie.getValue() != null) {
                    System.out.println("*** Error: value is not null"); 
                }
                invalidationRemoteClearAll++;
                break;
                case InvalidationEvent.DISK_GARBAGE_COLLECTOR:
                    if (invalidationRemoteDiskGC == 0) {
                        invalidationRemoteDiskGCValue = ie.getValue();
                    } else {
                        if (invalidationRemoteDiskGCValue == null) {
                            if (ie.getValue() != null) {
                                System.out.println("*** Error: invalidationRemoteDiskGCValue NOT Null"); 
                            }
                        } else {
                            if (ie.getValue() == null) {
                                System.out.println("*** Error: invalidationRemoteDiskGCValue is Null"); 
                            }
                        }
                    }
                    invalidationRemoteDiskGC++;
                    break;
                case InvalidationEvent.DISK_OVERFLOW:
                    if (invalidationRemoteDiskOverflow == 0) {
                        invalidationRemoteDiskOverflowValue = ie.getValue();
                    } else {
                        if (invalidationRemoteDiskOverflowValue == null) {
                            if (ie.getValue() != null) {
                                System.out.println("*** Error: invalidationRemoteDiskOverflowValue NOT Null"); 
                            }
                        } else {
                            if (ie.getValue() == null) {
                                System.out.println("*** Error: invalidationRemoteDiskOverflowValue is Null"); 
                            }
                        }
                    }
                    invalidationRemoteDiskOverflow++;
                    break;
            }
        }
        if (((invalidationLocal + invalidationRemote) % win) == 0) {
            System.out.println("*** Fired invalidation event:  id=" + ie.getId() + " value=" + ie.getValue() + " Cause=" + ie.getCauseOfInvalidation() + " source=" + ie.getSourceOfInvalidation());
            if (invalidationLocal > 0) {
                System.out.println("*** Fired invalidation event: local=" + invalidationLocal + " explicit=" + invalidationLocalExplicit + " LRU=" + invalidationLocalLRU + " timeout=" + invalidationLocalTimeout + " diskTimeout=" + invalidationLocalDiskTimeout + " clearAll=" + invalidationLocalClearAll + " DiskGC=" + invalidationLocalDiskGC + " DiskOverflow=" + invalidationLocalDiskOverflow);
            }
            if (invalidationRemote > 0) {
                System.out.println("*** Fired invalidation event: remote=" + invalidationRemote + " explicit=" + invalidationRemoteExplicit + " LRU=" + invalidationRemoteLRU + " timeout=" + invalidationRemoteTimeout + " diskTimeout=" + invalidationRemoteDiskTimeout + " clearAll=" + invalidationRemoteClearAll + " DiskGC=" + invalidationRemoteDiskGC + " DiskOverflow=" + invalidationRemoteDiskOverflow);
            }
        }

    }

    public void cacheEntryChanged(ChangeEvent ce) {
        System.out.println("*** Fired change event: listener=" + name + " id=" + ce.getId() + " value=" + ce.getValue() + " Cause=" + ce.getCauseOfChange() + " source=" + ce.getSourceOfChange());
    }

    public void reset() {
        invalidationLocal              = 0;
        invalidationRemote             = 0;
        invalidationLocalExplicit      = 0;
        invalidationLocalLRU           = 0;
        invalidationLocalTimeout       = 0;
        invalidationLocalDiskTimeout   = 0;
        invalidationLocalClearAll      = 0;
        invalidationLocalDiskGC        = 0;
        invalidationRemoteExplicit     = 0;
        invalidationRemoteLRU          = 0;
        invalidationRemoteTimeout      = 0;
        invalidationRemoteDiskTimeout  = 0;
        invalidationRemoteClearAll     = 0;
        invalidationRemoteDiskGC       = 0;
    }

    public String getInvalidationLocalInfo() {
        StringBuffer answer = new StringBuffer();
        answer.append("*** Fired invalidation event: local=");
        answer.append(invalidationLocal);
        answer.append(" explicit=");
        answer.append(invalidationLocalExplicit);
        answer.append(" LRU=");
        answer.append(invalidationLocalLRU);
        answer.append(" timeout=");
        answer.append(invalidationLocalTimeout);
        answer.append(" diskTimeout=");
        answer.append(invalidationLocalDiskTimeout);
        answer.append(" clearAll=");
        answer.append(invalidationLocalClearAll);
        answer.append(" diskGC=");
        answer.append(invalidationLocalDiskGC);
        answer.append(" diskOverflow=");
        answer.append(invalidationLocalDiskOverflow);
        return answer.toString();
    }

    public String getInvalidationRemoteInfo() {
        StringBuffer answer = new StringBuffer();
        answer.append("*** Fired invalidation event: remote=");
        answer.append(invalidationRemote);
        answer.append(" explicit=");
        answer.append(invalidationRemoteExplicit);
        answer.append(" LRU=");
        answer.append(invalidationRemoteLRU);
        answer.append(" timeout=");
        answer.append(invalidationRemoteTimeout);
        answer.append(" diskTimeout=");
        answer.append(invalidationRemoteDiskTimeout);
        answer.append(" clearAll=");
        answer.append(invalidationRemoteClearAll);
        answer.append(" diskGC=");
        answer.append(invalidationRemoteDiskGC);
        answer.append(" diskOverflow=");
        answer.append(invalidationLocalDiskOverflow);
        return answer.toString();
    }
}
