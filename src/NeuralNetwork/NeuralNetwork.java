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
        //Проверяем передаваемый массив на валидность
        checkWeightsArray(networkWeights);
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
        this.actFunction = new ActFunction() {
            public double out(double x) {
                funcExpr.setVariable("x", x);
                return funcExpr.eval();
            }
            public double der(double x) {
                funcExpr.setVariable("x", x);
                return funcExpr.deriv();
            }
        };
        // (x) -> {
        //     funcExpr.setVariable("x", x);
        //     return funcExpr.eval();
        // };
        //
    }

    private void checkWeightsArray(double[][][] networkWeights) {
        for (int i = 1; i < networkWeights.length; i++) {
            for (double[] w : networkWeights[i]) {
                //if (w.length != networkWeights[i - 1].length) throw new Exception("Invalid weights array"); //Так было бы по-правильному
                //Можно сделать метод boolean чтобы он возвращал false после ^этой проверки
                networkWeights[i - 1][w.length - 1].toString(); //Шаманизм
                w[networkWeights[i - 1].length - 1] += 0;       //
                //^Шаманская проверка массива на валидность чтобы не обрабатывать Exception. При невалидном выкинет "Out of bounds"
            }
        }
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
                layerOutput[neuron] = actFunction.out(neuronSum);

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
    double out(double x);
    double der(double x);
}