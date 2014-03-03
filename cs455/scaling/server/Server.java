package cs455.scaling.server;


import cs455.scaling.Node;

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
    private final Map<SocketChannel, ClientInfo>     _clients;

    public Server(InetAddress hostAddress, int port) throws IOException
    {
        _selector = initSelector(hostAddress, port);
        _clients = new HashMap<SocketChannel, ClientInfo>();
//        _buffer = ByteBuffer.allocate(_BUFSIZE);
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
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        ClientInfo client = new ClientInfo(socketChannel);
        _clients.put(socketChannel, client);

        System.out.println("Accepting incoming connection");

        socketChannel.configureBlocking(false);
        socketChannel.register(_selector, SelectionKey.OP_READ, client);
    }

    @Override
    //computes the hash of the read data packet and adds that the pending write list to client
    protected void handleResponse(SelectionKey key, byte[] bufferBytes)
    {
        try
        {
            byte[] hash = SHA1FromBytes(bufferBytes).getBytes();
            _clients.get(key.channel()).addPendingWrite(hash);


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
        for(byte[] data : _clients.get(key.channel()).getPendingWriteList())
        {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            channel.write(buffer);
            System.out.println("Writing: " + data.toString());
        }

        //clear the pending writes for client
        _clients.get(key.channel()).clearPendingWrites();

        //set key to read interest
        key.interestOps(SelectionKey.OP_READ);
    }

    //server entry point
    public static void main(String[] args)
    {
        //get port number
        int port = 0;
        if(args.length != 1) //invalid number of arguments
        {
            System.err.println("Invalid arguments\nUsagserver.startServer();e: java cs455.scaling.server.Server <port-num>");
            System.exit(1);
        }else
        {
            port = Integer.parseInt(args[0]);
        }

        //start server and assign to wildcard address and specified port
        try
        {
//            new Thread(new Server(null, port));
            new Server(null, port).run();
        }catch(IOException ioe)
        {
            ioe.printStackTrace();
        }

    }
}
