package splat.semanticanalyzer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import splat.parser.elements.*;

public class SemanticAnalyzer {

	private ProgramAST progAST;
	
	private Map<String, FunctionDecl> funcMap;
	private Map<String, Type> progVarMap;
	
	public SemanticAnalyzer(ProgramAST progAST) {
		this.progAST = progAST;
        this.funcMap = new HashMap<>();
        this.progVarMap = new HashMap<>();
    }

	public void analyze() throws SemanticAnalysisException {
		
		// Checks to make sure we don't use the same labels more than once for our program functions and variables
		checkNoDuplicateProgLabels();
		
		/* This sets the maps that will be needed later when we need to typecheck variable references and function
		calls in the program body */
		setProgVarAndFuncMaps();
		
		// Perform semantic analysis on the functions
		for (FunctionDecl funcDecl : funcMap.values()) {	
			analyzeFuncDecl(funcDecl);
		}
		
		// Perform semantic analysis on the program body
		for (Statement stmt : progAST.getStmts()) {
			stmt.analyze(funcMap, progVarMap);
		}
		
	}

	private void analyzeFuncDecl(FunctionDecl funcDecl) throws SemanticAnalysisException {
		
		/* Checks to make sure we don't use the same labels more than once among our function parameters, local variables,
		and function names */
		checkNoDuplicateFuncLabels(funcDecl);
		
		// Get the types of the parameters and local variables
		Map<String, Type> varAndParamMap = getVarAndParamMap(funcDecl);

		// Perform semantic analysis on the function body
		for (Statement stmt : funcDecl.getStmts()) {
			stmt.analyze(funcMap, varAndParamMap);
		}

        // check the return
        Type retType = funcDecl.getRetType();
        boolean isVoid = retType.equals(Type.Void);

        // check for return statement
        if (funcDecl.getStmts().isEmpty()) {
            if (!isVoid) {
                throw new SemanticAnalysisException("Non-void function must end with 'return <expr>;'", funcDecl);
            }
            return;
        }

        // last statement which is usually a return statement
        Statement last = funcDecl.getStmts().get(funcDecl.getStmts().size() - 1);

        /* If the return type <ret-type> is void, then the function cannot have a statement of the form return
        <expr> ;. Such a function completes execution when a statement of the form return ; is encountered,
        or the final statement of the body is executed. */
        /* If the return type of the function it non-void, then the function may not contain a simple return ;
        statement, but its body statements must be expr-return-terminated. */
        // For Return and IfThenElse the <expr> on all return statements must be of the same type as <ret-type>.
        if (isVoid) {
            if (last instanceof Return) {
                Return r = (Return) last;
                if (r.getExpr() != null) {
                    throw new SemanticAnalysisException("Void function cannot have 'return <expr>'", r);
                }
            }
        } else {
            if (last instanceof Return) {
                Return r = (Return) last;
                if (r.getExpr() == null) {
                    throw new SemanticAnalysisException("Non-void function must end with 'return <expr>;'", r);
                } else {
                    Type exprType = r.getExpr().analyzeAndGetType(funcMap, varAndParamMap);
                    if (!exprType.equals(retType)) {
                        throw new SemanticAnalysisException("The type in return statement does not match: " + exprType, r);
                    }
                }
            } else if (last instanceof IfThenElse) {
                checkRetCall((IfThenElse) last, retType, funcMap, varAndParamMap);
            } else {
                throw new SemanticAnalysisException("Non-void function must end with 'return <expr>;'", last);
            }
        }
	}

    // checks that every execution path makes 'return' when the function is defined to be of any type except void
    // if <expr> then <stmts> else <stmts> end if; where both <stmts> must have return <expr>;
    private void checkRetCall(IfThenElse ite, Type retType, Map<String, FunctionDecl> funcMap,
            Map<String, Type> varAndParamMap) throws SemanticAnalysisException {

        // last statements of both then and else statements which are checked for return;
        Statement lastThen = ite.getThenStmts().get(ite.getThenStmts().size() - 1);
        Statement lastElse = ite.getElseStmts().get(ite.getElseStmts().size() - 1);

        // Recursion
        checkSingleRetCall(lastThen, retType, funcMap, varAndParamMap);
        checkSingleRetCall(lastElse, retType, funcMap, varAndParamMap);
    }

    // checks that a single statement makes 'return'
    private void checkSingleRetCall(Statement stmt, Type retType, Map<String, FunctionDecl> funcMap,
                                   Map<String, Type> varAndParamMap) throws SemanticAnalysisException {
        if (stmt instanceof Return) {
            Return r = (Return) stmt;
            if (r.getExpr() == null) {
                throw new SemanticAnalysisException("Non-void function must end with 'return <expr>;'", r);
            } else {
                Type exprType = r.getExpr().analyzeAndGetType(funcMap, varAndParamMap);
                if (!exprType.equals(retType)) {
                    throw new SemanticAnalysisException("The type in return statement does not match: " + exprType, r);
                }
            }
        }
        else if (stmt instanceof IfThenElse) {
            checkRetCall((IfThenElse) stmt, retType, funcMap, varAndParamMap);
        }
        else {
            throw new SemanticAnalysisException("Non-void function must end with 'return <expr>;'", stmt);
        }
    }

    private Map<String, Type> getVarAndParamMap(FunctionDecl funcDecl) {
        Map<String, Type> varAndParamMap = new HashMap<String, Type>();
        for (Parameter param : funcDecl.getParams()) {
            String parLbl = param.getLabel().toString();
            Type parType =  param.getType();
            varAndParamMap.put(parLbl, parType);
        }
        for (VariableDecl varDecl : funcDecl.getLocVarDecls()) {
            String varLbl = varDecl.getLabel().toString();
            Type varType = varDecl.getType();
            varAndParamMap.put(varLbl, varType);
        }
        return varAndParamMap;
	}

	private void checkNoDuplicateFuncLabels(FunctionDecl funcDecl) throws SemanticAnalysisException {
        Set<String> labels = new HashSet<String>();
        String funcLabel = funcDecl.getLabel().toString();
        labels.add(funcLabel);
        for (Parameter param : funcDecl.getParams()) {
            String label = param.getLabel().toString();
            if (labels.contains(label)) {
                throw new SemanticAnalysisException("Cannot have duplicate label '" + label + "' in function", param);
            } else {
                labels.add(label);
            }
            if (funcMap.containsKey(label) && !label.equals(funcLabel)) {
                throw new SemanticAnalysisException("Cannot have duplicate label '" + label + "' with the parameter", param);
            }
        }
        for (VariableDecl decl : funcDecl.getLocVarDecls()) {
            String label = decl.getLabel().toString();
            if (labels.contains(label)) {
                throw new SemanticAnalysisException("Cannot have duplicate label '" + label + "' in function", decl);
            } else {
                labels.add(label);
            }
        }
	}
	
	private void checkNoDuplicateProgLabels() throws SemanticAnalysisException {
		Set<String> labels = new HashSet<String>();
 		for (Declaration decl : progAST.getDecls()) {
 			String label = decl.getLabel().toString();
			if (labels.contains(label)) {
				throw new SemanticAnalysisException("Cannot have duplicate label '" + label + "' in program", decl);
			} else {
				labels.add(label);
			}
		}
	}
	
	private void setProgVarAndFuncMaps() {
		for (Declaration decl : progAST.getDecls()) {
			String label = decl.getLabel().toString();
			if (decl instanceof FunctionDecl) {
				FunctionDecl funcDecl = (FunctionDecl)decl;
				funcMap.put(label, funcDecl);
			} else if (decl instanceof VariableDecl) {
				VariableDecl varDecl = (VariableDecl)decl;
				progVarMap.put(label, varDecl.getType());
			}
		}
	}
}
