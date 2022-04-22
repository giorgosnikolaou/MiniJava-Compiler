# Prerequisites
java -jar ../libs/jtb132di.jar -te ../libs/minijava.jj
java -jar ../libs/javacc5.jar ../libs/minijava-jtb.jj
javac Main.java

# Directories form which to choose test files from
directories=("../minijava-examples-new/minijava-error-extra/" "../minijava-examples-new/minijava-extra/" "../minijava-examples-new/" "./tests/")

# For every directory of the specified directories
for directory in ${directories[@]}; do

    # For every .java file on the directory run Main with it as an argument
    for file in "$directory"*.java 
    do 
        echo -e "\n\nTrying file: $file"
        java Main "$file" > error_type
        
        if grep -q "SC ERROR" error_type && [[ "$file" == *"error"* ]]; then
            echo "Error occured, as expected"
        fi

        if ! grep -q "SC ERROR" error_type && [[ "$file" == *"error"* ]]; then
            echo "Error didn't occure when it should have"
        fi
        
        if grep -q "SC ERROR" error_type && ! [[ "$file" == *"error"* ]]; then
            echo "Unexpected error occured"
        fi

        if ! grep -q "SC ERROR" error_type && ! [[ "$file" == *"error"* ]]; then
            echo "Error didn't occure, as expected"
        fi
        
    done
done

# Remove produced files
rm -f *.class *~ MiniJavaParser.java MiniJavaParserConstants.java MiniJavaParserTokenManager.java ParseException.java Token.java TokenMgrError.java JavaCharStream.java
rm -f SymbolTable/*.class error_type
rm -r visitor syntaxtree
