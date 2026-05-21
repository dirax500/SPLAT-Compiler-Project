package splat.parser.elements;

import splat.executor.ExecutionException;
import splat.executor.values.BoolValue;
import splat.executor.values.IntValue;
import splat.executor.values.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;

import java.util.Map;

public class UnaryOpExpr extends Expression {

    private Expression expr;
    private String unary_op;

    public UnaryOpExpr(Expression expr, String unary_op, Token tok) {
        super(tok);
        this.expr = expr;
        this.unary_op = unary_op;
    }

    public Expression getExpr() { return expr; }
    public String getUnaryOp() { return unary_op; }

    public String toString() {
        return "(" + unary_op + " " + expr + ")";
    }

    public Type analyzeAndGetType(Map<String, FunctionDecl> funcMap, Map<String, Type> varAndParamMap) throws
            SemanticAnalysisException {
        Type type = expr.analyzeAndGetType(funcMap, varAndParamMap);

        // Operator | Legal Argument Type | Resulting Type
        //    not   |        Boolean      |   Boolean
        if (unary_op.equals("not")) {
            if (type == Type.Bool) {
                return Type.Bool;
            } else {
                throw new SemanticAnalysisException("Invalid type in expression", expr);
            }
        }

        // Operator | Legal Argument Type | Resulting Type
        //     -    |        Integer      |   Integer
        if (unary_op.equals("-")) {
            if (type == Type.Int) {
                return Type.Int;
            } else {
                throw new SemanticAnalysisException("Invalid type in expression", expr);
            }
        }
        throw new SemanticAnalysisException("Unknown operator '" + unary_op + "'", this);
    }

    @Override
    public Value evaluate(Map<String, FunctionDecl> funcMap, Map<String, Value> varAndParamMap) throws ExecutionException {
        Value val = expr.evaluate(funcMap, varAndParamMap);
        if (unary_op.equals("-")) {
            int ival = ((IntValue) val).getInt();
            return new IntValue(-ival);
        }
        if (unary_op.equals("not")) {
            boolean bval = ((BoolValue) val).getBool();
            return new BoolValue(!bval);
        }
        throw new ExecutionException("Unknown operator '" + unary_op + "'", this);
    }


}
