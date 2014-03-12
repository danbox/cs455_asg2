JAVAC=javac
sources = $(wildcard cs455/scaling/*/*.java)
classes = $(sources:.java=.class)
all: $(classes)
clean :
	rm -f cs455/scaling/*/*.class
%.class : %.java
	$(JAVAC) -cp . $<
