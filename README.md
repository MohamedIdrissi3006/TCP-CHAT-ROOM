TCP Client-Server Chat Room Using Thread Pools

Overview
This is a simple TCP chat room application with:
- A server that handles multiple clients concurrently using thread pools
- Clients that connect to the server to send and receive messages

Features
- Multiple clients can join the chat room
- Server uses a thread pool to manage client connections efficiently
- Messages from any client are broadcast to all connected clients

Components

1. Server
- Listens on a TCP port
- Uses a fixed thread pool to handle incoming client connections
- For each client, a runnable handles message reading and broadcasting

2. Client
- Connects to the server TCP socket
- Reads user input and sends messages to the server
- Listens for messages from the server and displays them

Basic Flow

Server:
- Start server socket
- Create thread pool
- Accept client connections in a loop
- Submit a ClientHandler task to thread pool for each connection

ClientHandler (Runnable):
- Reads messages from client
- Broadcasts messages to other clients

Client:
- Connect to server socket
- Start a thread to read messages from server
- Main thread reads input from user and sends to server

---

Note: Use java.util.concurrent.ExecutorService for thread pool management.
