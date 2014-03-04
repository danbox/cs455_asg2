package cs455.scaling.tasks;

import cs455.scaling.node.Node;
import cs455.scaling.server.ClientInfo;
import cs455.scaling.server.Server;
import cs455.scaling.server.SocketChannelRequest;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author danbox
 * @date 3/2/14.
 */
public class ReadTask implements Task
{
    private final Server _server;
    private final SocketChannel _socketChannel;

    private final String        _TYPE = "ReadTask";

    public ReadTask(Server server, SocketChannel socketChannel)
    {
        _server = server;
        _socketChannel = socketChannel;
    }

    @Override
    public void run()
    {


        ByteBuffer buffer = ByteBuffer.allocate(Node._BUFSIZE);
        int read = 0;
        try
        {
            do
            {
                System.out.println(read);
                read = _socketChannel.read(buffer);
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
            synchronized(_socketChannel)
            {
                _server.handleResponse(_socketChannel, bufferBytes);
            }

            //set interest to write
            _server.addRequest(new SocketChannelRequest(_socketChannel, SocketChannelRequest._WRITE));

            _server.wakeupSelector();
        }

        System.out.println("Exiting task");
    }

    public String getType()
    {
        return _TYPE;
    }
}
