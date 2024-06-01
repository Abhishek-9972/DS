package DS.DesignPattern.creational.singleton;

import java.io.ObjectStreamException;
import java.io.Serializable;

public class Singleton implements Cloneable, Serializable {
    private static volatile Singleton soleInstance = null;

    //To prevent breaking from Reflection
    private Singleton() {
        if (soleInstance != null) {
            throw new RuntimeException("Cannot create Please use getInstance");
        }
    }

    public static Singleton getInstance() {
        //Double Checked Locking : To prevent from multithreading
        if (soleInstance == null) {
            synchronized (Singleton.class) {
                if (soleInstance == null) {
                    soleInstance = new Singleton();
                }
            }
        }
        return soleInstance;
    }

    //To prevent breaking from Serialization
    public Object readResolve() throws ObjectStreamException {
        return soleInstance;
    }

    //To prevent breaking from clone
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}

