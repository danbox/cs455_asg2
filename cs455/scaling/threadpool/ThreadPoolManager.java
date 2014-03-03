package cs455.scaling.threadpool;

import java.util.List;
import java.util.ArrayList;

import cs455.scaling.datastructures.CustomQueue;
import cs455.scaling.datastructures.SafeQueue;
import cs455.scaling.tasks.AddTask;
import cs455.scaling.tasks.SleepTask;
import cs455.scaling.tasks.Task;

/**
 * @author Dan Boxler
 */
public class ThreadPoolManager
{
    private final List<Worker>                  _workers;
    private final List<Thread>                  _workerThreads;
    private final CustomQueue<Task> _workQueue;

    public ThreadPoolManager(int numThreads)
    {
        _workers = new ArrayList<Worker>();
        _workerThreads = new ArrayList<Thread>();
        _workQueue  = new SafeQueue<Task>();

        for(Integer i = 0; i < numThreads; ++i)
        {
            //create worker
            Worker worker = new Worker(_workQueue, i.toString());
            _workers.add(worker);
            _workerThreads.add(new Thread(worker));
        }
    }

    public void runWorkerThreads()
    {
        for(Thread thread : _workerThreads)
        {
            thread.start();
        }
    }

    public void addTaskToQueue(Task task)
    {
        _workQueue.enqueue(task);
    }

    //entry point used to test thread pool
    public static void main(String[] args)
    {
        ThreadPoolManager tpm = new ThreadPoolManager(10);
        tpm.runWorkerThreads();

        for(int i = 0; i < 100; ++i)
        {
            tpm.addTaskToQueue(new AddTask(i, i + 1));
        }
        System.out.println("Starting task A");
        tpm.addTaskToQueue(new AddTask(1, 5));
        System.out.println("Starting task B");
        tpm.addTaskToQueue(new SleepTask(1000));
    }
}
