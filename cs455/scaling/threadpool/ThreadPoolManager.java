package cs455.scaling.threadpool;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import cs455.scaling.tasks.Task;

/**
 * @author Dan Boxler
 */
public class ThreadPoolManager
{
    private final ArrayList<Worker>             _workers;
    private final ArrayList<Thread>             _workerThreads;
    private final ConcurrentLinkedQueue<Task>   _workQueue;

    public ThreadPoolManager(int numThreads)
    {
        //housekeeping
        _workers = new ArrayList<Worker>();
        _workerThreads = new ArrayList<Thread>();
        _workQueue  = new ConcurrentLinkedQueue<Task>();

        for(int i = 0; i < numThreads; ++i)
        {
            //create worker
            Worker worker = new Worker();
            _workerThreads.add(new Thread(worker));

            //perhaps more housekeeping
        }
    }

    private void startWorkerThreads()
    {
        for(Thread thread : _workerThreads)
        {
            thread.run();
        }
    }

    private void addTaskToQueue(Task task)
    {
        _workQueue.add(task);
    }
}
