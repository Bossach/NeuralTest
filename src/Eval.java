import java.util.Map;

interface Expression {
    double eval();
}

public class Eval {

    public static Expression getEval(final String str, Map<String, Double> variables) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            Expression parse() {
                if ( str.trim().length() == 0 ) throw new RuntimeException("EMPTY FUNCTION");
                bracketsCheck();
                nextChar();
                Expression x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            void bracketsCheck() {
                int bracketCounter = 0;
                for (int i = 0; i < str.length(); i++) {
                    if ( str.charAt(i) == '(' ) bracketCounter++;
                    if ( str.charAt(i) == ')' ) bracketCounter--;
                    if ( bracketCounter < 0 ) throw new RuntimeException("INVALID BRACKETS ORDER and/or COUNT");
                }
                if ( bracketCounter != 0 ) throw new RuntimeException("INVALID BRACKETS COUNT");
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            Expression parseExpression() {
                Expression x = parseTerm();
                for (; ; ) {
                    if (eat('+')) {
                        Expression a = x, b = parseTerm();
                        x = () -> a.eval() + b.eval(); // addition
                    }
                    else if (eat('-')) {
                        // x = a + b
                        Expression a = x, b = parseTerm();
                        x = () -> a.eval() - b.eval(); // subtraction
                    }
                    else return x;
                }
            }

            Expression parseTerm() {
                Expression x = parseFactor();
                for (; ; ) {
                    if (eat('*')) {
                        Expression a = x, b = parseFactor();
                        x = () -> a.eval() * b.eval(); // multiplication
                    }
                    else if (eat('/')) {
                        Expression a = x, b = parseFactor();
                        x = () -> a.eval() / b.eval(); // division
                    }
                    else return x;
                }
            }

            Expression parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) {
                    Expression a = parseFactor(); // unary minus
                    return () -> -a.eval();
                }

                Expression x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    if ( !eat(')') ) {
                        x = parseExpression();
                        eat(')');
                    } else throw new RuntimeException("EMPTY BRACKETS EXCEPTION");
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    double y = Double.parseDouble(str.substring(startPos, this.pos));
                    x = () -> y;
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    if (variables.containsKey(func)) {
                        x = () -> variables.get(func);
                    } else {
                        x = parseFactor();
                        Expression a = x;
                        if (func.equals("sqrt")) x = () -> Math.sqrt(a.eval());
                        else if (func.equals("sin")) x = () -> Math.sin(Math.toRadians(a.eval()));
                        else if (func.equals("cos")) x = () -> Math.cos(Math.toRadians(a.eval()));
                        else if (func.equals("tan")) x = () -> Math.tan(Math.toRadians(a.eval()));
                        else if (func.equals("exp")) x = () -> Math.exp(a.eval());
                        else throw new RuntimeException("Unknown function: " + func);
                    }
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^')) {
                    Expression a = x, b = parseFactor();
                    x = () -> Math.pow(a.eval(), b.eval()); // exponentiation
                }

                return x;
            }
        }.parse();
    }
}