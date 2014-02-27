package cs455.scaling.tasks;

/**
 * @author Dan Boxler
 */

//junk task to test queueing
public class AddTask implements Task
{
    private final int _first;
    private final int _second;

    public AddTask(int first, int second)
    {
        _first = first;
        _second = second;
    }
    @Override
    public void run()
    {
        int total = _first + _second;
        System.out.println("Total: " + total);
    }

    @Override
    public String getType()
    {
        return "AddTask";
    }
}
