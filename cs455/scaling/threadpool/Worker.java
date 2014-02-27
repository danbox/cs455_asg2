package cs455.scaling.threadpool;

import cs455.scaling.tasks.*;
/**
 * @author Dan Boxler
 */
public class Worker implements Runnable
{
    private Task _currenttask;

    public void run()
    {
        while(true) //need to figure out how to terminate the runnable
        {
            //housekeeping

            if(_currenttask != null)
            {
                _currenttask.run();
                _currenttask = null;
            }

            //more housekeeping
        }
    }

    public void setCurrentTask(Task task)
    {
        _currenttask = task;
    }
}
