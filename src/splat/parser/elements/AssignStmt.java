package splat.parser.elements;

import splat.executor.ExecutionException;
import splat.executor.ReturnFromCall;
import splat.executor.values.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;

import java.util.Map;

public class AssignStmt extends Statement {

    private String label;
    private Expression expr;

    public AssignStmt(String label, Expression expr, Token tok) {
        super(tok);
        this.label = label;
        this.expr = expr;
    }

    public String getLabel() { return label; }

    public Expression getExpression() { return expr; }

    public String toString() {
        return label + " := " + expr + ";";
    }

    public void analyze(Map<String, FunctionDecl> funcMap, Map<String, Type> varAndParamMap) throws
            SemanticAnalysisException {
            Type lblType = varAndParamMap.get(label);
            Type exprType = expr.analyzeAndGetType(funcMap, varAndParamMap);
            // The type of <expr> must match that of the declared type of <label>.
            if (lblType != exprType) {
                throw new SemanticAnalysisException("Types do not match in the assignment call", this);
            }
    }

    @Override
    public void execute(Map<String, FunctionDecl> funcMap, Map<String, Value> varAndParamMap) throws ReturnFromCall, ExecutionException {
        Value val = expr.evaluate(funcMap, varAndParamMap);
        varAndParamMap.put(label, val);
    }
}
