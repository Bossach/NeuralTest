package NeuralNetwork;

import java.io.Serializable;

public class NetworkData implements Serializable {
    private static final long serialVersionUID = 5589202003297447649L;
    
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
