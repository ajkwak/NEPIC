package nepic.util;

import java.util.Iterator;

/**
 * Immutable wrapper for an iterable
 * 
 * @author Ameliap
 * 
 * @param <E>
 */
public class ImmutableIterable<E> implements Iterable<E> {
    final Iterable<E> mutable;

    public ImmutableIterable(Iterable<E> toCopy) {
        mutable = toCopy;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            Iterator<E> mutableItr = mutable.iterator();

            @Override
            public boolean hasNext() {
                return mutableItr.hasNext();
            }

            @Override
            public E next() {
                return mutableItr.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

}
