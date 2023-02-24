POKER GAME
@author Kacper Cienkosz

Application implements game of poker with server and CLI clients. The type of poker is five-card draw. Hand values are the same as in the most popular variants e.g. texas hold-em. There is no suit superiority. That means if two players have for example pair of eights, they are considered to be dead drawn and the prize is split between them. All of the players bid ante before the begining of the game. Ante is set to 20 credits. All players when connecting to server receive 1000 credits. Player receive five cards. Player can only see their cards. Then the bidding process starts. It works the same as in texas hold-em. After the first bidding phase player can discard and redraw 4 of their cards. Then next bidding phase starts. Finally, winner is determiend. Player on the "left" of the "dealer" (player assigned to be the dealer) always bids and discards cards first.


To run the PokerServer execute from parent directory (poker) 

	java -jar poker-server/target/poker-server-1.0-SNAPSHOT.jar num

where num stands for exact number of players that can play the game. This number can be 2, 3, or 4. By default it is 3.

To run the PokerClient execute from parent directory (poker)
	
	java -jar poker-client/target/poker-client-1.0-SNAPSHOT.jar


Comunication protocol:
Message consists of: 
	- gameId - ID of the game related to the message.
	- playerId - ID of the player sending or receiving message.
	- actionType - type of action requested by user or server.
	- actionParameters - further information send with the message. Can be empty.

Message looks like: gameId/playerId/actionType/actionParameters

Action types sent by server:
	- acc - ACCEPT:
		* Sent by the server to confirm that connection has been established. No response expected.
	- den - DENY:
		* Sent by the server to announce denial of action requested by user. No response excpected.
	- bid - BID:
		* Sent by the server to request bidding from the player. Excpected responses: fol, han, bid, crd. "{currentStake} {howMuchPlayerMustBid}"
	- han - HAND:
		* Sent by the server to inform player about their hand. No response excpected. "{rank} of {suit}".
	- srt - START:
		* Sent by the server to announce begining of the game. No response excpected.
	- dsc - DISCONNECT:
		* Sent by the server to inform client about breaking connection. No response excpected.
	- evl - EVAL:
		* Sent by the server to inform player about evaluation of their hand. No response excpected. Action parameter: "{handValue}".
	- end - END:
		* Sent by the server to announce end of the game and to ask if player still wants to play the game. Excpected responses: acc, dsc. Action parameter: "{numberOfPlayersMissing}". 
	- drw - DRAW:
		* Sent by the server to request discarding cards from the player. Excpected responses: drw, han, evl. 
	- crd - CREDIT:
		* Sent by the server to inform player about his current credit. No response excpected. Action parameters "{credit}".
	- prz - PRIZE:
		* Sent by the server to inform player about the prize they won. No response excpected. Action parameters: "{prize}".


Action types sent by the client:
	- acc - ACCEPT:
                * Sent by the client to confirm will to play the game. No response expected.
	- bid - BID:
		* Sent by the client to request bidding with given value. Expected responses: bid, den. Action parameters: "{bidValue}".
	- fol - FOLD:
		* Sent by the client to request folding. No expected responses.
	- han - HAND:
		* Sent by the client to request message with their hand from server. Expected responses: han.
	- dsc - DISCONNECT:
		* Sent by the client to inform about the closing connection. No expected responses.
	- evl - EVAL:
		* Sent by the client to request message with evaluation of their hand. Expected responses: evl.
	- drw - DRAW:
		* Sent by the client to inform wich cards player wants to discard. Excpected responses: den, han.
	- crd - CREDIT:
		* Sent by the client to request information of their credit. Excpected responses: crd.
	












