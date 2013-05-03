#
# Makefile for p2p-framework
#
#

all: java rmi

java:
	javac *.java

rmi:
	rmic SuperPeerServer; rmic TrackerServer; rmic PeerServer;

clean:
	rm *.class
