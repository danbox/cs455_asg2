package cs455.scaling.threadpool;

import cs455.scaling.datastructures.CustomQueue;
import cs455.scaling.tasks.*;
/**
 * @author Dan Boxler
 */
public class Worker implements Runnable
{
    private final CustomQueue<Task> _workQueue;
    private final String            _name;

    public Worker(CustomQueue<Task> workQueue, String name)
    {
        _workQueue = workQueue;
        _name = name;
    }

    @Override
    public void run()
    {
        while(true) //need to figure out how to terminate the runnable
        {
            //housekeeping

            Task task = _workQueue.dequeue();
            System.out.println("Task " + task.getType() + " taken by: " + _name);

            task.run();

            System.out.println("Task " + task.getType() + " completed by: " + _name);


            //more housekeeping
        }
    }

}
