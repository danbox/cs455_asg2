package cs455.scaling.threadpool;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author danbox
 * @date 3/1/14.
 */
public class SafeQueue<E> implements CustomQueue<E>
{
    private final Queue<E>  _queue = new LinkedList<E>();

    @Override
    public synchronized void enqueue(E e)
    {
        _queue.add(e);

        //notify all threads that an item has been added to the queue
        notifyAll();;
    }

    @Override
    public synchronized E dequeue()
    {
        E e = null;

        while(_queue.isEmpty())
        {
            try
            {
                wait();
            }catch(InterruptedException ie)
            {
                return e;
            }
        }
            e = _queue.remove();
            return e;
    }
}
