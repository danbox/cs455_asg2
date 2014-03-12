package cs455.scaling.tasks;

import cs455.scaling.node.Node;
import cs455.scaling.server.ClientInfo;
import cs455.scaling.server.Server;
import cs455.scaling.server.SocketChannelRequest;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * @author Dan Boxler
 */
public class ReadTask implements Task
{
    private final Server        _server;
    private final SelectionKey  _key;

    private final String        _TYPE = "ReadTask";

    public ReadTask(Server server, SelectionKey key)
    {
        _server = server;
        _key = key;
        ClientInfo client = (ClientInfo)_key.attachment();
        synchronized(client)
        {
            client.setReading(true);
        }
    }

    @Override
    public void run()
    {
        SocketChannel socketChannel = (SocketChannel)_key.channel();

        System.out.println("Beginning read from: " + socketChannel.socket().getInetAddress().getCanonicalHostName());

        ByteBuffer buffer = ByteBuffer.allocate(Node._BUFSIZE);
        int read = 0;
        try
        {
            while(buffer.hasRemaining() && read != -1)
            {
                Thread.sleep(5);
                read = socketChannel.read(buffer);
                System.out.println(read);
                System.out.println("REMAINING: " + buffer.remaining());
            }
        }catch(IOException ioe) //TODO: terminate the connection
        {
            //abnormal termination
            try
            {
                _key.channel().close();
                _key.cancel();
            }catch(IOException ioe2)
            {
                //do nothing
            }
            return;
        }catch(InterruptedException ie)
        {
            ie.printStackTrace();
        }

        if(read == -1)
        {
            //connection was terminated by the client
            try
            {
                _key.channel().close();
                _key.cancel();
            }catch(IOException ioe)
            {
                //do nothing
            }
            return;
        }

            buffer.flip();
            byte[] bufferBytes = new byte[read];
            buffer.get(bufferBytes);

            //handle response
            synchronized(socketChannel)
            {
                _server.handleResponse(socketChannel, bufferBytes);
            }

        System.out.println("Ending read from: " + socketChannel.socket().getInetAddress().getCanonicalHostName());

        ClientInfo client = (ClientInfo)_key.attachment();
        synchronized(client)
    {
        client.setReading(false);
    }
        //set interest to write
        _server.addRequest(new SocketChannelRequest(socketChannel, SocketChannelRequest._WRITE));

        _server.wakeupSelector();

        System.out.println("Exiting task");
    }

    public String getType()
    {
        return _TYPE;
    }
}
