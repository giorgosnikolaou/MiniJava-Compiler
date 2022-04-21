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

            System.out.println("Program parsed successfully.");
            
            SymbolTable st = new SymbolTable();
            PopulatingVisitor pop = new PopulatingVisitor(st);
            root.accept(pop, null);

            // st.print();
            

            AnalysisVisitor anal = new AnalysisVisitor(st);
            root.accept(anal, null);

            System.out.println("Program is semantically correct at this point.");

            // Need to build the vtable

        }
        catch(ParseException ex){
            System.err.println("==========================================\n" + ex.getMessage() + "\n==========================================");
        }
        catch(FileNotFoundException ex){
            System.err.println("==========================================\n" + ex.getMessage() + "\n==========================================");
        }
        catch(Exception ex){
            System.err.println("==========================================\n- " + ex.getMessage() + "\n==========================================");
        }
        finally{
            try{
                if(fis != null) fis.close();
            }
            catch(IOException ex){
                System.err.println(ex.getMessage());
            }
        }
    }
}