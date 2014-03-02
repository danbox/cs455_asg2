package cs455.scaling.server;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

/**
 * @author  Dan Boxler
 */

public class Server
{
    private final int                               _BUFFSIZE = 8192;
    private final int                               _port;
    private Selector                                _selector;
    private final Map<SelectionKey, ClientInfo>     _clients;

    public Server(int port)
    {
        _port = port;
        _clients = new HashMap<SelectionKey, ClientInfo>();
    }

    private void startServer(int port)
    {
        _selector = null;
        ServerSocketChannel  serverChannel = null;
        try
        {
            _selector = Selector.open();

            //set up ServerSocketChannel
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(_port));
            serverChannel.register(_selector, SelectionKey.OP_ACCEPT);

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
                    SelectionKey selectionKey = keys.next();
                    keys.remove();

                    if(!selectionKey.isValid())
                    {
                        continue;
                    }

                    //check what are the interests that are active
                    if(selectionKey.isAcceptable())
                    {
                        //a connection is ready to be completed
                        accept(selectionKey);
                        continue;
                    }
                    if(selectionKey.isReadable())
                    {
                        //can read

                    }
                    if(selectionKey.isWritable())
                    {
                        //can write

                    }
                }
            }

        }catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    private void accept(SelectionKey key) throws IOException
    {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        _clients.put(key, new ClientInfo(socketChannel));

        System.out.println("Accepting incoming connection");

        socketChannel.configureBlocking(false);
        socketChannel.register(_selector, SelectionKey.OP_READ, client);
    }

    private void read(SelectionKey key) throws IOException
    {
        SocketChannel channel = (SocketChannel)key.channel();

        ByteBuffer buffer = ByteBuffer.allocate(_BUFFSIZE);

        int read = 0;
        try
        {
            while(buffer.hasRemaining() && read != -1)
            {
                read = channel.read(buffer);
            }
        }catch(IOException ioe)
        {
            //abnormal termination
            //server.disconnect(key);
            return;
        }

        if(read == -1)
        {
            //connection was terminated by the client
            //server.disconnect(key);
            return;
        }

        buffer.flip();
        byte[] bufferBytes = new byte[_BUFFSIZE];
        buffer.get(bufferBytes);

        //...
        byte[] hash = Arrays.hashCode(bufferBytes);

        _clients.get(key).addPendingWrite(hash);

        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void write(SelectionKey key) throws IOException
    {
        SocketChannel channel = (SocketChannel)key.channel();

        //get list of pending writes, write out on the channel
        for(byte[] data : _clients.get(key).getPendingWriteList())
        {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            channel.write(buffer);
        }

        _clients.get(key).clearPendingWrites();
        key.interestOps(SelectionKey.OP_READ);
    }

    //program entry point
    public static void main(String[] args)
    {

    }
}
