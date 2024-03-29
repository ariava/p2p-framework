#!/bin/bash

# Script per avviare l'applicazione con un unico comando
#
# Uso:
# ~$./start opzione
#
# Opzioni:
# peer:		avvia, ognuno in un terminale, rmiregistry, peerserver,
#		superpeerserver, gui, piu' un terminale in cui fa il make
#		clean
# tracker:	avvia rmiregistry, terminale di appoggio col make clean e
#		tracker
# all: 		avvia tutto: rmiregistry, make clean, peerserver, superpeer-
#		server, gui
#
# Per fermare il tutto, CTRL+C su ogni shell termina la rispettiva parte
# dell'applicazione.

peer="peer"
all="all"
tracker="tracker"

show_usage () {
	echo "$0 [$peer|$tracker|$all]"
	echo "$peer:         avvia, ognuno in un terminale, rmiregistry, peerserver,"
	echo "              superpeerserver, gui, piu' un terminale in cui fa il make"
	echo "              clean"
	echo "$tracker:      avvia rmiregistry, terminale di appoggio col make clean e"
	echo "              tracker"
	echo "$all:          avvia tutto: rmiregistry, make clean, peerserver, superpeer-"
	echo "              server, gui"
}

if [[ "$1" == "" || "$peer $all $tracker" != *"$1"* ]]; then
	show_usage
	exit 0
fi

gnome-terminal --geometry=67x10+0+0 --title="Registry" -e "bash -c \"rmiregistry\";" &
gnome-terminal --geometry=67x10+700+0 --title="Varie" -e "bash -c \"make clean\";" &
sleep 1

if [ "$1" = "$peer" -o "$1" = "$all" ]; then
	gnome-terminal --geometry=67x10+0+300 --title="PeerServer" -e "bash -c \"make peerserver\";" &
	sleep 1
	gnome-terminal --geometry=67x10+700+300 --title="SuperPeerServer" -e "bash -c \"make superpeerserver\";" &
	gnome-terminal --geometry=67x10+0+600 --title="GUI" -e "bash -c \"make gui\";" &
fi

if [ "$1" = "$tracker" -o "$1" = "$all" ]; then
	# Il terminale del tracker mantiene attiva una shell per consentire di effettuare test
	# di downtime del tracker
	gnome-terminal --geometry=67x10+700+600 --title="Tracker" -e "bash -c \"make tracker\";bash" &
fi

