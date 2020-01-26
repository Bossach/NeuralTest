public class Layer {

    private static int counter = 0;

    private Neuron[] neurons;
    private int size;
    private int inputSize = 0;
    private ActFunction actFunction;
    private int layer_ID;

    public Layer( double[][] weights , ActFunction func ){
        this.size = weights.length;
        if( this.size > 0 ) this.inputSize = weights[0].length;

        this.actFunction = func;

        this.neurons = new Neuron[ this.size ];
        for (int i = 0; i < this.size; i++) {
            this.neurons[i] = new Neuron( weights[i] , func );
        }
        this.layer_ID = counter++;
    }

    public void mutate( double k ) {
        for (Neuron neuron : neurons) {
            neuron.mutate( k );
        }
    }

    public double[] process( double[] inputs ) {

        if( inputs.length != inputSize ) {
            System.err.println("COUNT OF INPUTS TO LAYER IS INVALID. Layer ID: " + layer_ID);
            return new double[0];
        }

        double[] res = new double[size];

        for (int i = 0; i < size; i++) {
            res[i] = neurons[i].process( inputs );
        }
        return res;
    }

    public double[][] getWeights() {
        double[][] res = new double[size][inputSize];
        for (int i = 0; i < size; i++) {
            res[i] = neurons[i].getWeights();
        }
        return res;
    }

    public Layer clone() {

        double[][] weights = new double[size][inputSize];

        for (int i = 0; i < size; i++) {
            weights[i] = neurons[i].getWeights();
        }

        return new Layer(weights , this.actFunction );
    }
}
