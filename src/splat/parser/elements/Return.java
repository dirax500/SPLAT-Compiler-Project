package splat.parser.elements;

import splat.executor.ExecutionException;
import splat.executor.ReturnFromCall;
import splat.executor.values.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;

import java.util.Map;

public class Return extends Statement {

    private Expression expr;

    public Return(Expression expr, Token tok) {
        super(tok);
        this.expr = expr;
    }

    public Expression getExpr() { return expr; }

    public String toString() {
        if (expr != null) {
            return "return" + expr + ";";
        } else {
            return "return" + ";";
        }
    }

    public void analyze(Map<String, FunctionDecl> funcMap, Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        if (varAndParamMap == null) {
            throw new SemanticAnalysisException("Call of 'return' cannot be in this place", this);
        }
        if (expr != null) {
            expr.analyzeAndGetType(funcMap, varAndParamMap);
        }
    }

    @Override
    public void execute(Map<String, FunctionDecl> funcMap, Map<String, Value> varAndParamMap) throws ReturnFromCall, ExecutionException {
        Value returnVal = null;
        if (expr != null) {
            returnVal = expr.evaluate(funcMap, varAndParamMap);
        }
        throw new ReturnFromCall(returnVal);
    }
}
