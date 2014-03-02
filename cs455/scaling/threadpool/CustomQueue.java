package cs455.scaling.threadpool;

/**
 * @author danbox
 * @date 3/1/14.
 */
public interface CustomQueue<E>
{
    public void enqueue(E e);
    public E dequeue();
}
