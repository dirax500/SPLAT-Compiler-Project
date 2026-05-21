package splat.executor.values;

public class StrValue extends Value {
    public StrValue(String val) {
        super(val);
    }

    public String getString() {
        return ((String)getValue());
    }
}
