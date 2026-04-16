// // Modify String source to test for different possible inputs
// // I have this file to conveniently debug particular cases 
// package tool;

// import cpsc326.*;

// import java.util.List;

// import cpsc326.Expr;
// import cpsc326.Lexer;
// import cpsc326.Parser;
// import cpsc326.Token;

// public class StringToExpr 
// {

//     public static void main(String[] args)
//     {

//         String source = "";

//         Lexer lexy = new Lexer(source);

//         List<Token> tolkein = lexy.scanTokens();

//         // for (Token token : tolkein)
//         //     System.out.println(token);
        
//         Parser parsy = new Parser(tolkein);

//         Expr exprsy = parsy.parse();

//         // ASTPrinter printy = new ASTPrinter();

//         // System.out.println(printy.print(exprsy));

//         // System.out.println("Okay...");

//         // Interpreter interprety = new Interpreter();

//         // interprety.interpret(exprsy);

//         // System.out.println(interprety.getInterpret(exprsy));


//         return;
//     }    
// }
