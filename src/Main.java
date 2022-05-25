import syntaxtree.*;
import visitor.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import SymbolTable.SymbolTable;

public class Main {
    private static final String eq = "====================================================================================";
    public static void main(String[] args) throws Exception {
        if(args.length < 1){
            System.err.println("Usage: java Main <inputFile1> <inputFile2> ... <inputFileN>");
            System.exit(1);
        }

        for (String arg : args)
        {
            System.err.println("\nFile: " + arg);
            FileInputStream fis = null;
            try{
                fis = new FileInputStream(arg);

                MiniJavaParser parser = new MiniJavaParser(fis);

                Goal root = parser.Goal();

                System.err.println("Program parsed successfully.");
                
                SymbolTable st = new SymbolTable();
                PopulatingVisitor pop = new PopulatingVisitor(st);
                root.accept(pop, null);

                AnalysisVisitor anal = new AnalysisVisitor(st);
                root.accept(anal, null);

                System.err.println("Program is semantically correct.");
                

                IRVisitor ir = new IRVisitor(st);
                root.accept(ir, null);

                System.err.println("LLVM code generated succesfully.\n");
                

            }
            catch(ParseException ex){
                System.err.println(eq + "\n" + ex.getMessage() + "\n" + eq);
                System.err.println("PARSE ERROR");
            }
            catch(FileNotFoundException ex){
                System.err.println(eq + "\n" + ex.getMessage() + "\n" + eq);
                System.err.println("IO ERROR");
            }
            catch(Exception ex){
                System.err.println(eq + "\n" + ex.getMessage() + "\n" + eq);
                System.err.println("SC ERROR");
            }
            finally{
                try{
                    if(fis != null) fis.close();
                }
                catch(IOException ex){
                    System.err.println(ex.getMessage());
                    System.err.println("IO ERROR");
                }
            }
        }

        
    }
}









