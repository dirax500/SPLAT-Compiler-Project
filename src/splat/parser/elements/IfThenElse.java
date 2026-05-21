package splat.parser.elements;

import splat.executor.ExecutionException;
import splat.executor.ReturnFromCall;
import splat.executor.values.BoolValue;
import splat.executor.values.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;

import java.util.List;
import java.util.Map;

public class IfThenElse extends Statement {

    private Expression expr;
    private List<Statement> thenStmts;
    private List<Statement> elseStmts;

    public IfThenElse(Expression expr, List<Statement> thenStmts, List<Statement> elseStmts, Token tok) {
        super(tok);
        this.expr = expr;
        this.thenStmts = thenStmts;
        this.elseStmts = elseStmts;
    }

    public Expression getExpr() {
        return expr;
    }

    public List<Statement> getThenStmts() {
        return thenStmts;
    }

    public List<Statement> getElseStmts() { return elseStmts; }

    public String toString() {
        String result = "if " + expr + "\n";
        result = result + "then \n";
        for (Statement tstmt : thenStmts) {
            result = result + "   " + tstmt + "\n";
        }
        result = result + "else \n";
        for (Statement estmt : elseStmts) {
            result = result + "   " + estmt + "\n";
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
        for (Statement tstmt : thenStmts) {
            tstmt.analyze(funcMap, varAndParamMap);
            if (tstmt instanceof Return) {
                Return ret = (Return) tstmt;
                if (ret.getExpr() == null) {
                    throw new SemanticAnalysisException("IfThenElse loop must not have a void return statement", tstmt);
                }
            }
        }
        for (Statement estmt : elseStmts) {
            estmt.analyze(funcMap, varAndParamMap);
        }
    }

    @Override
    public void execute(Map<String, FunctionDecl> funcMap, Map<String, Value> varAndParamMap) throws ReturnFromCall, ExecutionException {
        Value exprval = expr.evaluate(funcMap, varAndParamMap);
        boolean bval = ((BoolValue) exprval).getBool();
        if (bval) {
            for (Statement tstmt : thenStmts) {
                tstmt.execute(funcMap, varAndParamMap);
            }
        } else {
            for (Statement estmt : elseStmts) {
                estmt.execute(funcMap, varAndParamMap);
            }
        }
    }
}
