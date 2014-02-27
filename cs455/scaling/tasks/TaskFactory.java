package cs455.scaling.tasks;

/**
 * @author Dan Boxler
 */
public class TaskFactory
{
    private static TaskFactory _instance = new TaskFactory();

    public static TaskFactory getInstance()
    {
        return _instance;
    }

    private TaskFactory()
    {

    }
}
