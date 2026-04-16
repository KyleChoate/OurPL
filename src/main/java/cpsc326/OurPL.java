package cpsc326;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class OurPL {
    
    public static void main(String[] args) throws IOException {
        if (args.length > 1) 
        {
            System.out.println("Usage: OurPl [script]");
            System.exit(64);
        }
        else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }
    
    public static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) System.exit(65);
    }

    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void runPrompt() throws IOException 
    {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if(line == null) {
                break;
            }
            run(line);
            hadError = false;
        }
    }

    public static void run(String source) 
    {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        Parser parser = new Parser(tokens);

        for (Token token : tokens){
            System.out.println(token);
        }
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }


    public static void runtimeError(RuntimeError error)
    {
        hadRuntimeError = true;
        report(error.token.line, "", error.getMessage());
        // System.err.println("Error: " + error.getMessage() + " at token " + error.token);
    }
}
