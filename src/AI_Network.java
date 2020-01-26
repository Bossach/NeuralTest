
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AI_Network {
    private static int counter = 0;
    private static Random rand = new Random();


    private Layer[] layers;
    private int Network_ID;
    private int inputSize = 0;
    private ActFunction actFunction;
    private String actFunctionStr;
    static private String defaultFunction = "1 / ( 1 + exp( -x ) )";


    public AI_Network( double[][][] network_weights , String funcString ) {

        if ( network_weights.length > 0 )
            this.inputSize = network_weights[0][0].length;

        this.actFunctionStr = funcString.trim();
        Map<String, Double> params = new HashMap<String, Double>();
        params.put("x", 0d);
        Expression tempFuncExp;
        try {
            tempFuncExp = Eval.getEval( this.actFunctionStr , params );
        } catch (RuntimeException e) {
            System.err.println("ILLEGAL ACTIVATION FUNCTION, (" + e + ") USING DEFAULT: " + defaultFunction + ";");
            tempFuncExp = Eval.getEval(defaultFunction, params);
            this.actFunctionStr = defaultFunction;
        }
        Expression funcExpr = tempFuncExp;
        this.actFunction = (x) -> {
            params.replace("x", x);
            return funcExpr.eval();
        };

        this.layers = new Layer[network_weights.length];
        for (int i = 0; i < network_weights.length; i++) {
            this.layers[i] = new Layer( network_weights[i] , this.actFunction );
        }

        this.Network_ID = counter++;
    }

    public AI_Network(int[] layerSizes , String funcString ) {
        this( generateRandomWeights(layerSizes) , funcString );
    }

    public AI_Network(int[] layerSizes ) {
        this( layerSizes , defaultFunction );
    }

    private static double[][][] generateRandomWeights(int[] layerSizes) {
        double[][][] network_weights = new double[ layerSizes.length - 1 ][][];

        for (int i = 1; i < layerSizes.length; i++) {
            double[][] weights = new double[ layerSizes[i] ][ layerSizes[i - 1] ];
            for (int j = 0; j < layerSizes[i]; j++) {
                for (int k = 0; k < layerSizes[i - 1]; k++) {
                    weights[j][k] = rand.nextDouble() * 2 - 1 ;
                }
            }


            network_weights[ i - 1 ] = weights;
        }
        return network_weights;
    }

    public void mutate( double k ) {
        for (Layer layer : layers) {
            layer.mutate( k );
        }
    }

    public double[] process( double[] inputs ) {

        if ( inputs.length != inputSize ) {
            System.err.println("COUNT OF INPUTS TO NETWORK IS INVALID. Network ID: " + Network_ID );
            return new double[0];
        }

        double[] current = inputs.clone();

        for (int i = 0; i < layers.length; i++) {
            current = layers[i].process( current );
        }
        return current;
    }

    public double[][][] getWeights() {
        double[][][] res = new double[layers.length][][];
        for (int i = 0; i < layers.length; i++) {
            res[i] = layers[i].getWeights();
        }
        return res;

    }

    public String toString() {
        return "{" + this.actFunctionStr + "}" + Arrays.deepToString( getWeights() );
    }
}



interface ActFunction{
    double run( double x );
}
