all: compile

compile:
	@java -jar ../jtb132di.jar -te minijava.jj
	@java -jar ../javacc5.jar minijava-jtb.jj
	@javac Main.java

clean:
	@rm -f *.class *~ MiniJavaParser.java MiniJavaParserConstants.java MiniJavaParserTokenManager.java ParseException.java Token.java TokenMgrError.java minijava-jtb.jj JavaCharStream.java

run: all 
	@java Main ../minijava-examples-new/minijava-error-extra/BadAssign2.java
	@rm -f *.class *~ MiniJavaParser.java MiniJavaParserConstants.java MiniJavaParserTokenManager.java ParseException.java Token.java TokenMgrError.java minijava-jtb.jj JavaCharStream.java
	@rm -f SymbolTable/*.class 
# @rm -r visitor syntaxtree 


err: all 
	@for i in ../minijava-examples-new/minijava-error-extra/*.java 
	do 
		java Main i 
	done
	@rm -f *.class *~ MiniJavaParser.java MiniJavaParserConstants.java MiniJavaParserTokenManager.java ParseException.java Token.java TokenMgrError.java minijava-jtb.jj JavaCharStream.java
	@rm -f SymbolTable/*.class