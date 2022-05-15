## Asserts differnet inputs have the correct behavior 
## ie if it's supposed to throw an error and our program doesn't or if it isn't supposed to and our program does  

# Prerequisites
java -jar ../libs/jtb132di.jar -te ../libs/minijava.jj
java -jar ../libs/javacc5.jar ../libs/minijava-jtb.jj
javac Main.java

# Directories form which to choose test files from
directory_default=("../tests/")
directory=$([[ $# -eq 0 ]] && echo ${directory_default} || echo "$1")


GREEN='\033[1;32m'
RED='\033[1;31m'
NC='\033[0m' # No Color
ERROR="error" # Must be part of the path of a file that should produce an error


# For every .java file on the directory run Main with it as an argument
for file in $(find "$directory" -name "*.java" -type f -print)
do 
    echo -e "\nTrying file: $file"
    java Main "$file" 2> error_type > offsets

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

echo ""

# Remove produced files
rm -f *.class *~ MiniJavaParser* Token* ParseException.java JavaCharStream.java ../libs/minijava-jtb.jj
rm -f SymbolTable/*.class error_type offsets
rm -r visitor syntaxtree
