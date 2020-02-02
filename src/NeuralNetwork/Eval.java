package NeuralNetwork;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

interface EvalNode {
    double eval();
    double deriv();
}

interface Lambda {
    double run();
}

public class Eval {
    private String source;
    private EvalNode expr;
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

    public double deriv() {
        return expr.deriv();
    }

    private static EvalNode generateNode(Lambda a, Lambda b) {
        return new EvalNode() {
            public double eval() {
                return a.run();
            }
            public double deriv() {
                return b.run();
            }
        };
    }

    private static EvalNode parseExpresion(final String str, final Map<String, Double> variables) {
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

            EvalNode parse() {
                if (str.trim().length() == 0) throw new RuntimeException("EMPTY EXPRESSION");

                nextChar();
                EvalNode x = parseConds();
                if (pos < str.length()) throw new RuntimeException("Unexpected: '" + (char) ch + "' " + this.probableExceptionReason);
                return x;
            }

            // Grammar:
            // condits = comps `?` condits `:` condits
            // comps = plusminus | plusminus `>` plusminus | plusminus `<` plusminus | plusminus `=` plusminus | plusminus `==` plusminus
            // plusminus = muldiv | muldiv `+` plusminus | muldiv `-` plusminus
            // muldiv = numpowfnc | muldiv `*` numpowfnc | muldiv `/` numpowfnc
            // numpowfnc = `+` numpowfnc | `-` numpowfnc | `(` condits `)` | number | functionName `(` condits `)` | numpowfnc `^` numpowfnc | var

            EvalNode parseConds() {
                EvalNode x = parseComps();
                if (eat('?')) {
                    EvalNode a = x, b = parseConds(), c;
                    if (!eat(':')) throw new RuntimeException("EXCEPT ':'");
                    c = parseConds();
                    // x = () -> a.eval() != 0 ? b.eval() : c.eval();
                    x = generateNode(
                        () -> a.eval() != 0 ? b.eval() : c.eval(), 
                        () -> a.eval() != 0 ? b.deriv() : c.deriv()
                    );
                } 
                return x;
            }

            EvalNode parseComps() {
                EvalNode x = parsePlusMinus();
                if (eat('>')) {
                    boolean strict = !eat('=');
                    EvalNode a = x, b = parsePlusMinus();
                    if (strict) {
                        // x = () -> a.eval() > b.eval() ? 1 : 0;
                        x = generateNode(
                            () -> a.eval() > b.eval() ? 1 : 0, 
                            () -> 0
                        );
                    } else {
                        // x = () -> a.eval() >= b.eval() ? 1 : 0;
                        x = generateNode(
                            () -> a.eval() >= b.eval() ? 1 : 0, 
                            () -> 0
                        );
                    }
                } else if (eat('<')) {
                    boolean strict = !eat('=');
                    EvalNode a = x, b = parsePlusMinus();
                    if (strict) {
                        // x = () -> a.eval() < b.eval() ? 1 : 0;
                        x = generateNode(
                            () -> a.eval() < b.eval() ? 1 : 0, 
                            () -> 0
                        );
                    } else {
                        // x = () -> a.eval() <= b.eval() ? 1 : 0;
                        x = generateNode(
                            () -> a.eval() <= b.eval() ? 1 : 0, 
                            () -> 0
                        );
                    }
                } else if (eat('=')) {
                    boolean boolcomp = eat('=');
                    EvalNode a = x, b = parsePlusMinus();
                    if (boolcomp) { // `==` - boolean comp
                        // x = () -> (a.eval() != 0) == (b.eval() != 0) ? 1 : 0;
                        x = generateNode(
                            () -> (a.eval() != 0) == (b.eval() != 0) ? 1 : 0,
                            () -> 0
                        );
                    } else { // `=` - num comp
                        // x = () -> a.eval() == b.eval() ? 1 : 0;
                        x = generateNode(
                            () -> a.eval() == b.eval() ? 1 : 0, 
                            () -> 0
                        );
                    }
                }
                if (eat('>') || eat('<') || eat('=')) 
                    this.probableExceptionReason = " probably because multiple '>' '<' '=' and '==' need explicit '( ... )' prioritize";
                return x;
            }

            EvalNode parsePlusMinus() {
                EvalNode x = parseMulDiv();
                if (eat('+')) {
                    EvalNode a = x, b = parsePlusMinus();
                    // x = () -> a.eval() + b.eval(); // addition
                    x = generateNode(
                        () -> a.eval() + b.eval(), 
                        () -> a.deriv() + b.deriv()
                    );
                } else if (eat('-')) {
                    EvalNode a = x, b = parsePlusMinus();
                    // x = () -> a.eval() - b.eval(); // subtraction
                    x = generateNode(
                        () -> a.eval() - b.eval(), 
                        () -> a.deriv() - b.deriv()
                    );
                } 
                return x;
            }

            EvalNode parseMulDiv() {
                EvalNode x = parseNumPowFnc();
                if (eat('*')) {
                    EvalNode a = x, b = parseMulDiv();
                    // x = () -> a.eval() * b.eval(); // multiplication
                    x = generateNode(
                        () -> a.eval() * b.eval(), 
                        () -> a.deriv() * b.eval() + a.eval() * b.deriv()
                    );
                } else if (eat('/')) {
                    EvalNode a = x, b = parseMulDiv();
                    // x = () -> a.eval() / b.eval(); // division
                    x = generateNode(
                        () -> a.eval() / b.eval(), 
                        () -> (a.deriv() * b.eval() - a.eval() * b.deriv()) / Math.pow(b.eval(), 2)
                    );
                } 
                return x;
            }

            EvalNode parseNumPowFnc() {
                if (eat('+')) return parseNumPowFnc(); // unary plus
                if (eat('-')) {
                    EvalNode a = parseNumPowFnc(); // unary minus
                    // return () -> -a.eval();
                    return generateNode(
                        () -> -a.eval(), 
                        () -> -a.deriv()
                    );
                }

                EvalNode x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    if (!eat(')')) {
                        x = parseConds();
                        if (!eat(')')) throw new RuntimeException("BRACKETS NOT CLOSED");
                    } else throw new RuntimeException("EMPTY BRACKETS EXCEPTION");
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    double y = Double.parseDouble(str.substring(startPos, this.pos));
                    // x = () -> y;
                    x = generateNode(
                        () -> y, 
                        () -> 0
                    );
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    if (variables.containsKey(func)) {
                        // x = () -> variables.get(func);
                        x = generateNode(
                            () -> variables.get(func), 
                            () -> 1
                        );
                    } else {
                        if (!eat('(')) throw new RuntimeException("FUNCTION " + func + " '(' expected");
                        if (!eat(')')) {
                            x = parseConds();
                            if (!eat(')')) throw new RuntimeException("BRACKETS NOT CLOSED");
                        } else throw new RuntimeException("EMPTY BRACKETS EXCEPTION");
                        EvalNode a = x;
                        // if (func.equals("sqrt")) x = () -> Math.sqrt(a.eval());
                        if (func.equals("sqrt")) x = generateNode(
                            () -> Math.sqrt(a.eval()), 
                            () -> (1 / (2 * Math.sqrt(a.eval()))) * a.deriv()
                        );
                        // else if (func.equals("sin")) x = () -> Math.sin(Math.toRadians(a.eval()));
                        else if (func.equals("sin")) x = generateNode(
                            () -> Math.sin(Math.toRadians(a.eval())), 
                            () -> Math.cos(Math.toRadians(a.eval())) * a.deriv()
                        );
                        // else if (func.equals("cos")) x = () -> Math.cos(Math.toRadians(a.eval()));
                        else if (func.equals("cos")) x = generateNode(
                            () -> Math.cos(Math.toRadians(a.eval())), 
                            () -> -Math.sin(Math.toRadians(a.eval())) * a.deriv()
                        );
                        // else if (func.equals("tan")) x = () -> Math.tan(Math.toRadians(a.eval()));
                        else if (func.equals("tan")) x = generateNode(
                            () -> Math.tan(Math.toRadians(a.eval())), 
                            () -> ( 1 / Math.pow(Math.cos(Math.toRadians(a.eval())), 2) ) * a.deriv()
                        );
                        // else if (func.equals("exp")) x = () -> Math.exp(a.eval());
                        else if (func.equals("exp")) x = generateNode(
                            () -> Math.exp(a.eval()), 
                            () -> Math.exp(a.eval()) * a.deriv()
                        );
                        else throw new RuntimeException("Unknown function: " + func);
                    }
                } else {
                    throw new RuntimeException("Unexpected: '" + (char) ch + "' " + this.probableExceptionReason);
                }

                if (eat('^')) {
                    EvalNode a = x, b = parseNumPowFnc();
                    // x = () -> Math.pow(a.eval(), b.eval()); // exponentiation
                    x = generateNode(
                        () -> Math.pow(a.eval(), b.eval()), 
                        () -> {
                            double A = a.eval();
                            double B = b.eval();
                            if (A == 0 && B != 0 || A != 0 && B == 0) {
                                return 0;
                            }
                            double Ad = a.deriv();
                            double Bd = b.deriv();
                            return Math.pow(A, B - 1) * (B * Ad + A * Math.log(A) * Bd);
                        }
                    );
                }

                return x;
            }
        }.parse();
    }
}


