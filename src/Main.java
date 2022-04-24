import syntaxtree.*;
import visitor.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import SymbolTable.SymbolTable;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length != 1){
            System.err.println("Usage: java Main <inputFile>");
            System.exit(1);
        }

        FileInputStream fis = null;
        try{
            fis = new FileInputStream(args[0]);

            MiniJavaParser parser = new MiniJavaParser(fis);

            Goal root = parser.Goal();

            System.err.println("Program parsed successfully.");
            
            SymbolTable st = new SymbolTable();
            PopulatingVisitor pop = new PopulatingVisitor(st);
            root.accept(pop, null);

            // st.print();

            AnalysisVisitor anal = new AnalysisVisitor(st);
            root.accept(anal, null);

            System.err.println("Program is semantically correct at this point.");

            // Need to print offsets

            st.print_offsets();

        }
        catch(ParseException ex){
            System.err.println("==========================================\n" + ex.getMessage() + "\n==========================================");
            System.err.println("PARSE ERROR");
        }
        catch(FileNotFoundException ex){
            System.err.println("==========================================\n" + ex.getMessage() + "\n==========================================");
            System.err.println("IO ERROR");
        }
        catch(Exception ex){
            System.err.println("==========================================\n" + ex.getMessage() + "\n==========================================");
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