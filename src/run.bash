# Prerequisites
java -jar ../libs/jtb132di.jar -te ../libs/minijava.jj
java -jar ../libs/javacc5.jar ../libs/minijava-jtb.jj
javac Main.java

java Main "$1" 2> error_type
        

# Remove produced files
rm -f *.class *~ MiniJavaParser.java MiniJavaParserConstants.java MiniJavaParserTokenManager.java ParseException.java Token.java TokenMgrError.java JavaCharStream.java
rm -f SymbolTable/*.class error_type
# rm -r visitor syntaxtree

# I PERIPTOSI POU KANEI EXTEND TIN KLASSI TIS MAIN
# I PERIPTOSI POU KANEI EXTEND TIN KLASSI TIS MAIN
# I PERIPTOSI POU KANEI EXTEND TIN KLASSI TIS MAIN

# CURRENTLY DEN TIN VAZO MESA STA CLASSES