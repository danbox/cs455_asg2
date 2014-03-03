package cs455.scaling.tasks;

import cs455.scaling.datastructures.CustomQueue;
import cs455.scaling.server.ClientInfo;
import cs455.scaling.datastructures.CustomMap;
import cs455.scaling.server.Server;
import cs455.scaling.server.SocketChannelRequest;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;


/**
 * @author danbox
 * @date 3/2/14.
 */
public class AcceptConnectionTask implements Task
{
    private final Server        _server;
    private final SocketChannel _socketChannel;

    private final String        _TYPE = "AcceptConnectionTask";

    public AcceptConnectionTask(Server server, SocketChannel socketChannel)
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
        }catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        System.out.println("Exiting task");
    }

    @Override
    public String getType()
    {
        return _TYPE;
    }
}
