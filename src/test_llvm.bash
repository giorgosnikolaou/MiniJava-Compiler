## Asserts output is correctly produced 

# Prerequisites
make compile

# Directories form which to choose test files from
directory_default="../tests/llvm-examples/"

directory=$([[ $# -eq 0 ]] && echo ${directory_default} || echo "$1")


GREEN='\033[1;32m'
RED='\033[1;31m'
NC='\033[0m' # No Color
ERROR="error"

# For every .out file on the directory (it should contain the results that should be produced by the .java file with the same name)
# run the corresponding .java file and see if the output produced matches with what the given one
for file in $(find "$directory" -name "*.out" -type f -print)
do 
    name="$(basename -s .out "$file")"
    echo -e "\nTrying file: $name"    

    find "$directory" -name "$name.java" -exec java Main '{}' 2> error_type > offsets \;
    clang -Wno-everything "$name.ll" -o out1

    ./out1 > results

    if ! cmp -s results "$file" ; then
        echo -e "${RED}Output is not correct${NC}"
        # diff results "$file"
        break
    else
        echo -e "${GREEN}Output is correct${NC}"
    fi

done

echo ""

# Remove produced files
make clean 
rm -f out1 results