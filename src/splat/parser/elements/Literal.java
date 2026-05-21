package splat.parser.elements;

import splat.executor.ExecutionException;
import splat.executor.values.BoolValue;
import splat.executor.values.IntValue;
import splat.executor.values.StrValue;
import splat.executor.values.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;

import java.util.Map;

public class Literal extends Expression {

    private Object literal;

    public Literal(Object literal, Token tok) {
        super(tok);
        this.literal = literal;
    }

    public Object getLiteral() { return literal; }

    public String toString() {
        return literal.toString();
    }

    public Type analyzeAndGetType(Map<String, FunctionDecl> funcMap, Map<String, Type> varAndParamMap)
            throws SemanticAnalysisException {
        if (!(literal instanceof Token)) {
            throw new SemanticAnalysisException("Invalid literal type", this);
        }
        String strLit = ((Token) literal).getValue();
        if (isDigit(strLit.charAt(0))) {
            return Type.Int;
        } else if (strLit.equals("true") || strLit.equals("false")) {
            return Type.Bool;
        } else if (strLit.startsWith("\"") && strLit.endsWith("\"")) {
            return Type.Str;
        } else {
            throw new SemanticAnalysisException("Invalid type in literal " + literal, this);
        }
    }

    private boolean isDigit(int c) {
        if (c >= '0' && c <= '9') {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Value evaluate(Map<String, FunctionDecl> funcMap, Map<String, Value> varAndParamMap) throws ExecutionException {
        String strLit = ((Token) literal).getValue();
        if (isDigit(strLit.charAt(0))) {
            try {
                return new IntValue(Integer.parseInt(strLit));
            } catch (NumberFormatException e) {
                throw new ExecutionException("Invalid integer literal: " + strLit, this);
            }
        } else if (strLit.equals("true") || strLit.equals("false")) {
            return new BoolValue(Boolean.parseBoolean(strLit));
        } else if (strLit.startsWith("\"") && strLit.endsWith("\"")) {
            return new StrValue(strLit.substring(1, strLit.length() - 1));
        } else {
            throw new ExecutionException("Invalid type in literal " + literal, this);
        }
    }
}
