package splat.parser.elements;

import splat.executor.ExecutionException;
import splat.executor.ReturnFromCall;
import splat.executor.values.BoolValue;
import splat.executor.values.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;

import java.util.List;
import java.util.Map;

public class WhileDoEnd extends Statement {

    private Expression expr;
    private List<Statement> stmts;

    public WhileDoEnd(Expression expr, List<Statement> stmts, Token tok) {
        super(tok);
        this.expr = expr;
        this.stmts = stmts;
    }

    public Expression getExpr() {
        return expr;
    }

    public List<Statement> getStmts() {
        return stmts;
    }

    public String toString() {
        String result = "while " + expr + "\n";
        result = result + "do \n";
        for (Statement stmt : stmts) {
            result = result + "   " + stmt + "\n";
        }
        result = result	+ "end while;";

        return result;
    }

    public void analyze(Map<String, FunctionDecl> funcMap, Map<String, Type> varAndParamMap) throws
            SemanticAnalysisException {
        Type exprType = expr.analyzeAndGetType(funcMap, varAndParamMap);
        // The type of <expr> must be Boolean.
        if (exprType != Type.Bool) {
            throw new SemanticAnalysisException("Expression must be Boolean", expr);
        }
        for (Statement stmt : stmts) {
            stmt.analyze(funcMap, varAndParamMap);
        }
    }

    @Override
    public void execute(Map<String, FunctionDecl> funcMap, Map<String, Value> varAndParamMap) throws ReturnFromCall, ExecutionException {
        while (true) {
            Value exprval = expr.evaluate(funcMap, varAndParamMap);
            boolean bval = ((BoolValue) exprval).getBool();
            if (!bval) break;

            for (Statement stmt : stmts) {
                stmt.execute(funcMap, varAndParamMap);
            }
        }
    }
}
