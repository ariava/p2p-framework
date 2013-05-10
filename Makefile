#
# Makefile for p2p-framework
#
#

all: java rmi

trackerserver: java rmi
	java -Djava.rmi.server.codebase=file:`pwd`/ TrackerServer

peerserver: java rmi
	java -Djava.rmi.server.codebase=file:`pwd`/ PeerServer debug

java:
	javac *.java

rmi:
	rmic SuperPeerServer; rmic TrackerServer; rmic PeerServer;

clean:
	rm *.class; rm *.txt;
