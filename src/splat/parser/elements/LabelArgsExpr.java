package splat.parser.elements;

import splat.executor.ExecutionException;
import splat.executor.ReturnFromCall;
import splat.executor.values.Value;
import splat.lexer.Token;
import splat.semanticanalyzer.SemanticAnalysisException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LabelArgsExpr extends Expression {

    private String label;
    private List<Expression> args;

    public LabelArgsExpr(String label, List<Expression> args, Token tok) {
        super(tok);
        this.label = label;
        this.args = args;
    }

    public String getLabel() {
        return label;
    }
    public List<Expression> getArguments() {
        return args;
    }

    public String toString() {
        String result = label + " (";
        int i;
        for (i = 0; i < args.size(); i++) {
            result = result + args.get(i);
            if (i < args.size() - 1) {
                result = result + ", ";
            }
        }
        result = result + ");";

        return result;
    }

    public Type analyzeAndGetType(Map<String, FunctionDecl> funcMap, Map<String, Type> varAndParamMap) throws
            SemanticAnalysisException {
        FunctionDecl FunctDecl = funcMap.get(label);
        if (FunctDecl == null) {
            throw new SemanticAnalysisException("Following function is not declared in this scope: " + label, this);
        }
        // the number and types of <args> must match those of the declared <params> of the function.
        List<Parameter> params = FunctDecl.getParams();
        if (args.size() != params.size()) {
            throw new SemanticAnalysisException("Number of the arguments in function do not match as in parameters", this);
        }
        for (int i = 0; i < args.size(); i++) {
            Expression arg = args.get(i);
            Type argType = arg.analyzeAndGetType(funcMap, varAndParamMap);
            Type paramType = params.get(i).getType();
            if (argType != paramType) {
                throw new SemanticAnalysisException("Types of arguments do not match to those declared in parameters" +
                        "in the function call", this);
            }
        }
        // Functions of function call expressions must have a declared <ret-type> of something other than void
        Type retType = FunctDecl.getRetType();
        if (retType.equals(Type.Void)) {
            throw new SemanticAnalysisException("For expression, functions must have non-void return type", this);
        }
        return retType;
    }

    @Override
    public Value evaluate(Map<String, FunctionDecl> funcMap, Map<String, Value> varAndParamMap) throws ExecutionException {
        FunctionDecl FunctDecl = funcMap.get(label);
        List<Value> eargs = new ArrayList<>();
        for (Expression arg : args) {
            eargs.add(arg.evaluate(funcMap, varAndParamMap));
        }
        Map<String, Value> map = new HashMap<>();
        List<Parameter> params = FunctDecl.getParams();
        for (int i = 0; i < params.size(); i++) {
            map.put(params.get(i).getLabel(), eargs.get(i));
        }
        for (VariableDecl VarDecl : FunctDecl.getLocVarDecls()) {
            map.put(VarDecl.getLabel(), VarDecl.getStartValue());
        }
        try {
            for (Statement stmt : FunctDecl.getStmts()) {
                stmt.execute(funcMap, map);
            }
        } catch (ReturnFromCall r) {
            return r.getReturnVal();
        }
        throw new ExecutionException("Syntax error in this function", this);
    }
}