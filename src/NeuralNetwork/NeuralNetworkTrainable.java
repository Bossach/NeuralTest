package NeuralNetwork;

import java.util.Arrays;
import java.util.Random;

public class NeuralNetworkTrainable extends NeuralNetwork {
    private static Random rand = new Random();

    public NeuralNetworkTrainable(double[][][] networkWeights, String funcString) {
        super(networkWeights, funcString);
    }

    public NeuralNetworkTrainable(int[] layerSizes, String funcString) {
        this(generateRandomWeights(layerSizes), funcString);
    }

    public NeuralNetworkTrainable(int[] layerSizes) {
        this(layerSizes, defaultFunctionStr);
    }

    public NeuralNetworkTrainable(NetworkData data) {
        super(data);
    }

    private static double[][][] generateRandomWeights(int[] layerSizes) {
        double[][][] networkWeights = new double[layerSizes.length - 1][][];

        for (int i = 1; i < layerSizes.length; i++) {
            double[][] layerWeights = new double[layerSizes[i]][layerSizes[i - 1]];
            for (int j = 0; j < layerSizes[i]; j++) {
                for (int k = 0; k < layerSizes[i - 1]; k++) {
                    layerWeights[j][k] = rand.nextDouble() * 2 - 1;
                }
            }
            networkWeights[i - 1] = layerWeights;
        }
        return networkWeights;
    }

    public void mutate(double k) {
        for (int layer = 0; layer < weights.length; layer++) {
            for (int neuron = 0; neuron < weights[layer].length; neuron++) {
                for (int weight = 0; weight < weights[layer][neuron].length; weight++) {
                    k *= rand.nextDouble() * 2 - 1;
                    if (weights[layer][neuron][weight] + k >= 1 || weights[layer][neuron][weight] + k <= -1) k *= -1;
                    weights[layer][neuron][weight] += k;
                }
            }
        }
    }

    public void train(double[] input, double[] correctResult, double k) {

        if (input.length != inputSize || correctResult.length != weights[weights.length - 1].length ) {
            System.err.println("Train failed, invalid argument");
            return;
        }
        if ( k < 0 || k > 1 ) {
            System.err.println("Train failed, invalid 'k' ( must E [0,1] ) ");
            return;
        }


        double[][] neuronOutputs = verboseProcess(input);
        double[][] neuronErrors = getErrors(neuronOutputs[neuronOutputs.length - 1], correctResult);
        correctWeights(input, neuronOutputs, neuronErrors, k);

    }

    public void multTrain(double[][][] trainData, int count, double k) {
        int index = 0;
        //TODO Write checks

        int percent = count / 100;


        for (int i = 0; i < count; i++) {
            //index = ( index + 1 ) % trainData.length;
            index = rand.nextInt(trainData.length);
            train(trainData[index][0], trainData[index][1], k);
            if ( i % percent == 0 ) System.out.println(i / percent + "% complete.");
        }


        //TODO CheckData
    }

    private void correctWeights(double[] input, double[][] neuronOutputs, double[][] neuronErrors, double k) {

        //first layer
        for (int neuron = 0; neuron < weights[0].length; neuron++) {    //пробегаем по нейронам "первого" слоя
            for (int w = 0; w < weights[0][neuron].length; w++) {       //по всем его связям ( == первоначальным входам)
                weights[0][neuron][w] += k * neuronErrors[0][neuron] * actFunction.der(neuronOutputs[0][neuron]) * input[w];    //кол-во связей == кол-во элементов в input
            }
        }
        //other layers
        for (int layer = 1; layer < weights.length; layer++) {
            for (int neuron = 0; neuron < weights[layer].length; neuron++) {
                for (int w = 0; w < weights[layer][neuron].length; w++) {
                    weights[layer][neuron][w] += k * neuronErrors[layer][neuron] * actFunction.der(neuronOutputs[layer][neuron]) * neuronOutputs[layer - 1][w];
                }
            }
        }
    }

    private double[][] getErrors(double[] result, double[] correctResult) {
        double[][] errors = new double[weights.length][];
        //output layer
        errors[weights.length - 1] = new double[result.length];
        for (int neuron = 0; neuron < result.length; neuron++) {
            errors[weights.length - 1][neuron] = correctResult[neuron] - result[neuron];
        }

        //other layers
        for (int layer = weights.length - 2; layer >= 0; layer--) {
            errors[layer] = new double[weights[layer].length];
            for (int neuron = 0; neuron < weights[layer].length; neuron++) {
                errors[layer][neuron] = 0;
                for (int weight = 0; weight < weights[layer + 1].length; weight++) {
                    errors[layer][neuron] += errors[layer + 1][weight] * weights[layer + 1][weight][neuron];
                }
            }
        }

        return errors;
    }

    public double[][][] getWeights() {
        return weights.clone();
    }

    public String toString() {
        return "{" + this.actFunctionStr + "}" + Arrays.deepToString(weights);
    }

    public NetworkData export() {
        return new NetworkData(weights, actFunctionStr);
    }

}


