package cs455.scaling.tasks;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Dan Boxler
 */

//junk task to test queueing
public class ReadFileTask implements Task
{
    private final String _filename;

    public ReadFileTask(String filename)
    {
        _filename = filename;
    }

    @Override
    public void run()
    {
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(_filename));
            String line = null;
            while((line = br.readLine()) != null)
            {
                System.out.println(line);
            }
        }catch(FileNotFoundException fnfe)
        {
            fnfe.printStackTrace();
        }catch(IOException ioe)
        {
            ioe.printStackTrace();
        }

    }

    @Override
    public String getType()
    {
        return "ReadFileTask";
    }
}
