enum tokentype {
    // Keywords
    SAFELET, SAFEPRINT, SAFEIF, SAFEELSE, SAFEWHILE, SAFEINPUT, TYPE_INT, TYPE_STR,

    // Literals
    ID, NUMBER, STRING,

    // Operators
    PLUS, MUL, DIV, MINUS, ASSIGN,

    // Comparison
    GT, LT, GTE, LTE, EQ, NEQ,

    // Grouping
    LPAREN, RPAREN, LBRACE, RBRACE,

    // Special
    EOF
}

public class token {
    tokentype type;
    String value;

    token(tokentype type, String value) {
        this.type = type;
        this.value = value;
    }

    String getCategory() {
        switch (type) {
            case SAFELET:
            case SAFEPRINT:
            case SAFEIF:
            case SAFEELSE:
            case SAFEWHILE:
            case SAFEINPUT:
            case TYPE_INT:
            case TYPE_STR:
                return "KEYWORD";

            case PLUS:
            case MINUS:
            case MUL:
            case DIV:
            case ASSIGN:
                return "OPERATOR";

            case GT:
            case LT:
            case GTE:
            case LTE:
            case EQ:
            case NEQ:
                return "COMPARISON";

            case LPAREN:
            case RPAREN:
            case LBRACE:
            case RBRACE:
                return "GROUPING";

            case ID:
                return "IDENTIFIER";

            case NUMBER:
            case STRING:
                return "LITERAL";

            case EOF:
                return "EOF";

            default:
                return "UNKNOWN";
        }
    }

    static tokentype parseTokenType(String value) {
        switch (value) {
            case "Safelet": return tokentype.SAFELET;
            case "Safeprint": return tokentype.SAFEPRINT;
            case "Safeif": return tokentype.SAFEIF;
            case "Safeelse": return tokentype.SAFEELSE;
            case "Safewhile": return tokentype.SAFEWHILE;
            case "safeinput": return tokentype.SAFEINPUT;
            case "int": return tokentype.TYPE_INT;
            case "str": return tokentype.TYPE_STR;
            case "+": return tokentype.PLUS;
            case "-": return tokentype.MINUS;
            case "*": return tokentype.MUL;
            case "/": return tokentype.DIV;
            case "=": return tokentype.ASSIGN;
            case "==": return tokentype.EQ;
            case "!=": return tokentype.NEQ;
            case "<": return tokentype.LT;
            case ">": return tokentype.GT;
            case "<=": return tokentype.LTE;
            case ">=": return tokentype.GTE;
            case "(": return tokentype.LPAREN;
            case ")": return tokentype.RPAREN;
            case "{": return tokentype.LBRACE;
            case "}": return tokentype.RBRACE;
            default:
                if (value.matches("\\d+")) return tokentype.NUMBER;
                if (value.matches("[a-zA-Z_][a-zA-Z0-9_]*")) return tokentype.ID;
                return tokentype.EOF;
        }
    }
}
