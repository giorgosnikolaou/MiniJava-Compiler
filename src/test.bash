# Prerequisites
java -jar ../libs/jtb132di.jar -te ../libs/minijava.jj
java -jar ../libs/javacc5.jar ../libs/minijava-jtb.jj
javac Main.java

# Directories form which to choose test files from
directories_big=("../minijava-examples-new/minijava-error-extra/" "../minijava-examples-new/minijava-extra/" "../minijava-examples-new/" "../tests/" "../my/")
directories_small=("../tests/")
# directories_small=("../my/")

directories=$([[ $# -eq 0 ]] && echo ${directories_small[@]} || echo ${directories_big[@]})


GREEN='\033[1;32m'
RED='\033[1;31m'
NC='\033[0m' # No Color
ERROR="error"

# For every directory of the specified directories
for directory in ${directories[@]}; do

    # For every .java file on the directory run Main with it as an argument
    for file in "$directory"*.java 
    do 
        echo -e "\n\nTrying file: $file"
        java Main "$file" 2> error_type
        
        if grep -q "SC ERROR" error_type && [[ "$file" == *"$ERROR"* ]]; then
            echo -e "${GREEN}Semantic error occured, as expected${NC}"
        fi

        if ! grep -q "SC ERROR" error_type && [[ "$file" == *"$ERROR"* ]]; then
            echo -e "${RED}Semantic error didn't occure when it should have${NC}"
            break
        fi
        
        if grep -q "SC ERROR" error_type && ! [[ "$file" == *"$ERROR"* ]]; then
            echo -e "${RED}Semantic error occured when it shouldn't have${NC}"
            break
        fi

        if ! grep -q "SC ERROR" error_type && ! [[ "$file" == *"$ERROR"* ]]; then
            echo -e "${GREEN}Semantic error didn't occure, as expected${NC}"
        fi
        
    done
done

echo ""

# java Main ../my/ERROR_not.java > error_type

# Remove produced files
rm -f *.class *~ MiniJavaParser.java MiniJavaParserConstants.java MiniJavaParserTokenManager.java ParseException.java Token.java TokenMgrError.java JavaCharStream.java
rm -f SymbolTable/*.class error_type
# rm -r visitor syntaxtree
