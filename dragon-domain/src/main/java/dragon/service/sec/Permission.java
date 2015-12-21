package dragon.service.sec;

public enum Permission {

    list(0), read(1), create(2), delete(3), modify(4), update(5);
    
    private int index;
    
    private Permission(int index) {
        if (index <0  || index > 31) {
            throw new IllegalArgumentException("index much be between 0 and 31");
        }
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
