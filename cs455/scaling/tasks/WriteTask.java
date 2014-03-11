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
public class WriteTask implements Task
{
    private final Server        _server;
    private final SelectionKey _key;

    private final String        _TYPE = "WriteTask";

    public WriteTask(Server server, SelectionKey key)
    {
        _server = server;
        _key = key;
        ClientInfo client = (ClientInfo)_key.attachment();
        synchronized(client)
        {
            client.setWriting(true);
        }
    }

    @Override
    public void run()
    {
        //get list of pending writes, write out on the channel
        SocketChannel socketChannel = (SocketChannel)_key.channel();
        try
        {
            synchronized(socketChannel)
            {
                for(byte[] data : _server.getClientPendingWriteList(socketChannel))
                {
                    ByteBuffer buffer = ByteBuffer.wrap(data);
                    socketChannel.write(buffer);
                    System.out.println("Writing hash: " + new String(data));
                }

                //clear the pending writes for client
                _server.clearClientPendingWritesList(socketChannel);
            }
            //set interest to read
            _server.addRequest(new SocketChannelRequest(socketChannel, SocketChannelRequest._READ));

            _server.wakeupSelector();

            ClientInfo client = (ClientInfo)_key.attachment();
            synchronized(client)
            {
                client.setWriting(false);
            }

            System.out.println("Exiting task");
        }catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    public String getType()
    {
        return _TYPE;
    }
}
