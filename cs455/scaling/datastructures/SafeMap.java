package cs455.scaling.datastructures;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dan Boxler
 */
public class SafeMap<K, V> implements CustomMap<K, V>
{
    private final Map<K, V> _map = new HashMap<K, V>();

    public synchronized void put(K k, V v)
    {
        _map.put(k, v);
    }

    public synchronized V get(K k)
    {
        return _map.get(k);
    }

    public synchronized V remove(K k)
    {
        return _map.remove(k);
    }
}
