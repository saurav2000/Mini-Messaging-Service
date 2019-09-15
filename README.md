# Mini-Messaging-Service

## How to Run
+ To compile the code run `javac *.java`
+ To run the client run `java Client Username SERVER_IP_ADDRESS PORT_NO MODE`
	> MODE is 0 for unencrypted messages, 1 is for encrypted messages without signature, 2 is for encrypted message with signature. 
+ To run the server run `java Server PORT_NO MODE`

## Parameter Validation
If the mode of the client/server is negative integer or > 2, error message is printed and client/server is exited completely.

## Sending and Receiving a Message
Suppose **Saurav** is sending a message to **Nikhil**
+ **Saurav** sends the message as `@Nikhil [MESSAGE]`
+ **Saurav** receives the message `Succesfully sent` upon succesful delivery or any other appropriate message. 
+ **Nikhil** sees the message as `@Saurav: [MESSAGE] (SAFE)`
	> Safe indicates the signature is verified and is only available in Mode 2
	> If the message is tampered the sender gets the message `Message Authenticaition Failed`

## Registration and Deregistration
+ The initial username is taken from the parameters. If this username is taken or invalid the client is prompted to enter a valid username till the username is verified by the server.
+ If the mode of the client does not match with that of the server, error message is printed and the client is exited completely.
+ The client can deregister by typing in `DEREGISTER`.
+ On pressing `Ctrl-C` the client is deregistered and a message is printed on the server that it exited abruptly.
+ When `Ctrl-C` is pressed on the server, all clients are closed after printing a message that the server closed.

## Socket and Port Handling
+ While initialising the server if the port is occupied by some other process or its permission is denied the error message is printed and the server exits completely
+ While initialising the client if the port or ip address does not match with that of the server the error is printed and the client exits completely

