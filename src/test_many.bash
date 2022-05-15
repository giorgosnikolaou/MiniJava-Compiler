## Quick test for this functionality `java [MainClassName] [file1] [file2] ... [fileN]`

# Prerequisites
java -jar ../libs/jtb132di.jar -te ../libs/minijava.jj
java -jar ../libs/javacc5.jar ../libs/minijava-jtb.jj
javac Main.java

java Main $@

# Remove produced files
rm -f *.class *~ MiniJavaParser* Token* ParseException.java JavaCharStream.java ../libs/minijava-jtb.jj
rm -f SymbolTable/*.class error_type offsets
rm -r visitor syntaxtree
