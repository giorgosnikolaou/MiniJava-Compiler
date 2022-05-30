## Asserts offsets produced are correct 

# Prerequisites
make compile

# Directories form which to choose test files from
directory_java="../tests/examples/correct/"
directory_default="../tests/examples/offset-results/"

directory=$([[ $# -eq 0 ]] && echo ${directory_default} || echo "$1")

if [[ $# -eq 2 ]]; then 
    directory_java=$(echo "$2")
fi

GREEN='\033[1;32m'
RED='\033[1;31m'
NC='\033[0m' # No Color
ERROR="error"

# For every .txt file on the directory (it should contain the results that should be produced by the .java file with the same name)
# run the corresponding .java file and see if the offsets produce match with what they should be
for file in $(find "$directory" -name "*.txt" -type f -print)
do 
    name="$(basename -s .txt "$file")"
    echo -e "\nTrying file: $name"    

    find "$directory_java" -name "$name.java" -exec java Main '{}' 2> error_type > offsets \;

    if ! cmp -s offsets "$file" ; then
        echo -e "${RED}Offsets are not correct${NC}"
        diff offsets "$file"
        break
    else
        echo -e "${GREEN}Offsets are correct${NC}"
    fi

done

echo ""

# Remove produced files
make clean