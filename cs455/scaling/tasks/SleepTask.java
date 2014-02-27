package cs455.scaling.tasks;

/**
 * @author Dan Boxler
 */

//junk task to test queueing
public class SleepTask implements Task
{
    private final int _sleepTime;

    public SleepTask(int sleepTime)
    {
        _sleepTime = sleepTime;
    }

    @Override
    public void run()
    {
        System.out.println("Starting sleep for: " + _sleepTime);

        //start sleeping
        try
        {
            Thread.sleep(_sleepTime);
        }catch(InterruptedException ie)
        {
            ie.printStackTrace();
        }

        System.out.println("Done Sleeping");
    }

    @Override
    public String getType()
    {
        return "SleepTask";
    }
}
