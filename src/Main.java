import NeuralNetwork.*;

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        int inputs = 1;

        int outputs = 8;

        double[][][] array = new double[256][][];

        for (int i = 0; i < array.length; i++) {
            array[i] = new double[][]{{i/1000}, {(i >> 7) % 2, (i >> 6) % 2, (i >> 5) % 2, (i >> 4) % 2, (i >> 3) % 2, (i >> 2) % 2, (i >> 1) % 2, i % 2}};
        }



        NeuralNetworkTrainable netw = new NeuralNetworkTrainable(new int[]{inputs, 20, 20, 16, 10, outputs});


        long starttime = System.currentTimeMillis();
        netw.multTrain(array , 30000000, 0.3);
        long endtime = System.currentTimeMillis();

        System.out.println("Обучение окончено. Времени затрачено: " + (endtime - starttime));


        Scanner scan = new Scanner(System.in);
        for (; ; ) {
            try {
                System.out.println(Arrays.toString(netw.process(new double[]{scan.nextDouble()})));
            } catch (Exception e) {
                System.err.println("error");
                scan.nextLine();
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
        return ois.readObject();
    }

}
