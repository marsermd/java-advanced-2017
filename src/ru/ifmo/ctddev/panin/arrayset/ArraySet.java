package ru.ifmo.ctddev.panin.arrayset;

import com.sun.javaws.exceptions.InvalidArgumentException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Created by marsermd on 20.02.2017.
 */
public class ArraySet<E> implements NavigableSet<E>
{
    private final Comparator<? super E> comparator;
    private final E[] values;
    private final int from, to;

    public ArraySet()
    {
        this((E[])new Object[0]);
    }

    public ArraySet(Collection<E> collection)
    {
        this((E[]) collection.toArray());
    }

    public ArraySet(Collection<E> collection, Comparator<? super E> comparator)
    {
        this.comparator = comparator;

        TreeSet<E> treeSet = new TreeSet<E>(comparator);
        treeSet.addAll(collection);

        values = (E[]) treeSet.toArray();

        from = 0;
        to = values.length;
    }

    public ArraySet(E[] array)
    {
        this(array, null);
    }

    public ArraySet(E[] array, Comparator<? super E> comparator)
    {
        this(Arrays.asList(array), comparator);
    }

    private ArraySet(ArraySet<E> other, int from, int to)
    {
        this.comparator = other.comparator;
        this.from = from;
        this.to = to;

        if (from > to)
        {
            throw new IllegalArgumentException("from must be greater than to");
        }

        values = other.values;
    }

    @Override
    public Comparator<? super E> comparator()
    {
        return comparator;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement)
    {
        int from = getElementPos(fromElement);
        int to = getElementPos(toElement);
        if (from > to)
        {
            throw new IllegalArgumentException("from greater than to");
        }
        return new ArraySet<E>(this, from, to);
    }

    @Override
    public SortedSet<E> headSet(E toElement)
    {
        int pos = ceilingPos(toElement);
        if (pos == -1)
        {
            pos = to;
        }
        return new ArraySet<E>(this, from, pos);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement)
    {
        int pos = ceilingPos(fromElement);
        if (pos == -1)
        {
            pos = to;
        }
        return new ArraySet<E>(this, pos, to);
    }

    private int getElementPos(E element)
    {
        int pos;
        pos = findPos(element);
        if (pos < 0)
        {
            pos = -1 - pos;
        }
        return pos;
    }

    @Override
    public E first()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException();
        }
        return values[0];
    }

    @Override
    public E last()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException();
        }
        return values[size() - 1];
    }

    @Override
    public int size()
    {
        return values == null ? 0 : to - from;
    }

    @Override
    public boolean isEmpty()
    {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o)
    {
        int pos;
        pos = findPos(o);
        return pos >= 0;
    }

    private int findPos(Object o)
    {
        return Arrays.binarySearch(values, from, to, (E) o, comparator);
    }

    private int findPos(Object o, boolean equals, boolean greater)
    {
        int pos = findPos(o);
        if (pos < 0)
        {
            pos = -1 - pos;
            if (!greater)
            {
                pos = pos - 1;
            }
        }
        else
        {
            if (!equals)
            {
                if (greater)
                {
                    pos = pos + 1;
                }
                else
                {
                    pos = pos - 1;
                }
            }
        }

        if (pos < from || pos >= to)
        {
            return -1;
        }

        return pos;
    }

    private class ForwardIterator implements Iterator<E>
    {
        int pos = -1;

        @Override
        public boolean hasNext()
        {
            return pos < size() - 1;
        }

        @Override
        public E next()
        {
            pos++;
            return values[pos];
        }
    }

    private class ReversedIterator implements Iterator<E>
    {
        int pos = size();

        @Override
        public boolean hasNext()
        {
            return pos > 0;
        }

        @Override
        public E next()
        {
            pos--;
            return values[pos];
        }
    }

    @Override
    public E lower(E e)
    {
        int pos = lowerPos(e);
        if (pos == -1)
        {
            return null;
        }
        return values[pos];
    }

    private int lowerPos(E e)
    {
        return findPos(e, false, false);
    }

    @Override
    public E floor(E e)
    {
        int pos = floorPos(e);
        if (pos == -1)
        {
            return null;
        }
        return values[pos];
    }

    private int floorPos(E e)
    {
        return findPos(e, true, false);
    }

    @Override
    public E ceiling(E e)
    {
        int pos = ceilingPos(e);
        if (pos == -1)
        {
            return null;
        }
        return values[pos];

    }

    private int ceilingPos(E e)
    {
        return findPos(e, true, true);
    }

    @Override
    public E higher(E e)
    {
        int pos = higherPos(e);
        if (pos == -1)
        {
            return null;
        }
        return values[pos];
    }

    private int higherPos(E e)
    {
        return findPos(e, false, true);
    }

    @Override
    public E pollFirst()
    {
        throw new UnsupportedOperationException("immutable");
    }

    @Override
    public E pollLast()
    {
        throw new UnsupportedOperationException("immutable");
    }

    @Override
    public Iterator<E> iterator()
    {
        return new ForwardIterator();
    }

    @Override
    public NavigableSet<E> descendingSet()
    {
        Comparator<? super E> newComparator;
        if (comparator == null)
        {
            newComparator = new Comparator<E>()
            {
                @Override
                public int compare(E o1, E o2)
                {
                    return -((Comparable<E>) o1).compareTo(o2);
                }
            };
        }
        else
        {
            newComparator = Collections.reverseOrder(comparator);
        }
        return new ArraySet<E>((E[])toArray(), newComparator);
    }

    @Override
    public Iterator<E> descendingIterator()
    {
        return new ReversedIterator();
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive)
    {
        return headSet(toElement, toInclusive).tailSet(fromElement, fromInclusive);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive)
    {
        int pos;
        if (!inclusive)
        {
            pos = ceilingPos(toElement);
        }
        else
        {
            pos = higherPos(toElement);
        }

        if (pos == -1)
        {
            pos = to;
        }
        return new ArraySet<E>(this, from, pos);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive)
    {
        int pos;
        if (inclusive)
        {
            pos = ceilingPos(fromElement);
        }
        else
        {
            pos = higherPos(fromElement);
        }

        if (pos == -1)
        {
            pos = to;
        }
        return new ArraySet<E>(this, pos, to);
    }

    @Override
    public Object[] toArray()
    {
        return Arrays.copyOfRange(values, from, to);
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        if (a.length < size())
        {
            return Arrays.copyOfRange((T[]) values, from, to);
        }
        else
        {
            System.arraycopy(values, from, a, 0, size());
            return a;
        }
    }

    @Override
    public boolean add(E e)
    {
        throw new UnsupportedOperationException("immutable");
    }

    @Override
    public boolean remove(Object o)
    {
        throw new UnsupportedOperationException("immutable");
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        for (Object obj: c)
        {
            if (!contains(obj))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c)
    {
        throw new UnsupportedOperationException("immutable");
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        throw new UnsupportedOperationException("immutable");
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        throw new UnsupportedOperationException("immutable");
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException("immutable");
    }
}
