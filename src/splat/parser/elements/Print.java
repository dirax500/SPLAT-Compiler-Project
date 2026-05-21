package splat.parser.elements;

import splat.executor.ExecutionException;
import splat.executor.ReturnFromCall;
import splat.executor.values.BoolValue;
import splat.executor.values.IntValue;
import splat.executor.values.StrValue;
import splat.executor.values.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;

import java.util.Map;

public class Print extends Statement {

    private Expression expr;
    private boolean isPrintLine;

    public Print(Expression expr, boolean isPrintLine, Token tok) {
        super(tok);
        this.expr = expr;
        this.isPrintLine = isPrintLine;
    }

    public Expression getExpr() { return expr; }

    public boolean isPrintLine() { return isPrintLine; }

    public String toString() {
        if(isPrintLine) {
            return "print_line;";
        }
        else {
            return "print" + expr + ";";
        }
    }

    public void analyze(Map<String, FunctionDecl> funcMap, Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        if (!isPrintLine) {
            expr.analyzeAndGetType(funcMap, varAndParamMap);
        }
    }

    @Override
    public void execute(Map<String, FunctionDecl> funcMap, Map<String, Value> varAndParamMap) throws ReturnFromCall, ExecutionException {
        if (!isPrintLine) {
            Value exprval = expr.evaluate(funcMap, varAndParamMap);
            if (exprval instanceof IntValue) {
                System.out.print(((IntValue) exprval).getInt());
            } else if (exprval instanceof BoolValue) {
                System.out.print(((BoolValue) exprval).getBool());
            } else if (exprval instanceof StrValue) {
                System.out.print(((StrValue) exprval).getString());
            }
        } else {
            System.out.println();
        }
    }
}
