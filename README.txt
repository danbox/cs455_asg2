CS455 Assignment 2
Dan Boxler

README

FILES
Client.java: Entry point for client, extends Node
CustomMap.java: Interface for custom map
CustomQueue.java: Interface for custom queue
SafeMap.java: Thread safe implementation of CustomMap interface
SafeQueue.java: Thread safe implementation of CustomQueue interface
Node.java: Abstract class for server and clients
ClientInfo.java: Wrapper for client information
Server.java: Entry point for server, extends Node
SocketChannelRequest.java: contains information pertaining to interest changes for SocketChannels
AcceptConnectionTask.java: Task used in queue to accept incoming connection request
AddTask.java: Dummy task used for thread pool testing
ReadFileTask.java: Dummy task used for thread pool testing
ReadTask.java: Task used in queue to read file from SocketChannel
SleepTask.java: Dummy task used for thread pool testing
Task.java: Interface for tasks
TaskFactory.java: Factory for tasks
WriteTask.java: Task used in queue to write to SocketChannel
ThreadPoolManager.java: Thread pool implementation
Worker.java: Worker used for threads, implements Runnable
Makefile: make file to compile source code

Usage:
Server: java cs455.scaling.server server <port-num> <thread-pool-size>
Client: java cs455.scaling.client.Client <server-hostname> <server-listening-port> <message-rate>
