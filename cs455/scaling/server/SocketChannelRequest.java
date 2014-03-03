package cs455.scaling.server;

import java.nio.channels.SocketChannel;

/**
 * @author danbox
 * @date 3/2/14.
 */
public class SocketChannelRequest
{
    public static final int _REGISTER   = 1;
    public static final int _DEREGISTER = 2;
    public static final int _READ       = 3;
    public static final int _WRITE      = 4;

    private final SocketChannel   _socketChannel;
    private final int             _type;

    public SocketChannelRequest(SocketChannel socketChannel, int type)
    {
        _socketChannel = socketChannel;
        _type = type;
    }

    public SocketChannel getSocketChannel()
    {
        return _socketChannel;
    }

    public int getType()
    {
        return _type;
    }
}
