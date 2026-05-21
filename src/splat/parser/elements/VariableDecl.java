package splat.parser.elements;

import splat.executor.ExecutionException;
import splat.executor.values.BoolValue;
import splat.executor.values.IntValue;
import splat.executor.values.StrValue;
import splat.executor.values.Value;
import splat.lexer.Token;

public class VariableDecl extends Declaration {

	private String label;
    private Type type;

	public VariableDecl(String label, Type type, Token tok) {
        super(tok);
        this.label = label;
        this.type = type;
	}

    public String getLabel() { return label; }
    public Type getType() { return type; }

    public Value getStartValue() throws ExecutionException {
        if (type == Type.Int) {
            return new IntValue(0);
        } else if (type == Type.Bool) {
            return new BoolValue(false);
        } else if (type == Type.Str) {
            return new StrValue("");
        } else {
            throw new ExecutionException("Unknown type", this);
        }
    }

	public String toString() {
		return label + " : " + type + ";";
	}
}