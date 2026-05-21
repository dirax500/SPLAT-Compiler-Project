package splat.parser.elements;

import splat.executor.values.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;

import java.util.Map;

public class Label extends Expression {

    private String label;

    public Label(String label, Token tok) {
        super(tok);
        this.label = label;
    }

    public String getLabel() { return label; }

    public String toString() {
        return label;
    }

    public Type analyzeAndGetType(Map<String, FunctionDecl> funcMap, Map<String, Type> varAndParamMap) throws
            SemanticAnalysisException {
        Type lblType = varAndParamMap.get(label);
        if (lblType == null) {
            throw new SemanticAnalysisException("Following variable is not declared in this scope: " + label, this);
        }
        return lblType;
    }

    @Override
    public Value evaluate(Map<String, FunctionDecl> funcMap, Map<String, Value> varAndParamMap) {
        Value labelValue = varAndParamMap.get(label);
        return labelValue;
    }
}
