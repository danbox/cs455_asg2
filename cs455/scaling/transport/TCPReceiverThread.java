package cs455.scaling.transport;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by danbox on 2/26/14.
 */
public class TCPReceiverThread extends Thread
{
    private Socket          _socket;
    //private Node          _node;
    private DataInputStream _din;

    public TCPReceiverThread(Socket socket) throws IOException
    {
        _socket = socket;
        _din = new DataInputStream(_socket.getInputStream());
    }

    @Override
    public void run()
    {
        int dataLength;

        while(_socket != null)
        {
            try
            {
                dataLength = _din.readInt();

                byte[] data = new byte[dataLength];

                _din.readFully(data, 0, dataLength);

                //create event

            }catch(IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
    }
}
