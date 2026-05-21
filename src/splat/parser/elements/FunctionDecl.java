package splat.parser.elements;

import splat.lexer.Token;

import java.util.List;

public class FunctionDecl extends Declaration {

	private String label;
    private List<Parameter> params;
    private Type retType;
    private List<VariableDecl> locVarDecls;
    private List<Statement> stmts;

	public FunctionDecl(String label, List<Parameter> params, Type retType, List<VariableDecl> locVarDecls,
                        List<Statement> stmts, Token tok) {
		super(tok);
        this.label = label;
        this.params = params;
        this.retType = retType;
        this.locVarDecls = locVarDecls;
        this.stmts = stmts;
	}

    public String getLabel() { return label; }
    public List<Parameter> getParams() { return params; }
    public Type getRetType() { return retType; }
    public List<VariableDecl> getLocVarDecls() { return locVarDecls; }
    public List<Statement> getStmts() { return stmts; }

    public String toString() {
        String result = label + "(";
        int i;
        for (i =  0; i < params.size(); i++) {
            result = result + params.get(i);
            if  (i != params.size() - 1) {
                result = result + ", ";
            }
        }
        result = result + ") :" + retType + " is\n";
        for (VariableDecl lvd : locVarDecls) {
            result = result + "   " + lvd + "\n";
        }
        result = result + "begin\n";
        for (Statement stmt : stmts) {
            result = result + "   " + stmt + "\n";
        }
        result = result + "end;";
        return result;
    }
}