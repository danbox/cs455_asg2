package cs455.scaling.server;


import cs455.scaling.node.Node;
import cs455.scaling.datastructures.CustomMap;
import cs455.scaling.datastructures.SafeMap;
import cs455.scaling.tasks.AcceptConnectionTask;
import cs455.scaling.tasks.ReadTask;
import cs455.scaling.tasks.WriteTask;
import cs455.scaling.threadpool.ThreadPoolManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author  Dan Boxler
 */

public class Server extends Node
{
    private final CustomMap<Channel, ClientInfo>            _clients;
    private final ThreadPoolManager                         _threadPoolManager;
    private final List<SocketChannelRequest>                _pendingRequests;
    private final Set<Channel>                              _inProgressChannels;

    public Server(InetAddress hostAddress, int port, int threadPoolSize) throws IOException
    {
        _selector = initSelector(hostAddress, port);
        _clients = new SafeMap<Channel, ClientInfo>();
        _threadPoolManager = new ThreadPoolManager(threadPoolSize);
        _pendingRequests = new LinkedList<SocketChannelRequest>();
        _inProgressChannels = new HashSet<Channel>();
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
                        SocketChannel socketChannel = (SocketChannel)request.getChannel();
                        switch(request.getType())
                        {
                            case SocketChannelRequest._REGISTER:
                                //register the socket channel to the selector

                                socketChannel.register(_selector, SelectionKey.OP_READ, _clients.get(request.getChannel()));
                                break;

                            case SocketChannelRequest._DEREGISTER:
                                //deregister the socket channel from the selector
                                System.out.println("Sorry this isn't implemented yet...");
                                break;

                            case SocketChannelRequest._READ:
                                //set the interest to read
                                socketChannel.keyFor(_selector).interestOps(SelectionKey.OP_READ);
                                break;

                            case SocketChannelRequest._WRITE:
                                //set the interest to write
                                socketChannel.keyFor(_selector).interestOps(SelectionKey.OP_WRITE);
                                break;
                        }
                    }

                    //clear all pending requests
                    _pendingRequests.clear();
                    synchronized(_inProgressChannels)
                    {
                        _inProgressChannels.clear();
                    }
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

                    synchronized(_inProgressChannels)
                    {
                        if(_inProgressChannels.contains(key.channel()))
                        {
                            continue;
                        }
                    }

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
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        synchronized(_inProgressChannels)
        {
            _inProgressChannels.add(socketChannel);
        }
        _threadPoolManager.addTaskToQueue(new AcceptConnectionTask(this, socketChannel));


    }

    @Override
    protected void read(SelectionKey key) throws IOException
    {
        SocketChannel socketChannel = (SocketChannel)key.channel();
        ClientInfo client = (ClientInfo)key.attachment();
        if(!client.isReading())
        {
            synchronized (_inProgressChannels)
            {
                _inProgressChannels.add(socketChannel);
            }
            _threadPoolManager.addTaskToQueue(new ReadTask(this, key));
        }
    }

    @Override
    //computes the hash of the read data packet and adds that the pending write list to client
    public void handleResponse(SocketChannel socketChannel, byte[] bufferBytes)
    {
        try
        {
            byte[] hash = SHA1FromBytes(bufferBytes).getBytes();
            synchronized(_clients)
            {
                _clients.get(socketChannel).addPendingWrite(hash);
            }


        }catch(NoSuchAlgorithmException nsae)
        {
            nsae.printStackTrace();
        }
    }

    @Override
    //sends all pending writes to client associated with key
    protected void write(SelectionKey key) throws IOException
    {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ClientInfo client = (ClientInfo)key.attachment();

        if(!client.isWriting())
        {
            synchronized(_inProgressChannels)
            {
                _inProgressChannels.add(socketChannel);
            }
            _threadPoolManager.addTaskToQueue(new WriteTask(this, key));
        }
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

    public void wakeupSelector()
    {
        _selector.wakeup();
    }

    public List<byte[]> getClientPendingWriteList(SocketChannel socketChannel)
    {
        synchronized(_clients)
        {
            return _clients.get(socketChannel).getPendingWriteList();
        }
    }

    public void clearClientPendingWritesList(SocketChannel socketChannel)
    {
        synchronized(_clients)
        {
            _clients.get(socketChannel).clearPendingWrites();
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
            System.err.println("Invalid arguments\nUsage: java cs455.scaling.server.Server <port-num> <thread-pool-size>");
            System.exit(1);
        }else
        {
            port = Integer.parseInt(args[0]);
            threadPoolSize = Integer.parseInt(args[1]);
        }

        //start server and assign to wildcard address and specified port
        try
        {
            Server server = new Server(null, port, threadPoolSize);
            server.startThreadPool();
            server.run();
        }catch(IOException ioe)
        {
            ioe.printStackTrace();
        }

    }
}
