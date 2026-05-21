package splat.lexer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private File file;

    public Lexer(File progFile) {
        this.file = progFile;
    }

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

    private boolean isValidSymbol(int c) {
        if (c == '>' || c == '<' || c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || c == '=' ||
                c == '(' || c == ')' || c == ';' || c == ':' || c =='_' || c == ' ' || c == '\n' ||
                c == '\r' || c == '\t' || c == '"' || c == '.' || c == ',' || c == -1) {
            return true;
        } else {
            return false;
        }
    }

    public List<Token> tokenize() throws LexException {
        List<Token> tokens = new ArrayList<Token>();
        String token = "";
        int line = 1;
        int column = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int ch;
            while ((ch = reader.read()) != -1) {
                if (ch == '"') {
                    if (!token.isEmpty()) {
                        tokens.add(new Token(token.trim(), line, column));
                        token = "";
                    }

                    StringBuilder string = new StringBuilder();
                    string.append('"');
                    int next;
                    boolean isClosed = false;

                    while ((next = reader.read()) != -1) {
                        column++;
                        if (next == '"') {
                            string.append('"');
                            isClosed = true;
                            break;
                        }
                        if (next == '\n' || next == '\\') {
                            throw new LexException("Newlines and backlashes cannot be inside quotes", line, column);
                        }
                        string.append((char) next);
                    }

                    if (!isClosed) {
                        throw new LexException("Quotes cannot be left open in a single line", line, column);
                    }
                    tokens.add(new Token(string.toString(), line, column));
                    continue;
                }

                    if (!isLetter(ch)) {
                        if (!isDigit(ch)) {
                            if (!isValidSymbol(ch)) {
                                throw new LexException("Invalid character", line, column);
                            }
                        }
                    }
                column++;


                if (ch == '(' || ch == ')' || ch == ';' || ch == ',' || ch == '.' || ch == '+' || ch == '-' || ch == '*'
                        || ch == '/' || ch == '%') {
                    if (token.trim().length() > 0) {
                        tokens.add(new Token(token.trim(), line, column));
                        token = "";
                    } tokens.add(new Token(Character.toString((char) ch), line, column));
                    continue;
                }

                if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') {
                    if (token.trim().length() > 0) {
                        tokens.add(new Token(token.trim(), line, column));
                        token = "";
                    }
                    if (ch == '\n') {
                        line++;
                        column = 0;
                    }
                    continue;
                }

                if (ch == '=') {
                    if (token.trim().length() > 0) {
                        tokens.add(new Token(token.trim(), line, column));
                        token = "";
                    }

                    reader.mark(1);
                    int next = reader.read();
                    column++;

                    if (next == '=') {
                        tokens.add(new Token("==", line, column));
                    } else {
                        reader.reset();
                        column--;
                        throw new LexException("Invalid standalone '='", line, column);
                    }

                    continue;
                }

                if (ch == ':') {
                    if (token.trim().length() > 0) {
                        tokens.add(new Token(token.trim(), line, column));
                        token = "";
                    }

                    reader.mark(1);
                    int next = reader.read();
                    column++;

                    if (next == '=') {
                        tokens.add(new Token(":=", line, column));
                    } else {
                        tokens.add(new Token(":", line, column));
                        reader.reset();
                    }

                    continue;
                }

                if (ch == '>') {
                    if (token.trim().length() > 0) {
                        tokens.add(new Token(token.trim(), line, column));
                        token = "";
                    }

                    reader.mark(1);
                    int next = reader.read();
                    column++;

                    if (next == '=') {
                        tokens.add(new Token(">=", line, column));
                    } else {
                        tokens.add(new Token(">", line, column));
                        reader.reset();
                    }

                    continue;
                }

                if (ch == '<') {
                    if (token.trim().length() > 0) {
                        tokens.add(new Token(token.trim(), line, column));
                        token = "";
                    }

                    reader.mark(1);
                    int next = reader.read();
                    column++;

                    if (next == '=') {
                        tokens.add(new Token("<=", line, column));
                    } else {
                        tokens.add(new Token("<", line, column));
                        reader.reset();
                    }

                    continue;
                }
                token = token + (char) ch;
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tokens;
    }
}