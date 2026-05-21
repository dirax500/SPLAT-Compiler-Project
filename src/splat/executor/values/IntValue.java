package splat.executor.values;

public class IntValue extends Value {
    public IntValue(int val) {
        super(val);
    }
    public int getInt() {
        return (int) getValue();
    }
}