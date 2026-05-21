package splat.parser.elements;

import splat.lexer.Token;

public class Parameter extends ASTElement {
    public String label;
    public Type type;

    public Parameter(String label, Type type, Token tok) {
        super(tok);
        this.label = label;
        this.type = type;
    }

    public String getLabel() { return label; }
    public Type getType() { return type; }

    public String toString() {
        return label + ": " + type;
    }
}
