import NeuralNetwork.*;

import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {

    }

    public static void testEval() {
        List<String> params = new ArrayList<>();
        params.add("x");
        Eval eval = new Eval("x", params);
        Scanner scan = new Scanner(System.in);
        for (;;) {
            try {
                eval.setVariable("x", scan.nextDouble());
                System.out.println("Result ( " + eval.getSource() + " )(x= " + eval.getVariable("x") + " ): " + eval.eval());
                System.out.println("Deriv  ( " + eval.getSource() + " )(x= " + eval.getVariable("x") + " ): " + eval.deriv());
            } catch (Exception e) {

                String newExpr = scan.nextLine();
                if (newExpr.trim().equals("")) continue;
                if (newExpr.equals("exit")) {
                    return;
                }

                try {
                    eval = new Eval(newExpr, params);
                    System.out.println("Func changed to (" + eval.getSource() + ").");                   
                } catch (Exception ee) {
                    System.out.println("Error: " + ee);
                }
            }
        }
    }

    public static void testNeural() {

        
        NeuralNetworkTrainable netw = new NeuralNetworkTrainable(new int[]{1});
        Scanner scan = new Scanner(System.in);
        
        for (; ; ) {
            try {
                System.out.println("Input:");
                System.out.println(Arrays.toString( netw.process(new double[]{scan.nextDouble()}) ));
            } catch (Exception e) {
                try {
                    String cmd = scan.nextLine().toLowerCase();
                    if (cmd.startsWith("save ")) {
                        save(netw.export(), cmd.replaceFirst("save ", ""));
                    } else if (cmd.startsWith("load ")) {
                        netw = new NeuralNetworkTrainable( (NetworkData) load(cmd.replaceFirst("load ", "")) );
                    } else if (cmd.equals("create")) {
                        System.out.println("Layers count?");
                        int[] layers = new int[scan.nextInt()];

                        for (int i = 0; i < layers.length; i++) {
                            System.out.println("Neuronst count on " + i + " layer ( 0 = input; " + (layers.length - 1) + " = output)");
                            layers[i] = scan.nextInt();
                        }
                        String func = scan.nextLine();
                        if (func.trim().length() == 0) {
                            func = "1 / ( 1 + exp( -x ) )";
                        }
                        netw = new NeuralNetworkTrainable(layers, func);

                    } else if (cmd.equals("train")) {
                        double[][][] array = new double[][][]{
                            {{-1}, { 1 / ( 1 + Math.exp( 1) ) }},
                            {{-0.5}, { 1 / ( 1 + Math.exp( 0.5) ) }},
                            {{0}, { 1 / ( 1 + Math.exp( - 0) ) }},
                            {{0.5}, { 1 / ( 1 + Math.exp( - 0.5) ) }},
                            {{1}, { 1 / ( 1 + Math.exp( - 1) ) }},
                        };

                        System.out.println("Iteraitons:");
                        int cnt = scan.nextInt();

                        System.out.println("k:");
                        double k = scan.nextDouble(); // 0.1

                        long starttime = System.currentTimeMillis();
                        netw.multTrain(array, cnt, k);
                        long endtime = System.currentTimeMillis();

                        System.out.println("Обучение окончено. Времени затрачено: " + (endtime - starttime));
                    } else if (cmd.equals("print")) {
                        System.out.println(netw);
                    } else if (cmd.equals("exit")) {
                        return;
                    } else {
                        System.out.println("Unknown cmd '" + cmd + "'");
                    }
                } catch (Exception ee) {
                    System.out.println(ee);
                }
            }
        }

    }

    private static void save(Object obj, String filename) {
        try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(filename))) {
            stream.writeObject(obj);
        } catch (Exception e) {
            System.err.println("Save error: " + e);
        }
    }

    private static Object load(String filename) throws Exception {
        final FileInputStream fis = new FileInputStream(filename);
        final ObjectInputStream ois = new ObjectInputStream(fis);
        Object ret = ois.readObject();
        ois.close();
        return ret;
    }

}
