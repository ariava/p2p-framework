#!/bin/bash

# Script per avviare l'applicazione con un unico comando
#
# Uso:
# ~$./start opzione
#
# Opzioni:
# peer: avvia, ognuno in un terminale, rmiregistry, peerserver, superpeerserver, gui, piu' un terminale
#       in cui fa il make clean e lo lascia aperto per eventuali cose che vogliamo farci a mano
# tracker: avvia rmiregistry, terminale di appoggio col make clean e tracker
# all: avvia tutto: rmiregistry, make clean, peerserver, superpeerserver, gui
#
# Per fermare il tutto, CTRL+C su ogni shell termina la rispettiva parte dell'applicazione.
# Non mi e' venuto in mente un modo migliore e che costasse meno di 2 mesi-stefano per chiudere le cose..
# questo non e' molto elegante pero' funziona:P


peer="peer"
all="all"
tracker="tracker"


gnome-terminal --title="Registry" -e "bash -c \"rmiregistry\";" &

gnome-terminal --title="Varie" -e "bash -c \"make clean\";bash" &
sleep 1

if [ "$1" = "$peer" -o "$1" = "$all" ];
then
	gnome-terminal --title="PeerServer" -e "bash -c \"make peerserver\";" &
	sleep 1
	gnome-terminal --title="SuperPeerServer" -e "bash -c \"make superpeerserver\";" &
	gnome-terminal --title="GUI" -e "bash -c \"make gui\";" &

fi

if [ "$1" = "$tracker" -o "$1" = "$all" ]; then
	
	gnome-terminal --title="Tracker" -e "bash -c \"make tracker\";bash"

fi

