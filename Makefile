JFLAGS = -g
JC = javac

default:
	$(JC) $(JFLAGS) src/*.java

clean:
	$(RM) src/*.class
	
i:
	java -cp src Client internal
	
e:
	java -cp src Client external
	
sim:
	java -cp src Simulator
	
nsim: clean default sim
