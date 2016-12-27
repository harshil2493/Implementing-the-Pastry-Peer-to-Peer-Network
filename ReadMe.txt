Name 	: Shah, Harshil
CSUID 	: 830387790
(For better looking of ReadMe, open it in gedit (Keep It Maximized))

Index
-----

1. 	Included Files
2. 	How To Run
3. 	Important Notes
4. 	Class Description


1. 	Included Files
--	--------------
(a)	All Source Java Files Included Inside Packages Shown Below ( Folder Replication )
		package node
		package protocols
		package transport
		package util
(b)	Makefile
(c)	ReadMe


2.	How To Run
--	----------
Run DiscoveryNode by java node.Discovery and give port number which we want DiscoveryNode to listen to.
Run Peer by java node.Peer by giving host address and port number of DiscoveryNode as argument. Furthermore, We have to specify its port too. Put Zero to randomly assign. We can specify ID of Peer too in Argument.
Run StoreData by java node.StoreData and give host address and port number of DiscoveryNode as argument.

3. 	Important Notes
--	---------------
	Commands
	--------
	StoreData : 
	---------	
		store FILENAME {KEY} 
		--------------------
		To store file in overlay use this command and specify FILENAME. Customize key for that file can also be given
		
		read FILENAME {KEY} 
		--------------------
		To read file from overlay use this command and specify FILENAME. Customize key for that file can also be given
	DiscoveryNode :
	-------------
		set-number-of-leaf NUMBER
		-------------------------
		To set number of leaves each peer can have, use this command.
		
		list-nodes
		----------
		To view live nodes' details within overlay
	Peer :
	----
		show-me-routing-table
		---------------------
		To view peer's routing table!
		
		show-me-leaf-set
		----------------
		To view leaf set of peer.
		
		show-me-files
		-------------
		To view information of files, peer had them stored.

		print-my-path
		------------
		To view path taken by joining protocol of this peer in overlay.
	
		print-my-ID
		-----------
		To view my basic details!

		view-back-up
		------------
		To view backUp nodes for Routing Table
	
		exit-me
		-------
		To leave Overlay