package NeuralNetwork;

import java.util.ArrayList;
import java.util.List;

public class NeuralNetwork {
    protected static final String defaultFunctionStr = "1 / ( 1 + exp( -x ) )";
    protected int inputSize = 0;
    protected ActFunction actFunction;
    protected String actFunctionStr;
    protected double[][][] weights;

    public NeuralNetwork(double[][][] networkWeights, String funcString) {
        //
        if (networkWeights.length > 0)
            this.inputSize = networkWeights[0][0].length;
        //TODO Проверить передаваемый массив на валидность
        //присваиваем массив весов
        weights = networkWeights.clone();
        //обрабатываем и присваиваем функцию активации
        this.actFunctionStr = funcString.replaceAll(" ", "");

        List<String> params = new ArrayList<String>();
        params.add("x");

        Eval tempFuncExp;
        try {
            tempFuncExp = new Eval(this.actFunctionStr, params);
        } catch (RuntimeException e) {
            System.err.println("ILLEGAL ACTIVATION FUNCTION, (" + e + ") USING DEFAULT: " + defaultFunctionStr + ";");
            tempFuncExp = new Eval(defaultFunctionStr, params);
            this.actFunctionStr = defaultFunctionStr;
        }
        Eval funcExpr = tempFuncExp;
        this.actFunction = (x) -> {
            funcExpr.setVariable("x", x);
            return funcExpr.eval();
        };
        //
    }

    public NeuralNetwork(NetworkData data) {
        this(data.getWeights(), data.getActFunctionStr());
    }


    public double[] process(double[] inputs) {

        double[][] outputs = verboseProcess(inputs);

        return outputs[outputs.length - 1];
    }

    protected double[][] verboseProcess(double[] inputs) {
        if (inputs.length != inputSize) {
            System.err.println("COUNT OF INPUTS IS INVALID.");
            return new double[1][0];
        }
        double[] layerInput = inputs.clone();
        double[] layerOutput;

        double[][] outputs = new double[weights.length][];

        for (int layer = 0; layer < weights.length; layer++) {
            layerOutput = new double[weights[layer].length]; //TODO сюда можно дописать отдельно нейрон смещения
            for (int neuron = 0; neuron < weights[layer].length; neuron++) {
                double neuronSum = 0;
                for (int weight = 0; weight < weights[layer][neuron].length; weight++) {
                    neuronSum += weights[layer][neuron][weight] * layerInput[weight];
                }
                layerOutput[neuron] = actFunction.run(neuronSum);

            }
            outputs[layer] = layerOutput;
            layerInput = layerOutput;
        }
        return outputs;
    }

    public int getInputSize() {
        return inputSize;
    }
}


interface ActFunction {
    double run(double x);
}