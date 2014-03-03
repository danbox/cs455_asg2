package cs455.scaling.server;


import cs455.scaling.Node;
import cs455.scaling.client.Client;
import cs455.scaling.datastructures.CustomMap;
import cs455.scaling.datastructures.CustomQueue;
import cs455.scaling.datastructures.SafeMap;
import cs455.scaling.datastructures.SafeQueue;
import cs455.scaling.tasks.AcceptConnectionTask;
import cs455.scaling.threadpool.ThreadPoolManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author  Dan Boxler
 */

public class Server extends Node
{
    private final CustomMap<SocketChannel, ClientInfo>      _clients;
    private final ThreadPoolManager                         _threadPoolManager;
    private final List<SocketChannelRequest>                _pendingRequests;

    public Server(InetAddress hostAddress, int port, int threadPoolSize) throws IOException
    {
        _selector = initSelector(hostAddress, port);
        _clients = new SafeMap<SocketChannel, ClientInfo>();
        _threadPoolManager = new ThreadPoolManager(threadPoolSize);
        _pendingRequests = new LinkedList<SocketChannelRequest>();
    }

    @Override
    public void run()
    {
        try
        {
            //main loop
            for(;;)
            {
                //check to see if server should be terminated

                synchronized(_pendingRequests)
                {
                    for(SocketChannelRequest request : _pendingRequests)
                    {
                        switch(request.getType())
                        {
                            case SocketChannelRequest._REGISTER:
                                //register the socket channel to the selector
                                request.getSocketChannel().register(_selector, SelectionKey.OP_READ, _clients.get(request.getSocketChannel()));
                                break;

                            case SocketChannelRequest._DEREGISTER:
                                //deregister the socket channel from the selector
                                System.out.println("Sorry this isn't implemented yet...");
                                break;

                            case SocketChannelRequest._READ:
                                //set the interest to read
                                request.getSocketChannel().keyFor(_selector).interestOps(SelectionKey.OP_READ);
                                break;

                            case SocketChannelRequest._WRITE:
                                //set the interest to write
                                request.getSocketChannel().keyFor(_selector).interestOps(SelectionKey.OP_WRITE);
                                break;
                        }
                    }

                    //clear all pending requests
                    _pendingRequests.clear();
                }

                int selectedKeys = _selector.select();
                if(selectedKeys == 0)
                {
                    //go back to beginning of loop
                    continue;
                }

                //dispatch all ready I/O events
                Iterator<SelectionKey> keys = _selector.selectedKeys().iterator();
                while(keys.hasNext())
                {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if(!key.isValid())
                    {
                        //selection key is not valid, so skip it
                        continue;
                    }

                    //check what are the interests that are active
                    if(key.isAcceptable())
                    {
                        //a connection is ready to be completed
                        accept(key);
                        continue;
                    }
                    if(key.isReadable())
                    {
                        //can read
                        read(key);
                        continue;
                    }
                    if(key.isWritable())
                    {
                        //can write
                        write(key);
                        continue;
                    }
                }
            }

        }catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    private Selector initSelector(InetAddress hostAddress, int port) throws IOException
    {
        //grabs open selector
        Selector selector = initSelector();

        //set up ServerSocketChannel
        ServerSocketChannel serverChannel = ServerSocketChannel.open();

        //configure non-blocking
        serverChannel.configureBlocking(false);

        //bind to host address and specified portSelector.open();
        serverChannel.socket().bind(new InetSocketAddress(hostAddress, port));

        //register server sockSelector selector = Selector.open();et channel to selector and set to accept
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        return selector;
    }

    private void accept(SelectionKey key) throws IOException
    {
        _threadPoolManager.addTaskToQueue(new AcceptConnectionTask(this, key));

        try
        {
            Thread.sleep(1000);
        }catch(InterruptedException ie)
        {
            ie.printStackTrace();
        }

    }

    @Override
    //computes the hash of the read data packet and adds that the pending write list to client
    protected void handleResponse(SelectionKey key, byte[] bufferBytes)
    {
        SocketChannel channel = (SocketChannel)key.channel();
        try
        {
            byte[] hash = SHA1FromBytes(bufferBytes).getBytes();
            _clients.get(channel).addPendingWrite(hash);


        }catch(NoSuchAlgorithmException nsae)
        {
            nsae.printStackTrace();
        }
    }

    @Override
    //sends all pending writes to client associated with key
    protected void write(SelectionKey key) throws IOException
    {
        //get channel from key
        SocketChannel channel = (SocketChannel)key.channel();

        //get list of pending writes, write out on the channel
        for(byte[] data : _clients.get(channel).getPendingWriteList())
        {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            channel.write(buffer);
            System.out.println("Writing: " + data.toString());
        }

        //clear the pending writes for client
        _clients.get(channel).clearPendingWrites();

        //set key to read interest
        key.interestOps(SelectionKey.OP_READ);
    }

    //starts the worker threads in the pool
    private void startThreadPool()
    {
        _threadPoolManager.runWorkerThreads();
    }

    public void addClient(SocketChannel socketChannel, ClientInfo clientInfo)
    {
        _clients.put(socketChannel, clientInfo);
    }

    public void addRequest(SocketChannelRequest request)
    {
        synchronized(_pendingRequests)
        {
            _pendingRequests.add(request);
        }
    }

    //server entry point
    public static void main(String[] args)
    {
        //get port number
        int port = 0;
        int threadPoolSize = 0;
        if(args.length != 2) //invalid number of arguments
        {
            System.err.println("Invalid arguments\nUsagserver.startServer();e: java cs455.scaling.server.Server <port-num> <thread-pool-size>");
            System.exit(1);
        }else
        {
            port = Integer.parseInt(args[0]);
            threadPoolSize = Integer.parseInt(args[1]);
        }

        //start server and assign to wildcard address and specified port
        try
        {
//            new Thread(new Server(null, port));
            Server server = new Server(null, port, threadPoolSize);
            server.startThreadPool();
            server.run();
        }catch(IOException ioe)
        {
            ioe.printStackTrace();
        }

    }
}
