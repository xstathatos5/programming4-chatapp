to start the server chat
1.  cd c:\Users\xen\programming4-chatapp\programming4-chatapp
2.  mvn clean compile
    mvn exec:java "-Dexec.mainClass=com.chat.server.server.ChatServer"
3.  Open a new Terminal
4.  mvn exec:java "-Dexec.mainClass=com.chat.server.client.ChatClient"