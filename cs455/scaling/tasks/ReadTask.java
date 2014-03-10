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

        ByteBuffer buffer = ByteBuffer.allocate(Node._BUFSIZE);
        int read = 0;
        try
        {
            do
            {
                System.out.println(read);
                read = socketChannel.read(buffer);
                System.out.println("REMAINING: " + buffer.remaining());
            }while(buffer.hasRemaining() && read > 0);
//            read = _socketChannel.read(buffer);
        }catch(IOException ioe) //TODO: terminate the connection
        {
            //abnormal termination
//                key.channel().close();
//                key.cancel();
            return;
        }

        if(read == -1)
        {
            //connection was terminated by the client
//                key.channel().close();
//                key.cancel();
            return;
        }

        if(read > 0)
        {
            buffer.flip();
            byte[] bufferBytes = new byte[read];
            buffer.get(bufferBytes);

            System.out.println("Read: " + bufferBytes);

            //handle response
            synchronized(socketChannel)
            {
                _server.handleResponse(socketChannel, bufferBytes);
            }

            //set interest to write
            _server.addRequest(new SocketChannelRequest(socketChannel, SocketChannelRequest._WRITE));

            _server.wakeupSelector();
        }

        ClientInfo client = (ClientInfo)_key.attachment();

        synchronized(client)
        {
            client.setReading(false);
        }

        System.out.println("Exiting task");
    }

    public String getType()
    {
        return _TYPE;
    }
}
