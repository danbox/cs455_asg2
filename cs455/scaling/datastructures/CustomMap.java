package cs455.scaling.datastructures;

/**
 * @author danbox
 * @date 3/2/14.
 */
public interface CustomMap<K, V>
{
    public void put(K k, V v);
    public V get(K k);
    public V remove(K k);
}
