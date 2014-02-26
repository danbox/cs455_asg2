package cs455.scaling.transport;

/**
 * @author Dan Boxler
 */

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPSender
{
    private DataOutputStream 	_dout;

    public TCPSender(Socket socket) throws IOException
    {
        _dout = new DataOutputStream(socket.getOutputStream());
    }

    public void sendData(byte[] dataToSend) throws IOException
    {
        int dataLength = dataToSend.length;
        _dout.writeInt(dataLength);
        _dout.write(dataToSend, 0, dataLength);
        _dout.flush();
    }
}
