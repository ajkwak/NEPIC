package nepic.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ImmutableList<E> implements List<E> {
    private List<E> list;

    public ImmutableList(List<E> list) {
        Verify.notNull(list);
        this.list = list;
    }

    @Override
    public boolean add(E arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int arg0, E arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int arg0, Collection<? extends E> arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object arg0) {
        return list.contains(arg0);
    }

    @Override
    public boolean containsAll(Collection<?> arg0) {
        return list.containsAll(arg0);
    }

    @Override
    public E get(int arg0) {
        return list.get(arg0);
    }

    @Override
    public int indexOf(Object arg0) {
        return list.indexOf(arg0);
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private Iterator<E> mutableItr = list.iterator();

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

    @Override
    public int lastIndexOf(Object arg0) {
        return list.lastIndexOf(arg0);
    }

    @Override
    public ListIterator<E> listIterator() {
        return new ListIterator<E>() {
            private ListIterator<E> mutableItr = list.listIterator();

            @Override
            public void add(E arg0) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean hasNext() {
                return mutableItr.hasNext();
            }

            @Override
            public boolean hasPrevious() {
                return mutableItr.hasPrevious();
            }

            @Override
            public E next() {
                return mutableItr.next();
            }

            @Override
            public int nextIndex() {
                return mutableItr.nextIndex();
            }

            @Override
            public E previous() {
                return mutableItr.previous();
            }

            @Override
            public int previousIndex() {
                return mutableItr.previousIndex();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void set(E arg0) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public ListIterator<E> listIterator(final int arg0) {
        return new ListIterator<E>() {
            private ListIterator<E> mutableItr = list.listIterator(arg0);

            @Override
            public void add(E arg0) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean hasNext() {
                return mutableItr.hasNext();
            }

            @Override
            public boolean hasPrevious() {
                return mutableItr.hasPrevious();
            }

            @Override
            public E next() {
                return mutableItr.next();
            }

            @Override
            public int nextIndex() {
                return mutableItr.nextIndex();
            }

            @Override
            public E previous() {
                return mutableItr.previous();
            }

            @Override
            public int previousIndex() {
                return mutableItr.previousIndex();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void set(E arg0) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public boolean remove(Object arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E remove(int arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E set(int arg0, E arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public List<E> subList(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] arg0) {
        return list.toArray(arg0);
    }

}
