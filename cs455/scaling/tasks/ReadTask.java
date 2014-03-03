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
        try
        {
            ClientInfo client = new ClientInfo(_socketChannel);
            _server.addClient(_socketChannel, client);

            System.out.println("Accepting incoming connection");

            _socketChannel.configureBlocking(false);

            _server.addRequest(new SocketChannelRequest(_socketChannel, SocketChannelRequest._REGISTER));

            _server.wakeupSelector();

            ByteBuffer buffer = ByteBuffer.allocate(Node._BUFSIZE);
            int read = 0;
            try
            {
//            while(buffer.hasRemaining() && read != -1)
//            {
//                System.out.println(read);
//                read = channel.read(buffer);
//            }
                read = _socketChannel.read(buffer);
            }catch(IOException ioe)
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

            buffer.flip();
            byte[] bufferBytes = new byte[read];
            buffer.get(bufferBytes);

            System.out.println("Read: " + bufferBytes.toString());


        }catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        System.out.println("Exiting task");
    }

    public String getType()
    {
        return _TYPE;
    }
}
