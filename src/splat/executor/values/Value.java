package splat.executor.values;

public abstract class Value {
    private Object val;
    public Value(Object val) {
        this.val = val;
    }
    public Object getValue() { return val; }
}

// Several subclasses of Value are created to represent things of different types.