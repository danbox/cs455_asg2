package cs455.scaling.server;

import java.nio.channels.Channel;

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

    private final Channel         _channel;
    private final int             _type;

    public SocketChannelRequest(Channel socketChannel, int type)
    {
        _channel = socketChannel;
        _type = type;
    }

    public Channel getChannel()
    {
        return _channel;
    }

    public int getType()
    {
        return _type;
    }
}
