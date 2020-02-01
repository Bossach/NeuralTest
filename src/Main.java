import NeuralNetwork.*;

import java.io.*;
import java.util.*;

public class Main {

    public static void mainx(String[] args) {
        List<String> params = new ArrayList<>();
        params.add("x");
        Eval eval = new Eval("x", params);
        Scanner scan = new Scanner(System.in);
        for (;;) {
            try {
                eval.setVariable("x", scan.nextDouble());
                System.out.println("Result ( " + eval.getSource() + " )(x= " + eval.getVariable("x") + " ): " + eval.eval());
            } catch (Exception e) {

                String newExpr = scan.nextLine();
                if (newExpr.trim().equals("")) continue;
                if (newExpr.equals("exit")) {
                    scan.close();
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

    public static void main(String[] args) {

        int inputs = 1;

        int outputs = 1;

        double[][][] array = new double[200][][];

        for (int i = 0; i < array.length; i += 2) {
            array[i] = new double[][]{{i}, {i}};
            array[i + 1] = new double[][]{{-i}, {i}};
            System.out.println(i);
        }



        NeuralNetworkTrainable netw = new NeuralNetworkTrainable(new int[]{inputs, 3, 3, outputs}, "1 / ( 1 + exp( -x ) )", "x*(1-x)");


        long starttime = System.currentTimeMillis();
        netw.multTrain(array , 3000000, 0.1);
        long endtime = System.currentTimeMillis();

        System.out.println("Обучение окончено. Времени затрачено: " + (endtime - starttime));


        Scanner scan = new Scanner(System.in);
        for (; ; ) {
            try {
                System.out.println(Arrays.toString(netw.process(new double[]{scan.nextDouble()})));
            } catch (Exception e) {
                String cmd = scan.nextLine().toLowerCase();
                if (cmd.startsWith("save ")) {
                    save(netw.export(), cmd.replaceFirst("save ", ""));
                } else if (cmd.startsWith("load ")) {
                    try {
                        netw = new NeuralNetworkTrainable( (NetworkData) load(cmd.replaceFirst("load ", "")) );
                    } catch (Exception ee) {
                        System.out.println("Error loading network: " + ee);
                    }
                } else if (cmd.equals("exit")) {
                    scan.close();
                    return;
                } else {
                    System.out.println("Unknown cmd '" + cmd + "'");
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
