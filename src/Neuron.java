import java.util.Arrays;
import java.util.Random;

public class Neuron {

    private static Random rand = new Random();
    private static int counter = 0;


    private double[] weights;
    private double cachedRes;
    private double[] cachedInput;
    private boolean cached = false;
    private ActFunction actFunction;
    private int neuron_ID;

    public Neuron( double[] weights , ActFunction func ) {
        this.weights = weights.clone();
        this.neuron_ID = counter++;
        this.actFunction = func;
    }

    public double[] getWeights() {
        return this.weights.clone();
    }

    public void mutate( double k ) {

        //checking k valid
        /*if (k > 1 || k < -1) {
            k = 1 / k;
        }*/
        this.cached = false;
        double d, w;
        for (int i = 0; i < weights.length; i++) {
            w = this.weights[i];
            d = k * ( rand.nextDouble() * 2 - 1 ) ;
            if ( w + d >= 1 || w + d <= -1 ) d *= -1;
            this.weights[i] +=d ;
        }
    }

    public double process( double[] input ) {

        if ( input.length != weights.length ) {
            System.err.println("INPUTS COUNT INVALID. Neuron_ID: " + this.neuron_ID );
            return 0;
        }

        if( this.cached && Arrays.equals( input , this.cachedInput) ) {
            return this.cachedRes;
        }

        double res = 0;

        for (int i = 0; i < input.length ; i++) {
            res += input[i] * this.weights[i];
        }
        res = this.actFunction.run( res );
        this.cachedInput = input.clone();
        this.cachedRes = res;
        this.cached = true;
        return res;
    }

    public Neuron clone() {
        return new Neuron(this.weights , this.actFunction );
    }

}


