package dragon.service.sec;

import javax.persistence.Embeddable;

/**
 * a bit set class holding all permissions
 * 
 */
@Embeddable
public class PermissionBits implements java.io.Serializable, Cloneable {
    private static final long serialVersionUID = -7769872146174877507L;
    private int value;

    public PermissionBits() {
    }

    public PermissionBits(int value) {
        this.value = value;
    }

    public PermissionBits(PermissionBits perms) {
        this(perms.value);
    }
    
    public void add(Permission perm) {
        int temp = 1 << perm.getIndex();
        value |= temp;
    }
    
    public void remove(Permission perm) {
        int temp = 1 << perm.getIndex();
        
        value &= ~temp;
    }
    
    public void addAll(int perms) {
        value |= perms;
    }
    
    public void addAll(PermissionBits perms) {
        addAll(perms.value);
    }
    
    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
    
    public boolean contains(Permission perm) {
        int temp = 1 << perm.getIndex();
        return (value & temp) > 0;
    }
    
    @Override
    public String toString() {
        String s = "{";
        for (Permission p : Permission.values()) {
            if (contains(p)) {
                s += p.name() + ", ";
            }
        }
        int index = s.lastIndexOf(",");
        if (index > 0) {
            return s.substring(0, s.lastIndexOf(',')) + "}";
        } else {
            return "{}";
        }
    }
    
    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PermissionBits other = (PermissionBits) obj;
        if (this.value != other.value) {
            return false;
        }
        return true;
    }

    @Override
    protected PermissionBits clone() {
        try {
            PermissionBits pb = (PermissionBits) super.clone();
            pb.value = this.value;

            return pb;
        } catch (CloneNotSupportedException ex) {
            // shouldn't go here since we implemented Clonable interface
            throw new RuntimeException("Can't clone " + PermissionBits.class.getName());
        }
    }
}
