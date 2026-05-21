package splat.parser;

import java.util.ArrayList;
import java.util.List;

import splat.lexer.Token;
import splat.parser.elements.*;
import splat.semanticanalyzer.SemanticAnalysisException;

public class Parser {

    private List<Token> tokens;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Compares the next token to an expected value, and throws
     * an exception if they don't match.  This removes the front-most
     * (next) token
     *
     * @param expected value of the next token
     * @throws ParseException if the actual token doesn't match what
     * 			was expected
     */
    private void checkNext(String expected) throws ParseException {

        Token tok = tokens.remove(0);

        if (!tok.getValue().equals(expected)) {
            throw new ParseException("Expected '"+ expected + "', got '"
                    + tok.getValue()+ "'.", tok);
        }
    }

    /**
     * Returns a boolean indicating whether or not the next token matches
     * the expected String value.  This does not remove the token from the
     * token list.
     *
     * @param expected value of the next token
     * @return true iff the token value matches the expected string
     */
    private boolean peekNext(String expected) {
        return tokens.get(0).getValue().equals(expected);
    }

    /**
     * Returns a boolean indicating whether or not the token directly after
     * the front most token matches the expected String value.  This does
     * not remove any tokens from the token list.
     *
     * @param expected value of the token directly after the next token
     * @return true iff the value matches the expected string
     */
    private boolean peekTwoAhead(String expected) {
        return tokens.get(1).getValue().equals(expected);
    }



    // <program> ::= program <decls> begin <stmts> end ;
    public ProgramAST parse() throws ParseException {

        try {
            // Needed for 'program' token position info
            Token startTok = tokens.get(0);

            checkNext("program");

            List<Declaration> decls = parseDecls();

            checkNext("begin");

            List<Statement> stmts = parseStmts();

            checkNext("end");
            checkNext(";");

            return new ProgramAST(decls, stmts, startTok);

            // This might happen if we do a tokens.get(), and nothing is there!
        } catch (IndexOutOfBoundsException ex) {

            throw new ParseException("Unexpectedly reached the end of file.", -1, -1);
        }
    }

    //  <decls> ::= (  <decl>  )*
    private List<Declaration> parseDecls() throws ParseException {

        List<Declaration> decls = new ArrayList<Declaration>();

        while (!peekNext("begin")) {
            Declaration decl = parseDecl();
            decls.add(decl);
        }

        return decls;
    }


    // <decl> ::= <var-decl> | <func-decl>
    private Declaration parseDecl() throws ParseException {
        if (peekTwoAhead(":")) {
            return parseVarDecl();
        } else if (peekTwoAhead("(")) {
            return parseFuncDecl();
        } else {
            Token tok = tokens.get(0);
            throw new ParseException("Declaration expected", tok);
        }
    }

    // <func-decl> ::= <label> ( <params> ) : <ret-type> is <loc-var-decls> begin <stmts> end ;
    private FunctionDecl parseFuncDecl() throws ParseException {
        Token startTok = tokens.get(0);
        Token labelTok = tokens.remove(0);
        String label = labelTok.getValue();
        isValidLabel(label, startTok);
        checkNext("(");
        List<Parameter> params = parseParams();
        checkNext(")");
        checkNext(":");

        Token retTypeTok = tokens.remove(0);
        String retType = retTypeTok.getValue();
        Type type;
        if (retType.equals("Integer")) {
            type = Type.Int;
        } else if (retType.equals("Boolean")) {
            type = Type.Bool;
        } else if  (retType.equals("String")) {
            type = Type.Str;
        } else if (retType.equals("void")) {
            type = Type.Void;
        } else {
            throw new ParseException("Invalid type '" + retType + "'", retTypeTok);
        }

        checkNext("is");
        List<VariableDecl> lvd = parseLocVarDecls();
        checkNext("begin");
        List<Statement> stmts = parseStmts();
        checkNext("end");
        checkNext(";");
        return new FunctionDecl(label, params, type, lvd, stmts, startTok);
    }

    // <loc-var-decls> ::= (  <var-decl>  )*
    private List<VariableDecl> parseLocVarDecls() throws ParseException {
        List<VariableDecl> lvd = new ArrayList<VariableDecl>();
        while (!peekNext("begin")) {
            VariableDecl varDecl = parseVarDecl();
            lvd.add(varDecl);
        }
        return lvd;
    }

    // <params> ::= <param>   ( ,   <param> )*
    private List<Parameter> parseParams() throws ParseException {
        List<Parameter> params = new ArrayList<Parameter>();
        if (peekNext(")")) {
            return params;
        }
        params.add(parseParam());
        while (peekNext(",")) {
            checkNext(",");
            Parameter param = parseParam();
            params.add(param);
        }
        return params;
    }

    // <param> ::= <label> : <type>
    private Parameter parseParam() throws ParseException {
        Token startTok = tokens.get(0);
        Token labelTok = tokens.remove(0);
        String label = labelTok.getValue();
        isValidLabel(label, startTok);
        checkNext(":");

        Token typeTok = tokens.remove(0);
        String strType = typeTok.getValue();
        Type type;
        if (strType.equals("Integer")) {
            type = Type.Int;
        } else if (strType.equals("Boolean")) {
            type = Type.Bool;
        } else if  (strType.equals("String")) {
            type = Type.Str;
        } else {
            throw new ParseException("Invalid type '" + strType + "'", typeTok);
        }
        return new Parameter(label, type, startTok);
    }

    // <var-decl> ::= <label> : <type> ;
    private VariableDecl parseVarDecl() throws ParseException {
        Token startTok = tokens.get(0);
        Token labelTok = tokens.remove(0);
        String label = labelTok.getValue();
        isValidLabel(label, startTok);
        checkNext(":");

        Token typeTok = tokens.remove(0);
        String strType = typeTok.getValue();
        Type type;
        if (strType.equals("Integer")) {
            type = Type.Int;
        } else if (strType.equals("Boolean")) {
            type = Type.Bool;
        } else if  (strType.equals("String")) {
            type = Type.Str;
        } else {
            throw new ParseException("Invalid type '" + strType + "'", typeTok);
        }
        checkNext(";");
        return new VariableDecl(label, type, startTok);
    }

    // <stmts> ::= (  <stmt>  )*
    private List<Statement> parseStmts() throws ParseException {
        List<Statement> stmts = new ArrayList<Statement>();

        while (!peekNext("end") && !peekNext("end while") && !peekNext("end if") &&
                !peekNext("else")) {
            Statement stmt = parseStmt();
            stmts.add(stmt);
        }

        return stmts;
    }

    private Statement parseStmt() throws ParseException {
        if (peekTwoAhead(":=")) {
            return parseAssign();
        } else if (peekNext("while")) {
            return parseWhileDo();
        } else if (peekNext("if")) {
            return parseIf();
        } else if (peekNext("print") || peekNext("print_line")) {
            return parsePrint();
        } else if (peekNext("return")) {
            return parseReturn();
        } else if (peekTwoAhead("(")) {
            return parseArgsStmt();
        } else {
            Token tok = tokens.get(0);
            String errorTok = tok.getValue();
            throw new ParseException("Invalid statement at '" + errorTok + "'", tok);
        }
    }

    // <stmt>  ::= <label>   :=    <expr>   ;
    private AssignStmt parseAssign() throws ParseException {
        Token startTok = tokens.get(0);
        Token labelTok = tokens.remove(0);
        String label = labelTok.getValue();
        isValidLabel(label, startTok);
        checkNext(":=");
        Expression expr= parseExpr();
        checkNext(";");
        return new AssignStmt(label, expr, startTok);
    }

    // while   <expr>  do  <stmts>   end while  ;
    private WhileDoEnd parseWhileDo() throws ParseException {
        Token startTok = tokens.get(0);
        checkNext("while");
        Expression expr = parseExpr();
        checkNext("do");
        List<Statement> stmts = parseStmts();
        checkNext("end");
        checkNext("while");
        checkNext(";");
        return new WhileDoEnd(expr, stmts, startTok);
    }

    // stmt ::= if   <expr>  then  <stmts>   else  <stmts>   end if  ;
    // stmt ::= if   <expr>  then  <stmts>   end if  ;
    private Statement parseIf() throws ParseException {
        Token startTok = tokens.get(0);
        checkNext("if");
        Expression expr = parseExpr();
        checkNext("then");
        List<Statement> thenStmts = parseStmts();
        if (peekNext("else")) {
            checkNext("else");
            List<Statement> elseStmts = parseStmts();
            checkNext("end");
            checkNext("if");
            checkNext(";");
            return new IfThenElse(expr, thenStmts, elseStmts, startTok);
        } else {
            checkNext("end");
            checkNext("if");
            checkNext(";");
            return new IfThen(expr, thenStmts, startTok);
        }
    }

    // stmt ::= <label>  (   <args> ) ;
    private LabelArgsStmt parseArgsStmt() throws ParseException {
        Token startTok = tokens.get(0);
        Token labelTok = tokens.remove(0);
        String label = labelTok.getValue();
        isValidLabel(label, startTok);
        checkNext("(");
        List<Expression> args = parseArgs();
        checkNext(")");
        checkNext(";");
        return new LabelArgsStmt(label, args, startTok);
    }

    // stmt ::= print  <expr>  ;
    // stmt ::=  print_line   ;
    private Print parsePrint() throws ParseException {
        Token startTok = tokens.get(0);
        boolean isPrintLine = peekNext("print_line");
        Expression expr = null;
        if (!isPrintLine) {
            checkNext("print");
            expr = parseExpr();
        } else {
            checkNext("print_line");
        }
        checkNext(";");
        return new Print(expr, isPrintLine, startTok);
    }

    // stmt ::= return  <expr> ;
    // stmt ::= return  ;
    private Return parseReturn() throws ParseException {
        Token startTok = tokens.get(0);
        checkNext("return");
        Expression expr = null;
        if (!peekNext(";")) {
            expr = parseExpr();
        }
        checkNext(";");
        return new Return(expr, startTok);
    }

    private Expression parseExpr() throws ParseException {
        Token tok = tokens.get(0);
        if (peekNext("(")) {
            if (peekTwoAhead("-") || peekTwoAhead("not")) {
                return parseUnaryOp();
            } else {
                return parseBinOp();
            }
        } else if (peekTwoAhead("(")) {
            return parseArgsExpr();
        } else if ((isLetter(tok.getValue().charAt(0)) || tok.getValue().charAt(0) == '_') && !peekNext("true")
                && !peekNext("false")) {
            return parseLabel();
        } else if (isDigit(tok.getValue().charAt(0)) || peekNext("true") || peekNext("false") ||
                tok.getValue().charAt(0) == '"') {
            return parseLiteral();
        }
        else {
            String errorTok = tok.getValue();
            throw new ParseException("Invalid expression at '" + errorTok + "'", tok);
        }
    }

    // <expr> ::= (  <expr>   <bin-op>   <expr> )
    private ExprBinOpExpr parseBinOp() throws ParseException {
        Token startTok = tokens.get(0);
        checkNext("(");
        Expression expr1 = parseExpr();

        Token tok = tokens.remove(0);
        String bin_op = tok.getValue();
        if (!bin_op.equals("and") && !bin_op.equals("or") && !bin_op.equals(">") && !bin_op.equals("<") &&
                !bin_op.equals("==") && !bin_op.equals(">=") && !bin_op.equals("<=") && !bin_op.equals("+") &&
                !bin_op.equals("-") && !bin_op.equals("*") && !bin_op.equals("/") && !bin_op.equals("%")) {
            throw new ParseException("Invalid binary operator '" + tok.getValue()+ "'.", tok);
        }

        Expression expr2 = parseExpr();
        checkNext(")");
        return new ExprBinOpExpr(expr1, expr2, bin_op, startTok);
    }

    // <expr> ::= (  <unary-op>   <expr> )
    private UnaryOpExpr parseUnaryOp() throws ParseException {
        Token startTok = tokens.get(0);
        checkNext("(");

        Token tok = tokens.remove(0);
        String unary_op = tok.getValue();
        if (!unary_op.equals("not") && !unary_op.equals("-")) {
            throw new ParseException("Invalid unary operator" + tok.getValue()+ "'.", tok);
        }

        Expression expr = parseExpr();
        checkNext(")");
        return new UnaryOpExpr(expr, unary_op, startTok);
    }

    // <expr> ::= <label>  ( <args> )
    private LabelArgsExpr parseArgsExpr() throws ParseException {
        Token startTok = tokens.get(0);
        Token labelTok = tokens.remove(0);
        String label = labelTok.getValue();
        isValidLabel(label, startTok);
        checkNext("(");
        List<Expression> args = parseArgs();
        checkNext(")");
        return new LabelArgsExpr(label, args, startTok);
    }

    // <expr> ::= <label>
    private Label parseLabel() throws ParseException {
        Token token = tokens.get(0);
        Token labelTok = tokens.remove(0);
        String label = labelTok.getValue();
        isValidLabel(label, token);
        return new Label(label, token);
    }

    // <expr> ::= <literal>
    private Literal parseLiteral() throws ParseException {
        Token token = tokens.get(0);
        Token literal = tokens.remove(0);
        if (isDigit(literal.getValue().charAt(0))) {
            for (int i = 1; i < literal.getValue().length(); i++) {
                if (!isDigit(literal.getValue().charAt(i))) {
                    throw new ParseException("Invalid literal", literal);
                }
            }
        } else if (literal.getValue().charAt(0) == '\"') {
            for (int i = 1; i < literal.getValue().length(); i++) {
                if (literal.getValue().charAt(i) == '\n' || literal.getValue().charAt(i) == '\\') {
                    throw new ParseException("Invalid literal", literal);
                }
            }
        } else if (!literal.getValue().equals("true") && !literal.getValue().equals("false")) {
            throw new  ParseException("Invalid literal at " + literal.getValue(), literal);
        }
        return new  Literal(literal, token);
    }

    // <args>  ::= <expr>   ( ,   <expr> )*
    private List<Expression> parseArgs() throws ParseException {
        List<Expression> exprs = new ArrayList<Expression>();
        if (peekNext(")")) {
            return exprs;
        }
        exprs.add(parseExpr());
        while (peekNext(",")) {
            checkNext(",");
            Expression expr = parseExpr();
            exprs.add(expr);
        }
        return exprs;
    }

    // Supporting functions
    private boolean isLetter(int c) {
        if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z') {
            return true;
        } else {
            return false;
        }
    }

    private boolean isDigit(int c) {
        if (c >= '0' && c <= '9') {
            return true;
        } else {
            return false;
        }
    }

    private void isValidLabel(String label, Token tok) throws ParseException {
            if (label.equals("program") || label.equals("begin") || label.equals("end") || label.equals("while") ||
            label.equals("do") || label.equals("if") || label.equals("then") || label.equals("else") ||
            label.equals("print") || label.equals("print_line") || label.equals("return") || label.equals("and") ||
            label.equals("or") || label.equals("not") || label.equals("Integer") || label.equals("Boolean") ||
            label.equals("String") || label.equals("void") || label.equals("true") || label.equals("false")) {
                throw new ParseException("Invalid label: '" + label + "' (reserved keyword)", tok);
            }

        char firstLet = label.charAt(0);
        if (!isLetter(firstLet) && firstLet != '_') {
            throw new ParseException("Invalid label: '" + label + "'", tok);
        }

        for (int i = 1; i < label.length(); i++) {
            char ch = label.charAt(i);
            if (!isLetter(ch) && !isDigit(ch) && ch != '_') {
                if (i == label.length() - 1) {
                    if (ch != ':') {
                        throw new ParseException("Invalid label: '" + label + "'", tok);
                    }
                } else {
                    throw new ParseException("Invalid label: '" + label + "'", tok);
                }
            }
        }
    }
}