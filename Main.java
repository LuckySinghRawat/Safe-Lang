import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) {

        String filePath = "f1.txt";
        StringBuilder program = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));

            String line;

            while ((line = reader.readLine()) != null) {
                program.append(line).append("\n");
            }

            reader.close();

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return;
        }

        String sourceCode = program.toString();

        lexer lexer = new lexer(sourceCode);
        
        System.out.println("------------Tokens----------------");
        List<token> tokens = lexer.tokenize();
        for (token t : tokens) {
            System.out.println(t.getCategory() + ": " + t.value);
        }
        System.out.println("------------Execution-------------");
        parser parser = new parser(tokens);
        parser.parse();
        
    }
}