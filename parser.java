import java.util.*;

class parser {
    private List<token> tokens;
    private int pos = 0;
    private token currentToken;

    private Map<String, Object> memory = new HashMap<>();
    private Map<String, String> varTypes = new HashMap<>();   // "int" or "str"
    private Map<String, Integer> varLimits = new HashMap<>();  // buffer size limit
    private StringBuilder output = new StringBuilder();

    parser(List<token> tokens) {
        this.tokens = tokens;
        currentToken = tokens.get(pos);
    }

    private void eat(tokentype type) {
        if (currentToken.type == type) {
            pos++;
            if (pos < tokens.size()) {
                currentToken = tokens.get(pos);
            }
        } else {
            throw new RuntimeException("Unexpected token: " + currentToken.type + ", expected: " + type);
        }
    }

    private token peek() {
        if (pos + 1 < tokens.size()) {
            return tokens.get(pos + 1);
        }
        return new token(tokentype.EOF, "");
    }

    // factor → NUMBER | ID | LPAREN expr RPAREN | MINUS factor
    private int factor() {
        token t = currentToken;

        if (t.type == tokentype.NUMBER) {
            eat(tokentype.NUMBER);
            return Integer.parseInt(t.value);
        }

        if (t.type == tokentype.ID) {
            eat(tokentype.ID);
            if (!memory.containsKey(t.value))
                throw new RuntimeException("Undefined variable: " + t.value);
            Object val = memory.get(t.value);
            if (!(val instanceof Integer)) {
                throw new RuntimeException("Type mismatch: Expected integer but got string for variable " + t.value);
            }
            return (Integer) val;
        }

        // Parenthesized expression
        if (t.type == tokentype.LPAREN) {
            eat(tokentype.LPAREN);
            int result = expr();
            eat(tokentype.RPAREN);
            return result;
        }

        // Unary minus
        if (t.type == tokentype.MINUS) {
            eat(tokentype.MINUS);
            return -factor();
        }

        throw new RuntimeException("Invalid factor: " + t.type);
    }

    // term → factor ((*|/) factor)*
    private int term() {
        int result = factor();

        while (currentToken.type == tokentype.MUL ||
               currentToken.type == tokentype.DIV) {

            if (currentToken.type == tokentype.MUL) {
                eat(tokentype.MUL);
                result *= factor();
            } else {
                eat(tokentype.DIV);
                int divisor = factor();
                if (divisor == 0) {
                    throw new RuntimeException("Division by zero error");
                }
                result /= divisor;
            }
        }
        return result;
    }

    // expr → term ((+|-) term)*
    private int expr() {
        int result = term();

        while (currentToken.type == tokentype.PLUS ||
               currentToken.type == tokentype.MINUS) {

            if (currentToken.type == tokentype.PLUS) {
                eat(tokentype.PLUS);
                result += term();
            } else {
                eat(tokentype.MINUS);
                result -= term();
            }
        }
        return result;
    }

    // condition → expr ((> | < | >= | <= | == | !=) expr)?
    private boolean condition() {
        int left = expr();

        if (currentToken.type == tokentype.GT) {
            eat(tokentype.GT);
            return left > expr();
        } else if (currentToken.type == tokentype.LT) {
            eat(tokentype.LT);
            return left < expr();
        } else if (currentToken.type == tokentype.GTE) {
            eat(tokentype.GTE);
            return left >= expr();
        } else if (currentToken.type == tokentype.LTE) {
            eat(tokentype.LTE);
            return left <= expr();
        } else if (currentToken.type == tokentype.EQ) {
            eat(tokentype.EQ);
            return left == expr();
        } else if (currentToken.type == tokentype.NEQ) {
            eat(tokentype.NEQ);
            return left != expr();
        }

        // No comparison operator — treat non-zero as true
        return left != 0;
    }

    // block → LBRACE statement* RBRACE
    private void block() {
        eat(tokentype.LBRACE);
        while (currentToken.type != tokentype.RBRACE && currentToken.type != tokentype.EOF) {
            statement();
        }
        eat(tokentype.RBRACE);
    }

    // Skip a block without executing (for false branches)
    private void skipBlock() {
        eat(tokentype.LBRACE);
        int depth = 1;
        while (depth > 0 && currentToken.type != tokentype.EOF) {
            if (currentToken.type == tokentype.LBRACE) depth++;
            if (currentToken.type == tokentype.RBRACE) depth--;
            if (depth > 0) {
                pos++;
                currentToken = tokens.get(pos);
            }
        }
        eat(tokentype.RBRACE);
    }

    // statement
    private void statement() {
        // Safelet x = expr | (int/str) safeinput(...)
        if (currentToken.type == tokentype.SAFELET) {
            eat(tokentype.SAFELET);
            String varName = currentToken.value;
            eat(tokentype.ID);
            eat(tokentype.ASSIGN);

            // Check for typed safeinput: (int/str) safeinput(...)
            if (currentToken.type == tokentype.LPAREN && 
                (peek().type == tokentype.TYPE_INT || peek().type == tokentype.TYPE_STR)) {
                
                eat(tokentype.LPAREN);
                tokentype type = currentToken.type;
                eat(type); // TYPE_INT or TYPE_STR
                eat(tokentype.RPAREN);

                if (currentToken.type != tokentype.SAFEINPUT) {
                    throw new RuntimeException("Syntax Error: expected safeinput after type cast");
                }
                eat(tokentype.SAFEINPUT);
                eat(tokentype.LPAREN);

                if (type == tokentype.TYPE_STR) {
                    if (currentToken.type == tokentype.NUMBER) {
                        // (str) safeinput(5) → declare with buffer limit, no value yet
                        int limit = Integer.parseInt(currentToken.value);
                        eat(tokentype.NUMBER);
                        eat(tokentype.RPAREN);

                        varTypes.put(varName, "str");
                        varLimits.put(varName, limit);
                        memory.put(varName, ""); // empty until assigned

                    } else if (currentToken.type == tokentype.STRING) {
                        // (str) safeinput("Delhi") → assign default string directly
                        String defStr = currentToken.value;
                        eat(tokentype.STRING);
                        eat(tokentype.RPAREN);

                        varTypes.put(varName, "str");
                        varLimits.put(varName, defStr.length()); // limit = length of default
                        memory.put(varName, defStr);
                        output.append("Input accepted: ").append(defStr).append("\n");

                    } else {
                        throw new RuntimeException("Syntax Error: str safeinput expects number limit or default string");
                    }

                } else if (type == tokentype.TYPE_INT) {
                    if (currentToken.type == tokentype.NUMBER) {
                        // (int) safeinput(3) → declare with digit limit, no value yet
                        int limit = Integer.parseInt(currentToken.value);
                        eat(tokentype.NUMBER);
                        eat(tokentype.RPAREN);

                        varTypes.put(varName, "int");
                        varLimits.put(varName, limit);
                        memory.put(varName, 0); // default until assigned

                    } else {
                        throw new RuntimeException("Syntax Error: int safeinput expects a number limit");
                    }
                } else {
                    throw new RuntimeException("Type not specified");
                }

            } else {
                // Normal: Safelet x = expr
                int value = expr();
                memory.put(varName, value);
                varTypes.put(varName, "int");
            }
        }

        // Safeprint expr | string_var
        else if (currentToken.type == tokentype.SAFEPRINT) {
            eat(tokentype.SAFEPRINT);
            
            // Check if printing a string variable
            if (currentToken.type == tokentype.ID && memory.containsKey(currentToken.value) 
                    && memory.get(currentToken.value) instanceof String) {
                String varName = currentToken.value;
                eat(tokentype.ID);
                output.append(memory.get(varName)).append("\n");
            } else {
                int value = expr();
                output.append(value).append("\n");
            }
        }

        // Safeif condition { ... } Safeelse { ... }
        else if (currentToken.type == tokentype.SAFEIF) {
            eat(tokentype.SAFEIF);
            boolean cond = condition();

            if (cond) {
                block();
                // Skip else block if present
                if (currentToken.type == tokentype.SAFEELSE) {
                    eat(tokentype.SAFEELSE);
                    skipBlock();
                }
            } else {
                skipBlock();
                // Execute else block if present
                if (currentToken.type == tokentype.SAFEELSE) {
                    eat(tokentype.SAFEELSE);
                    block();
                }
            }
        }

        // Safewhile condition { ... }
        else if (currentToken.type == tokentype.SAFEWHILE) {
            eat(tokentype.SAFEWHILE);
            int condStart = pos; // Save position to loop back to

            while (true) {
                // Reset to condition start
                pos = condStart;
                currentToken = tokens.get(pos);

                boolean cond = condition();
                if (!cond) {
                    skipBlock();
                    break;
                }
                block();
            }
        }

        // Variable reassignment: x = expr  OR  x = "string"
        else if (currentToken.type == tokentype.ID) {
            String varName = currentToken.value;
            eat(tokentype.ID);
            eat(tokentype.ASSIGN);
            
            if (!memory.containsKey(varName)) {
                throw new RuntimeException("Undefined variable: " + varName + ". Use 'Safelet' to declare first.");
            }

            // String assignment: x = "value"
            if (currentToken.type == tokentype.STRING) {
                String strVal = currentToken.value;
                eat(tokentype.STRING);

                // Check type compatibility
                String declaredType = varTypes.get(varName);
                if (declaredType != null && declaredType.equals("int")) {
                    throw new RuntimeException("Type mismatch: cannot assign string to int variable " + varName);
                }

                // Check buffer overflow
                if (varLimits.containsKey(varName)) {
                    int limit = varLimits.get(varName);
                    if (strVal.length() > limit) {
                        throw new RuntimeException("Error: characters are more than " + limit + ", buffer overflow protected");
                    }
                }

                memory.put(varName, strVal);
                output.append("Input accepted: ").append(strVal).append("\n");

            } else {
                // Integer expression assignment
                int value = expr();

                // Check type compatibility
                String declaredType = varTypes.get(varName);
                if (declaredType != null && declaredType.equals("str")) {
                    throw new RuntimeException("Type mismatch: cannot assign integer to str variable " + varName);
                }

                // Check digit limit for int safeinput variables
                if (varLimits.containsKey(varName) && declaredType != null && declaredType.equals("int")) {
                    int limit = varLimits.get(varName);
                    if (String.valueOf(Math.abs(value)).length() > limit) {
                        throw new RuntimeException("Error: invalid integer or size exceeded");
                    }
                }

                memory.put(varName, value);
            }
        }

        else {
            throw new RuntimeException("Invalid statement starting with: " + currentToken.type + " (" + currentToken.value + ")");
        }
    }

    // program
    void parse() {
        while (currentToken.type != tokentype.EOF) {
            statement();
        }
    }

    String getOutput() {
        return output.toString();
    }
}