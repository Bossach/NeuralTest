package NeuralNetwork;

import java.io.Serializable;

public class NetworkData implements Serializable {
    private double[][][] weights;
    private String actFunctionStr;

    public NetworkData(double[][][] weights, String func) {
        this.weights = weights.clone();
        this.actFunctionStr = func;
    }

    public double[][][] getWeights() {
        return weights.clone();
    }

    public String getActFunctionStr() {
        return actFunctionStr;
    }
}
