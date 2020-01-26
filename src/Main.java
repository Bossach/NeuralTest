import java.util.*;

public class Main {

    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);

        Random rand = new Random();

        int[] arr = new int[] { 3 , 2 , 1 };

        AI_Network netw = new AI_Network( arr );

        System.out.println(netw.toString());


        funcTest( "1/x" , 5 );

    }

    private static void funcTest(String str , double arg) {
        Map<String , Double> params = new HashMap<String, Double>();
        params.put("x", 0.0);

        Expression expr = Eval.getEval(str , params );

        ActFunction func = ( x ) -> {
            params.replace("x" , x);
            return expr.eval();
        };

        System.out.println( func.run(arg));
    }
}
