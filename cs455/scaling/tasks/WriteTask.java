package cs455.scaling.tasks;

import cs455.scaling.node.Node;
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
    private final Server _server;
    private final SocketChannel _socketChannel;

    private final String        _TYPE = "WriteTask";

    public WriteTask(Server server, SocketChannel socketChannel)
    {
        _server = server;
        _socketChannel = socketChannel;
    }

    @Override
    public void run()
    {
        //get list of pending writes, write out on the channel
        try
        {
            synchronized(_socketChannel)
            {
            for(byte[] data : _server.getClientPendingWriteList(_socketChannel))
            {
                ByteBuffer buffer = ByteBuffer.wrap(data);
                _socketChannel.write(buffer);
                System.out.println("Writing: " + new String(data));
            }

            //clear the pending writes for client
            _server.clearClientPendingWritesList(_socketChannel);
            }
            //set interest to read
            _server.addRequest(new SocketChannelRequest(_socketChannel, SocketChannelRequest._READ));

            _server.wakeupSelector();

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
