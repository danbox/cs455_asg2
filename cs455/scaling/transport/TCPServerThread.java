package cs455.scaling.transport;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by danbox on 2/26/14.
 */
public class TCPServerThread extends Thread
{
    //private Node _node;

    public TCPServerThread()
    {

    }

    @Override
    public void run()
    {
        try
        {
            ServerSocket ss = new ServerSocket(0);

            for(;;)
            {
                Socket socket = ss.accept();

                //create connection
            }
        }catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}
