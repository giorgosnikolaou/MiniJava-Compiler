
LIBS = ../libs/
SRC = ./
TESTS =../tests/

compile:
	@java -jar $(LIBS)jtb132di.jar -te $(LIBS)minijava.jj
	@java -jar $(LIBS)javacc5.jar $(LIBS)minijava-jtb.jj
	@javac Main.java

clean:
	@rm -f *.class *~ $(SRC)MiniJavaParser* $(SRC)Token* $(SRC)ParseException.java $(LIBS)minijava-jtb.jj $(SRC)JavaCharStream.java
	@rm -f $(SRC)SymbolTable/*.class  offsets error_type
	@rm -rf $(SRC)visitor $(SRC)syntaxtree


FILES = $(TESTS)Assign-error.java \
		$(TESTS)BrackeExpressionTest.java \
		$(TESTS)Inheritance-error.java \
		$(TESTS)InheritanceTest.java

run: compile 
	@java Main $(TESTS)BrackeExpressionTest.java
	@make -s clean

