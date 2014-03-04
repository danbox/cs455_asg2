package cs455.scaling.server;

import java.net.InetAddress;
import java.net.Socket;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Dan Boxler
 */
public class ClientInfo
{
    private final InetAddress   _hostname;
    private final int           _port;
    private final List<byte[]>  _pendingWriteList;

    public ClientInfo(Channel channel)
    {
        Socket socket = ((SocketChannel) channel).socket();

        _hostname = socket.getInetAddress();
        _port = socket.getPort();
        _pendingWriteList = new LinkedList<byte[]>();
    }

    protected InetAddress getHostname()
    {
        return _hostname;
    }

    protected int getPort()
    {
        return _port;
    }

    protected void addPendingWrite(byte[] dataToWrite)
    {
        synchronized(_pendingWriteList)
        {
            _pendingWriteList.add(dataToWrite);
        }
    }

    protected void clearPendingWrites()
    {
        synchronized(_pendingWriteList)
        {
            _pendingWriteList.clear();
        }
    }

    //performs a deep copy on the pending write list
    protected List<byte[]> getPendingWriteList()
    {
        synchronized(_pendingWriteList)
        {
            List<byte[]> newList = new LinkedList<byte[]>();
            for(byte[] write : _pendingWriteList)
            {
                newList.add(write.clone());
            }
            return newList;
        }
    }
}
