#
# Makefile for p2p-framework
#
#

trackerserver: all
	java -Djava.rmi.server.codebase=file:`pwd`/ TrackerServer

peerserver: all
	java -Djava.rmi.server.codebase=file:`pwd`/ PeerServer

all: java rmi

java:
	javac *.java

rmi:
	rmic SuperPeerServer; rmic TrackerServer; rmic PeerServer;

clean:
	rm *.class; rm *.txt;
