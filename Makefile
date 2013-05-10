#
# Makefile for p2p-framework
#
#

all: java rmi

tracker: clean java rmi
	java -Djava.rmi.server.codebase=file:`pwd`/ TrackerServer

peerserver: clean java rmi
	java -Djava.rmi.server.codebase=file:`pwd`/ PeerServer debug

superpeerserver: clean java rmi
	java -Djava.rmi.server.codebase=file:`pwd`/ SuperPeerServer debug

java:
	javac *.java

rmi:
	rmic SuperPeerServer; rmic TrackerServer; rmic PeerServer;

clean:
	rm *.class; rm *.txt, rm resources/*;
