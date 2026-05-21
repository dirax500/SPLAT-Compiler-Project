package splat.executor.values;

public class BoolValue extends Value {
    public BoolValue(boolean val) {
        super(val);
    }

    public boolean getBool() {
        return ((Boolean)getValue());
    }
}
