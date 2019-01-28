import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App {

    private final Scanner in = new Scanner(System.in);
















    public void start(String[] args) {
        printPromt();

        final String query = getInput();
        QueryProcessor processor = new QueryProcessor();
        List<Accumulator> results = null;
        try {
            results = processor.process(query, 10);
        } catch (Exception e) {
            System.err.println("Unexpected Error");
            e.printStackTrace();
        }

        printResults(results);
    }

    public void printResults(List<Accumulator> results) {
        int i = 1;
        for (Accumulator x : results) {
            Document doc = new Document(x.getDid());
            System.out.print(i++ + ": ");
            System.out.print(doc);
            System.out.println(" (" + x.getScore() + ")");
        }
    }
    public void printPromt(){
        System.out.println("Please enter your query:");
    }

    private String getInput() {
        return in.nextLine();
    }

    public App() {
        
    }

    public static void main(String[] args) {
        App instance = new App();
        instance.start(args);
    }
}