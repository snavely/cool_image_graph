# Makefile for graph_vis

JAVA=gcj
# JAVA=javac

CLASSFILES=GraphApplet.class GraphViewer.class Graph.class

all: GraphApplet.jar

%.class : %.java
	$(JAVA) -C $<
#	$(JAVA) $<

GraphApplet.jar: $(CLASSFILES)
	jar cfm $@ manifest.txt *.class

clean:
	rm *.class GraphApplet.jar
