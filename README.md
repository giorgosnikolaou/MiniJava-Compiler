## Symbol Table
 
* ### SymbolTable
    The main class used for storing all the necessary info about classes and inheritance

* ### Class
    Represents a defined class </br>
    Keeps info about the variables and functions of the class

* ###  Function
    Represents a defined function </br>
    Keeps info about the arguments the function takes and the variables declared in it

* ### Variable
    Represents a defined variable

In order to print the offsets in the required order, order of decleration is important and a LinkedHashMap is used (instead of a simple HashMap)

## Visitors
* ### PopulatingVisitor
    Populates the symbol table while performing minor semantic analysis, through the symbol table itself, in cases where an error is identifed throws an Exception </br>
    For example catches the case where a class extend a not yet defined one and if a class/variable/function is redefined on the same scope

* ### AnalysisVisitor
    Performs the semantic analysis </br>
    When a semantic error occurs throws an Exception

* ### IRVisitor
    Performs the LLVM code generation </br>
    Needs the other two visitors to have already executed


## Representation of array on LLVM code produced
* ### Integer arrays
    Integer arrays are represented with the type structure `type { i32, i32* }` <br>
    The non pointer is where the size is stored <br>
    The pointer points to the actuall array

* ### Boolean arrays
    Boolean arrays are represented with the type structure `type { i32, i1* }` <br>
    The non pointer is where the size is stored <br>
    The pointer points to the actuall array

It was implemented this way to perform the required out of bounds checks and to not waste any much memory 


## Changes on the code of the previous assignment
* Added a vtable field on each class, which is a map of `<Function name, Class Name>` and some helper functions
* The creation of the vtable for all the classes is performed while the `PopulatingVisitor` is executing. <br>
    When a new class is created it "inherits" the vtable of the class it extends (if it does) <br>
    For every new function added, if it already exists on the vtable change its `Class name` to the current function and if it doesn't just add it. <br>
    This way we can easily manage the vtables on the `IRVisitor`

## Compilation and Execution
While on folder `src` run
* `make compile` to generate the files needed
* `java Main <file1> ... <fileN>` to perform semantic analysis on files 
* `make clean` to remove the generated files
* `make run` to do all of the above

## Testing 
While on folder `src` run the following commands to perform different tests
* `./test_many.bash <MiniJava1> ... <MiniJavaN>` </br>
Test the functionality of `java [MainClassName] [file1] [file2] ... [fileN]` on the given files

* `./test_offsets.bash <path to offset.txt dir> <path to MiniJava.java dir>`  </br>
Number of arguments can be 0, 1 or 2  </br>
0 arguments -> default paths  </br>
1 argument -> path for directory containing the offset.txt files (files with the expected results)  </br>
2 arguments -> first argument same as above, second argument path for the directory with the inpout MiniJava files

* `./test_sc.bash <path to MiniJava.java dir>` </br>
Performs semantic analysis on all the `.java` files of the given directory (defualt one if none is given) </br>
Stops if it encounters an unexpected behavior (eg an error occured/didn't occur when it shouldn't have/should have occured)

* `./test_llvm.bash <path to outputs.out dir>` </br>
For every `.out` file of the fiven directory (defualt one if none is given), produces the `.ll` and `.out` (through clang) files for the corresponding `.java` files of the same folder and check if it matches the original (correct) `.out` file given </br>
Stops if the produced output is wrong
