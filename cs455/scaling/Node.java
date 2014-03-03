package cs455.scaling;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author danbox
 * @date 3/2/14.
 */
public abstract class Node implements Runnable
{
    protected final int     _BUFSIZE = 8129;
    protected Selector      _selector;

    @Override
    public abstract void run();

    protected Selector initSelector() throws IOException
    {
        return Selector.open();
    }

    protected void read(SelectionKey key) throws IOException
    {
        SocketChannel channel = (SocketChannel)key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(_BUFSIZE);
        int read = 0;
        try
        {
//            while(buffer.hasRemaining() && read != -1)
//            {
//                System.out.println(read);
//                read = channel.read(buffer);
//            }
            read = channel.read(buffer);
        }catch(IOException ioe)
        {
            //abnormal termination
            key.channel().close();
            key.cancel();
            return;
        }

        if(read == -1)
        {
            //connection was terminated by the client
            key.channel().close();
            key.cancel();
            return;
        }

        buffer.flip();
        byte[] bufferBytes = new byte[read];
        buffer.get(bufferBytes);

        System.out.println("Read: " + bufferBytes.toString());
        handleResponse(key, bufferBytes);

        key.interestOps(SelectionKey.OP_WRITE);
    }

    protected abstract void handleResponse(SelectionKey key, byte[] bufferBytes);

    protected abstract void write(SelectionKey key) throws IOException;

    protected String SHA1FromBytes(byte[] data) throws NoSuchAlgorithmException
    {
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        byte[] hash = digest.digest(data);
        BigInteger hashInt = new BigInteger(1, hash);

        return hashInt.toString(16);
    }
}
