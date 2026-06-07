Read Me
This is a message application that opens a server and allows people to send text messages. It includes a JavaFX-based GUI for real-time messaging and an admin website that displays a user list and the number of messages sent.

Software Requirements
- Java 21 or later
- Maven 3.6+
- Port 5000 and 8000 available

Installation
- cd c:\Users\xen\programming4-chatapp\programming4-chatapp
- mvn clean compile

Running Application
Start server
- cd c:\Users\xen\programming4-chatapp\programming4-chatapp
- mvn exec:java "-Dexec.mainClass=com.chat.server.server.ChatServer"
Start GUI Client
- cd c:\Users\xen\programming4-chatapp\programming4-chatapp
- mvn exec:java "-Dexec.mainClass=com.chat.server.GUI.ChatAppGUI"
Or Start CLI Client
- cd c:\Users\xen\programming4-chatapp\programming4-chatapp
- mvn exec:java "-Dexec.mainClass=com.chat.server.client.ChatClient"
Web server link: 
- http://localhost:8000/
Server Instructions
- Enter username
- Type messages and press Enter
- Use command buttons

Architecture
- Server: Handles multiple client connections and broadcasts messages
- GUI client: JavaFX interface for convenience
- CLI Client: Terminal Client server

Made by Xen Stathatos