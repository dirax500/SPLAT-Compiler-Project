package splat.parser.elements;

import splat.executor.ExecutionException;
import splat.executor.values.BoolValue;
import splat.executor.values.IntValue;
import splat.executor.values.StrValue;
import splat.executor.values.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;

import java.util.Map;
import java.util.Objects;

public class ExprBinOpExpr extends Expression {

    private Expression expr1;
    private Expression expr2;
    private String bin_op;

    public ExprBinOpExpr(Expression expr1, Expression expr2, String bin_op, Token tok) {
        super(tok);
        this.expr1 = expr1;
        this.expr2 = expr2;
        this.bin_op = bin_op;
    }

    public Expression getExpr1() { return expr1; }
    public Expression getExpr2() { return expr2; }
    public String getBinOp() { return bin_op; }

    public String toString() {
        return "(" + expr1 + " " + bin_op + " " + expr2 + ")";
    }

    public Type analyzeAndGetType(Map<String, FunctionDecl> funcMap, Map<String, Type> varAndParamMap) throws
            SemanticAnalysisException {
        Type type1 = expr1.analyzeAndGetType(funcMap, varAndParamMap);
        Type type2 = expr2.analyzeAndGetType(funcMap, varAndParamMap);

        // Operator | Legal Argument Type | Resulting Type
        //  and, or |        Boolean      |    Boolean
        if (bin_op.equals("and") || bin_op.equals("or")) {
            if (type1 == Type.Bool && type2 == Type.Bool) {
                return Type.Bool;
            } else {
                throw new SemanticAnalysisException("Invalid type in expression", expr1);
            }
        }

        //    Operator   | Legal Argument Type | Resulting Type
        //  >, <, >=, <= |        Integer      |    Boolean
        if (bin_op.equals(">") || bin_op.equals("<") || bin_op.equals(">=") || bin_op.equals("<=")) {
            if (type1 == Type.Int && type2 == Type.Int) {
                return Type.Bool;
            } else {
                throw new SemanticAnalysisException("Invalid type in expression", expr1);
            }
        }

        // Operator |           Legal Argument Type          | Resulting Type
        //    ==    | Any non-void type, but both must match |    Boolean
        if (bin_op.equals("==")) {
            if (type1.getType().equals(type2.getType())) {
                return Type.Bool;
            } else {
                throw new SemanticAnalysisException("Both expressions must have the same type", expr2);
            }
        }

        //      Operator   | Legal Argument Type | Resulting Type
        //  math operators |        Integer      |    Integer
        if (bin_op.equals("-") || bin_op.equals("+") || bin_op.equals("*") || bin_op.equals("/") || bin_op.equals("%")) {
            if (type1 == Type.Int && type2 == Type.Int) {
                return Type.Int;
            } else {
                throw new SemanticAnalysisException("Invalid type in expression", expr1);
            }
        }
        throw new SemanticAnalysisException("Unknown operator '" + bin_op + "'", this);
    }

    @Override
    public Value evaluate(Map<String, FunctionDecl> funcMap, Map<String, Value> varAndParamMap) throws ExecutionException {
        Value val1 = expr1.evaluate(funcMap, varAndParamMap);
        Value val2 = expr2.evaluate(funcMap, varAndParamMap);

        if (bin_op.equals("and")) {
            boolean bval1 = ((BoolValue) val1).getBool();
            boolean bval2 = ((BoolValue) val2).getBool();
            return new BoolValue(bval1 & bval2);
        }
        if (bin_op.equals("or")) {
            boolean bval1 = ((BoolValue) val1).getBool();
            boolean bval2 = ((BoolValue) val2).getBool();
            return new BoolValue(bval1 || bval2);
        }
        if (bin_op.equals("<")) {
            int ival1 = ((IntValue) val1).getInt();
            int ival2 = ((IntValue) val2).getInt();
            return new BoolValue(ival1 < ival2);
        }
        if (bin_op.equals(">")) {
            int ival1 = ((IntValue) val1).getInt();
            int ival2 = ((IntValue) val2).getInt();
            return new BoolValue(ival1 > ival2);
        }
        if (bin_op.equals("<=")) {
            int ival1 = ((IntValue) val1).getInt();
            int ival2 = ((IntValue) val2).getInt();
            return new BoolValue(ival1 <= ival2);
        }
        if (bin_op.equals(">=")) {
            int ival1 = ((IntValue) val1).getInt();
            int ival2 = ((IntValue) val2).getInt();
            return new BoolValue(ival1 >= ival2);
        }
        if (bin_op.equals("==")) {
            if (val1 instanceof IntValue && val2 instanceof IntValue) {
                return new BoolValue(((IntValue) val1).getInt() == ((IntValue) val2).getInt());
            } else if (val1 instanceof BoolValue && val2 instanceof BoolValue) {
                return new BoolValue(((BoolValue) val1).getBool() == ((BoolValue) val2).getBool());
            } else if (val1 instanceof StrValue && val2 instanceof StrValue) {
                return new BoolValue (Objects.equals(((StrValue) val1).getString(), ((StrValue) val2).getString()));
            }
        }
        if (bin_op.equals("+")) {
            int ival1 = ((IntValue) val1).getInt();
            int ival2 = ((IntValue) val2).getInt();
            return new IntValue(ival1 + ival2);
        }
        if (bin_op.equals("-")) {
            int ival1 = ((IntValue) val1).getInt();
            int ival2 = ((IntValue) val2).getInt();
            return new IntValue(ival1 - ival2);
        }
        if (bin_op.equals("*")) {
            int ival1 = ((IntValue) val1).getInt();
            int ival2 = ((IntValue) val2).getInt();
            return new IntValue(ival1 * ival2);
        }
        if (bin_op.equals("/")) {
            int ival1 = ((IntValue) val1).getInt();
            int ival2 = ((IntValue) val2).getInt();
            if (ival2 != 0) {
                return new IntValue(ival1 / ival2);
            } else {
                throw new ExecutionException("Divide by zero is impossible", this);
            }
        }
        if (bin_op.equals("%")) {
            int ival1 = ((IntValue) val1).getInt();
            int ival2 = ((IntValue) val2).getInt();
            if (ival2 != 0) {
                return new IntValue(ival1 % ival2);
            } else {
                throw new ExecutionException("Divide by zero is impossible", this);
            }
        }
        throw new ExecutionException("Unknown operator '" + bin_op + "'", this);
    }

}
