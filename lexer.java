import java.util.*;

public class lexer {
    private String input;
    private int pos = 0;
    private char currChar;

    lexer(String input) {
        this.input = input;
        currChar = input.length() > 0 ? input.charAt(0) : '\0';
    }

    private void advance() {
        pos++;
        currChar = input.length() > pos ? input.charAt(pos) : '\0';
    }

    private char peek() {
        int nextPos = pos + 1;
        return input.length() > nextPos ? input.charAt(nextPos) : '\0';
    }

    private void skipWhiteSpace() {
        while (Character.isWhitespace(currChar)) advance();
    }

    private void skipComment() {
        // Skip from // to end of line
        while (currChar != '\n' && currChar != '\0') advance();
    }

    private String number() {
        StringBuilder sb = new StringBuilder();
        while (Character.isDigit(currChar)) {
            sb.append(currChar);
            advance();
        }
        return sb.toString();
    }

    private String identifier() {
        StringBuilder sb = new StringBuilder();
        while (Character.isLetterOrDigit(currChar) || currChar == '_') {
            sb.append(currChar);
            advance();
        }
        return sb.toString();
    }

    List<token> tokenize() {
        List<token> tokens = new ArrayList<>();

        while (currChar != '\0') {
            // Skip whitespace
            if (Character.isWhitespace(currChar)) {
                skipWhiteSpace();
                continue;
            }

            // Skip comments: // until end of line
            if (currChar == '/' && peek() == '/') {
                skipComment();
                continue;
            }

            // Numbers
            if (Character.isDigit(currChar)) {
                tokens.add(new token(tokentype.NUMBER, number()));
                continue;
            }

            // Strings
            if (currChar == '"') {
                advance();
                StringBuilder str = new StringBuilder();
                while (currChar != '"' && currChar != '\0') {
                    str.append(currChar);
                    advance();
                }
                if (currChar == '"') advance();
                tokens.add(new token(tokentype.STRING, str.toString()));
                continue;
            }

            // Identifiers and keywords
            if (Character.isLetter(currChar) || currChar == '_') {
                String id = identifier();
                switch (id) {
                    case "Safelet":
                        tokens.add(new token(tokentype.SAFELET, id));
                        break;
                    case "Safeprint":
                        tokens.add(new token(tokentype.SAFEPRINT, id));
                        break;
                    case "Safeif":
                        tokens.add(new token(tokentype.SAFEIF, id));
                        break;
                    case "Safeelse":
                        tokens.add(new token(tokentype.SAFEELSE, id));
                        break;
                    case "Safewhile":
                        tokens.add(new token(tokentype.SAFEWHILE, id));
                        break;
                    case "safeinput":
                        tokens.add(new token(tokentype.SAFEINPUT, id));
                        break;
                    case "int":
                        tokens.add(new token(tokentype.TYPE_INT, id));
                        break;
                    case "str":
                        tokens.add(new token(tokentype.TYPE_STR, id));
                        break;
                    default:
                        tokens.add(new token(tokentype.ID, id));
                        break;
                }
                continue;
            }

            // Two-character operators (check before single-char)
            if (currChar == '>' && peek() == '=') {
                tokens.add(new token(tokentype.GTE, ">="));
                advance(); advance();
                continue;
            }
            if (currChar == '<' && peek() == '=') {
                tokens.add(new token(tokentype.LTE, "<="));
                advance(); advance();
                continue;
            }
            if (currChar == '=' && peek() == '=') {
                tokens.add(new token(tokentype.EQ, "=="));
                advance(); advance();
                continue;
            }
            if (currChar == '!' && peek() == '=') {
                tokens.add(new token(tokentype.NEQ, "!="));
                advance(); advance();
                continue;
            }

            // Single-character tokens
            switch (currChar) {
                case '+':
                    tokens.add(new token(tokentype.PLUS, "+"));
                    break;
                case '-':
                    tokens.add(new token(tokentype.MINUS, "-"));
                    break;
                case '*':
                    tokens.add(new token(tokentype.MUL, "*"));
                    break;
                case '/':
                    tokens.add(new token(tokentype.DIV, "/"));
                    break;
                case '=':
                    tokens.add(new token(tokentype.ASSIGN, "="));
                    break;
                case '>':
                    tokens.add(new token(tokentype.GT, ">"));
                    break;
                case '<':
                    tokens.add(new token(tokentype.LT, "<"));
                    break;
                case '(':
                    tokens.add(new token(tokentype.LPAREN, "("));
                    break;
                case ')':
                    tokens.add(new token(tokentype.RPAREN, ")"));
                    break;
                case '{':
                    tokens.add(new token(tokentype.LBRACE, "{"));
                    break;
                case '}':
                    tokens.add(new token(tokentype.RBRACE, "}"));
                    break;
                default:
                    throw new RuntimeException("Invalid Character: " + currChar);
            }
            advance();
        }

        tokens.add(new token(tokentype.EOF, ""));
        return tokens;
    }
}
