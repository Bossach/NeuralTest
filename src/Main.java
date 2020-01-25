import java.util.*;

public class Main {

    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);

        Random rand = new Random();

        int[] arr = new int[] { 3 , 2 , 1 };

        AI_Network netw = new AI_Network( arr );

        System.out.println(netw.toString());

    }
}
