package cs455.scaling.wireformats;

import java.io.IOException;

/**
 * @author Dan Boxler
 */
public interface Event
{
    public int getType();
    public byte[] getBytes() throws IOException;
}
