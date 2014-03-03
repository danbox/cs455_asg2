package cs455.scaling.client;

import cs455.scaling.node.Node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * @author  Dan Boxler
 */
public class Client extends Node
{
    private Selector        _selector;
    private SelectionKey    _selectionKey;
    private SocketChannel   _socketChannel;
    private List<String>    _pendingHashes;
    private int             _messageRate;

    public Client(InetAddress serverAddress, int serverPort, int messageRate) throws IOException
    {
        _selector = initSelector();
        _socketChannel = initConnection(serverAddress, serverPort);
        _pendingHashes = new LinkedList<String>();
        _messageRate = messageRate;
    }

    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                _selector.select();

                Iterator<SelectionKey> keys = _selector.selectedKeys().iterator();
                while(keys.hasNext())
                {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if(!key.isValid())
                    {
                        //invalid key skip and continue
                        continue;
                    }

                    //check what are the interests that are active
                    if(key.isConnectable())
                    {
                        finishConnection(key);
                        continue;
                    }
                    if(key.isReadable())
                    {
                        read(key);
                        continue;
                    }
                    if(key.isWritable())
                    {
                        write(key);
                        continue;
                    }
                }
            }catch(IOException ioe)
            {
                ioe.printStackTrace();
            }
        }


    }

    private SocketChannel initConnection(InetAddress serverAddress, int serverPort) throws IOException
    {
        //creating a non-blocking socket channel
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);

        //connect to server
        socketChannel.connect(new InetSocketAddress(serverAddress, serverPort));

        _selector = Selector.open();

        socketChannel.register(_selector, SelectionKey.OP_CONNECT);

        return socketChannel;
    }

    private void finishConnection(SelectionKey key) throws IOException
    {
        //get channel from key
        SocketChannel socketChannel = (SocketChannel)key.channel();

        //finish the connection
        try
        {
            socketChannel.finishConnect();
            System.out.println("Finished connection");
        }catch(IOException ioe)
        {
            //cancel registration
            key.channel();
            return;
        }

        //set to write interest
        key.interestOps(SelectionKey.OP_WRITE);
    }

    @Override
    //checks to see if hashed response from server matches any pending hashes
    protected void handleResponse(SelectionKey key, byte[] bufferBytes)
    {
        String hash = new String(bufferBytes);
        if(_pendingHashes.contains(hash))
        {
            System.out.println("Verified hash " + hash);
            _pendingHashes.remove(hash);
        }else //data was corrupted
        {
            System.out.println("Data was corrupted");
        }
    }

    @Override
    //generate random 8kb byte array, compute and store the hash and send to server
    protected void write(SelectionKey key) throws IOException
    {
        //ensure target production rate
        try
        {
            Thread.sleep(1000/_messageRate);
        }catch(InterruptedException ie)
        {
            ie.printStackTrace();
        }

        //generate random 8kb byte array
        byte[] data = new byte[_BUFSIZE];
        new Random().nextBytes(data);
        System.out.println("Generated: " + data.toString());

        //store hash in pending hashes
        String hash = null;
        try
        {
            hash = SHA1FromBytes(data);
        }catch(NoSuchAlgorithmException nsae)
        {
            nsae.printStackTrace();
        }
        System.out.println("Adding hash " + hash + " to pending hashes");
        _pendingHashes.add(hash);

        //get channel from key
        SocketChannel channel = (SocketChannel)key.channel();

        //write buffer to channel
        ByteBuffer buffer = ByteBuffer.wrap(data);
        channel.write(buffer);

        //set key to read interest
        key.interestOps(SelectionKey.OP_READ);
    }

    //client entry pointString
    public static void main(String[] args)
    {
        InetAddress serverHostname = null;
        int serverListeningPort = 0;
        int messageRate = 0;
        if(args.length != 3)
        {
            System.err.println("Invalid arguments\nUsage: java cs455.scaling.client.Client <server-hostname> <server-listening-port> <message-rate>");
            System.exit(1);
        }else
        {
            try
            {
                serverHostname = InetAddress.getByName(args[0]);
            }catch(UnknownHostException uhe)
            {
                uhe.printStackTrace();
            }
            serverListeningPort = Integer.parseInt(args[1]);
            messageRate = Integer.parseInt(args[2]);
        }

        //create new client
        try
        {
//            new Thread(new Client(serverHostname, serverListeningPort, messageRate));
            new Client(serverHostname, serverListeningPort, messageRate).run();
        }catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}
