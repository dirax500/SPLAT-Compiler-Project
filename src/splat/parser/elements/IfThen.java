package splat.parser.elements;

import com.sun.org.apache.xpath.internal.operations.Bool;
import splat.executor.ExecutionException;
import splat.executor.ReturnFromCall;
import splat.executor.values.BoolValue;
import splat.executor.values.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;

import java.util.List;
import java.util.Map;

public class IfThen extends Statement {

    private Expression expr;
    private List<Statement> stmts;

    public IfThen(Expression expr, List<Statement> stmts, Token tok) {
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
        String result = "if " + expr + "\n";
        result = result + "then \n";
        for (Statement stmt : stmts) {
            result = result + "   " + stmt + "\n";
        }
        result = result	+ "end if;";

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
        Value exprval = expr.evaluate(funcMap, varAndParamMap);
        boolean bval = ((BoolValue) exprval).getBool();
        if (bval) {
            for (Statement stmt : stmts) {
                stmt.execute(funcMap, varAndParamMap);
            }
        }
    }
}

