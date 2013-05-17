#
# Makefile for p2p-framework
#

JAVAOPTS=-Djava.rmi.server.codebase=file:`pwd`/ -ea

all: java rmi

gui: java
	java -ea testFrameworkGUI

tracker: java rmi
	java $(JAVAOPTS) TrackerServer

peerserver: java rmi
	java $(JAVAOPTS) PeerServer debug

superpeerserver: java rmi
	java $(JAVAOPTS) SuperPeerServer debug

java:
	javac *.java

rmi:
	rmic SuperPeerServer; rmic TrackerServer; rmic PeerServer;

clean:
	rm -f *.class *.txt resources/*
