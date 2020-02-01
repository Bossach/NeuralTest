package NeuralNetwork;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Eval implements Expression {
    private String source;
    private Expression expr;
    private Map<String, Double> variables = new HashMap<String, Double>();

    public Eval(String source, List<String> variableNames) {
        this.source = source;
        for (String variableName : variableNames) {
            this.variables.put(variableName, 0d);
        }
        expr = Eval.parseExpresion(source, this.variables);
    }

    public boolean setVariable(String varname, double value) {
        if (!this.variables.containsKey(varname)) {
            return false;
        }
        this.variables.replace(varname, value);
        return true;
    }

    public double getVariable(String varname) {
        return this.variables.get(varname);
    }

    public String getSource() {
        return source;
    }

    public double eval() {
        return expr.eval();
    }

    private static Expression parseExpresion(final String str, final Map<String, Double> variables) {
        return new Object() {
            int pos = -1, ch;
            String probableExceptionReason = "";

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
                if (str.trim().length() == 0) throw new RuntimeException("EMPTY EXPRESSION");

                nextChar();
                Expression x = parseConds();
                if (pos < str.length()) throw new RuntimeException("Unexpected: '" + (char) ch + "' " + this.probableExceptionReason);
                return x;
            }

            // Grammar:
            // condits = comps `?` condits `:` condits
            // comps = plusminus | plusminus `>` plusminus | plusminus `<` plusminus | plusminus `=` plusminus | plusminus `==` plusminus
            // plusminus = muldiv | muldiv `+` plusminus | muldiv `-` plusminus
            // muldiv = numpowfnc | muldiv `*` numpowfnc | muldiv `/` numpowfnc
            // numpowfnc = `+` numpowfnc | `-` numpowfnc | `(` condits `)` | number | functionName `(` condits `)` | numpowfnc `^` numpowfnc | var

            Expression parseConds() {
                Expression x = parseComps();
                if (eat('?')) {
                    Expression a = x, b = parseConds(), c;
                    if (!eat(':')) throw new RuntimeException("EXCEPT ':'");
                    c = parseConds();
                    x = () -> a.eval() != 0 ? b.eval() : c.eval();
                } 
                return x;
            }

            Expression parseComps() {
                Expression x = parsePlusMinus();
                if (eat('>')) {
                    boolean strict = !eat('=');
                    Expression a = x, b = parsePlusMinus();
                    if (strict) {
                        x = () -> a.eval() > b.eval() ? 1 : 0;
                    } else {
                        x = () -> a.eval() >= b.eval() ? 1 : 0;
                    }
                } else if (eat('<')) {
                    boolean strict = !eat('=');
                    Expression a = x, b = parsePlusMinus();
                    if (strict) {
                        x = () -> a.eval() < b.eval() ? 1 : 0;
                    } else {
                        x = () -> a.eval() <= b.eval() ? 1 : 0;
                    }
                } else if (eat('=')) {
                    boolean boolcomp = eat('=');
                    Expression a = x, b = parsePlusMinus();
                    if (boolcomp) { // `==` - boolean comp
                        x = () -> (a.eval() != 0) == (b.eval() != 0) ? 1 : 0;
                    } else { // `=` - num comp
                        x = () -> a.eval() == b.eval() ? 1 : 0;
                    }
                }
                if (eat('>') || eat('<') || eat('=')) 
                    this.probableExceptionReason = " probably because multiple '>' '<' '=' and '==' need explicit '( ... )' prioritize";
                return x;
            }

            Expression parsePlusMinus() {
                Expression x = parseMulDiv();
                if (eat('+')) {
                    Expression a = x, b = parsePlusMinus();
                    x = () -> a.eval() + b.eval(); // addition
                } else if (eat('-')) {
                    Expression a = x, b = parsePlusMinus();
                    x = () -> a.eval() - b.eval(); // subtraction
                } 
                return x;
            }

            Expression parseMulDiv() {
                Expression x = parseNumPowFnc();
                if (eat('*')) {
                    Expression a = x, b = parseMulDiv();
                    x = () -> a.eval() * b.eval(); // multiplication
                } else if (eat('/')) {
                    Expression a = x, b = parseMulDiv();
                    x = () -> a.eval() / b.eval(); // division
                } 
                return x;
            }

            Expression parseNumPowFnc() {
                if (eat('+')) return parseNumPowFnc(); // unary plus
                if (eat('-')) {
                    Expression a = parseNumPowFnc(); // unary minus
                    return () -> -a.eval();
                }

                Expression x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    if (!eat(')')) {
                        x = parseConds();
                        if (!eat(')')) throw new RuntimeException("BRACKETS NOT CLOSED");
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
                        if (!eat('(')) throw new RuntimeException("FUNCTION " + func + " '(' expected");
                        if (!eat(')')) {
                            x = parseConds();
                            if (!eat(')')) throw new RuntimeException("BRACKETS NOT CLOSED");
                        } else throw new RuntimeException("EMPTY BRACKETS EXCEPTION");
                        Expression a = x;
                        if (func.equals("sqrt")) x = () -> Math.sqrt(a.eval());
                        else if (func.equals("sin")) x = () -> Math.sin(Math.toRadians(a.eval()));
                        else if (func.equals("cos")) x = () -> Math.cos(Math.toRadians(a.eval()));
                        else if (func.equals("tan")) x = () -> Math.tan(Math.toRadians(a.eval()));
                        else if (func.equals("exp")) x = () -> Math.exp(a.eval());
                        else throw new RuntimeException("Unknown function: " + func);
                    }
                } else {
                    throw new RuntimeException("Unexpected: '" + (char) ch + "' " + this.probableExceptionReason);
                }

                if (eat('^')) {
                    Expression a = x, b = parseNumPowFnc();
                    x = () -> Math.pow(a.eval(), b.eval()); // exponentiation
                }

                return x;
            }
        }.parse();
    }
}