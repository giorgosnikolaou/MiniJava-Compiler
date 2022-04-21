java -jar ../jtb132di.jar -te minijava.jj
java -jar ../javacc5.jar minijava-jtb.jj
javac Main.java

# directory=../minijava-examples-new/minijava-error-extra/ # Should all fail
directory=../minijava-examples-new/minijava-extra/ # Should all pass
# directory=../minijava-examples-new/
# directory=./tests/

for file in "$directory"*.java 
do 
    echo -e "\n\nTrying file: $file"
    java Main "$file" 
    
    if [[ "$file" == *"error"* ]]; then
        echo "Error file"
    fi
done



rm -f *.class *~ MiniJavaParser.java MiniJavaParserConstants.java MiniJavaParserTokenManager.java ParseException.java Token.java TokenMgrError.java minijava-jtb.jj JavaCharStream.java
rm -f SymbolTable/*.class
rm -r visitor syntaxtree
