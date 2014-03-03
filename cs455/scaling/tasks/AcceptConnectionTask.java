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
    private final Server                                _server;
    private final SelectionKey                          _key;

    private final String                                _TYPE = "AcceptConnectionTask";

    public AcceptConnectionTask(Server server, SelectionKey key)
    {
        _server = server;
        _key = key;
    }

    @Override
    public void run()
    {
        try
        {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel)_key.channel();
            SocketChannel socketChannel = serverSocketChannel.accept();
            ClientInfo client = new ClientInfo(socketChannel);
            _server.addClient(socketChannel, client);

            System.out.println("Accepting incoming connection");

            socketChannel.configureBlocking(false);

            _server.addRequest(new SocketChannelRequest(socketChannel, SocketChannelRequest._REGISTER));

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
