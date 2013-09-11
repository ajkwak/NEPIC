package nepic.util;

import java.util.Iterator;

/**
 * 
 * @author AJ Parmidge
 * @param <E>
 */
public class DoubleLinkRing<E> implements Iterable<E> {
    private DoubleLinkNode<E> myStart;
    private int length;

    /**
     * Constructs an empty DoubleLinkRing with a starting dummy node
     */
    public DoubleLinkRing() {
        myStart = new DoubleLinkNode<E>(null);
        length = 0;
    }// no-parameter constructor

    public void clear() {// running time: theta(1)
        myStart.myForward = myStart;
        myStart.myBack = myStart;
        length = 0;
    }// clear

    public boolean isEmpty() {// running time: theta(1)
        boolean isEmpty = myStart.myForward == myStart && myStart.myBack == myStart;
        if (isEmpty ^ size() == 0)
            throw new IllegalStateException("DoubleLinkRing is" + (isEmpty ? " " : " NOT ")
                    + "empty, but size field = " + size());
        return isEmpty;
    }// isEmpty

    // equivalent to addLast
    public void add(E theData) {// running time: theta(1)
        addLast(theData);
    }// add

    public void addFirst(E theData) {// running time: theta(1)
        DoubleLinkNode<E> temp = new DoubleLinkNode<E>(theData, myStart.myForward, myStart);
        myStart.myForward.myBack = temp;
        myStart.myForward = temp;
        length++;
    }// addFirst

    public void addLast(E theData) {// running time: theta(1)
        DoubleLinkNode<E> temp = new DoubleLinkNode<E>(theData, myStart, myStart.myBack);
        myStart.myBack.myForward = temp;
        myStart.myBack = temp;
        length++;
    }// addLast

    public E getFirst() {
        return myStart.myForward.myData;
    }// getFirst

    public E getLast() {
        return myStart.myBack.myData;
    }// getLast

    public boolean remove(E deleteMe) {// running time: theta(n)
        DoubleLinkNode<E> found = search(deleteMe);
        if (found == null)
            return false;
        remove(found);
        return true;
    }// delete

    public E removeFirst() {// running time: theta(1)
        return remove(myStart.myForward);
    }// removeFirst

    public E removeLast() {// running time: theta(1)
        return remove(myStart.myBack);
    }// removeLast

    private E remove(DoubleLinkNode<E> toRemove) {// running time: theta(1)
        toRemove.orphanSelf();
        length--;
        return toRemove.myData;// if toRemove == myStart, returns null (also, myStart, when orphans
                               // self, yields myStart
    }// remove
     //
     // public E removeMin(){//running time: theta(n)
     // return remove(getMin());//if node returned by getMin() is myStart, no effect
     // }//removeMin
     //
     // public E removeMax(){//running time: theta(n)
     // return remove(getMax());
     // }//removeMax
     //
     // public E findMin(){//running time: theta(n)
     // return getMin().myData;//returns null if list is empty (if node returned by getMin() is
     // myStart)
     // }//findMin
     //
     // public E findMax(){//running time: theta(n)
     // return getMax().myData;
     // }//findMax
     //
     // private DoubleLinkNode<E> getMin(){//running time: theta(n)
     // DoubleLinkNode<E> min = myStart.myForward;
     // E minData = min.myData;
     // DoubleLinkNode<E> toCheck = min.myForward;
     // while(toCheck != myStart){
     // if(toCheck.myData.compareTo(minData) < 0){
     // min = toCheck;
     // minData = min.myData;
     // }//if toCheck is the new min
     // toCheck = toCheck.myForward;
     // }//while
     // return min;//note: if ring is empty, will return myStart
     // }//getMin
     //
     // private DoubleLinkNode<E> getMax(){//running time: theta(n)
     // DoubleLinkNode<E> max = myStart.myForward;
     // E minData = max.myData;
     // DoubleLinkNode<E> toCheck = max.myForward;
     // while(toCheck != myStart){
     // if(toCheck.myData.compareTo(minData) > 0){
     // max = toCheck;
     // minData = max.myData;
     // }//if toCheck is the new max
     // toCheck = toCheck.myForward;
     // }//while
     // return max;//note: if ring is empty, will return myStart
     // }//getMax

    public boolean contains(E theData) {// running time: theta(n)
        DoubleLinkNode<E> aux = myStart.myForward;
        while (aux != myStart && !theData.equals(aux.myData)) {
            aux = aux.myForward;
        }// while
        return (aux == myStart ? false : true);
    }// contains

    private DoubleLinkNode<E> search(E theData) {// running time: theta(n)
        DoubleLinkNode<E> aux = myStart.myForward;
        while (aux != myStart && !theData.equals(aux.myData)) {
            aux = aux.myForward;
        }// while
        return (aux == myStart ? null : aux);
    }// search

    public int size() {// running time: theta(1)
        return length;
    }// size

    @Override
    public String toString() {// running time: theta(n)
        String temp = "[";
        String comma = "";
        DoubleLinkNode<E> aux = myStart.myForward;
        while (aux != null && aux != myStart) {
            temp += comma + aux.myData;
            comma = ",";
            aux = aux.myForward;
        }// while
        return temp + "]";
    }// toString

    // public int size_longWay(){
    // int length = 0;
    // DoubleLinkNode<E> runThru = myStart.myForward;
    // while(runThru != myStart){
    // length++;
    // runThru = runThru.myForward;
    // }//while
    // return length;
    // }//size_longWay

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private DoubleLinkNode<E> current = myStart;
            private boolean okToRemove = false;// TODO: don't really need.....

            @Override
            public boolean hasNext() {
                return current.myForward != myStart;
            }// hasNext

            @Override
            public E next() {
                if (!hasNext())
                    throw new java.util.NoSuchElementException();
                current = current.myForward;
                E toReturn = current.myData;
                okToRemove = true;
                return toReturn;
            }// next

            @Override
            public void remove() {
                if (!okToRemove)// TODO: don't necessarily need to do!
                    throw new IllegalStateException();
                DoubleLinkRing.this.remove(current);
                okToRemove = false;
            }// remove()
        };
    }

    public Iterator<E> reverseIterator() {
        return new Iterator<E>() {
            private DoubleLinkNode<E> current = myStart;
            private boolean okToRemove = false;// TODO: don't really need.....

            @Override
            public boolean hasNext() {
                return current.myBack != myStart;
            }// hasNext

            @Override
            public E next() {
                if (!hasNext())
                    throw new java.util.NoSuchElementException();
                current = current.myBack;
                E toReturn = current.myData;
                okToRemove = true;
                return toReturn;
            }// next

            @Override
            public void remove() {
                if (!okToRemove)// TODO: don't necessarily need to do!
                    throw new IllegalStateException();
                DoubleLinkRing.this.remove(current);
                okToRemove = false;
            }// remove()
        };
    }

    /**
     * 
     * @author Barbara Li Santi (except for orphanSelf() method, which was written by AJ Parmidge)
     * @param <E>
     */
    public static class DoubleLinkNode<E> {
        public E myData;
        public DoubleLinkNode<E> myForward;
        public DoubleLinkNode<E> myBack;

        public DoubleLinkNode(E theData, DoubleLinkNode<E> theForward, DoubleLinkNode<E> theBack) {
            myData = theData;
            myForward = theForward;
            myBack = theBack;
        }// 3 parameter constructor

        public DoubleLinkNode(E theData) {
            myData = theData;
            myForward = myBack = this;
        }// 1 parameter constructor

        public void orphanSelf() {
            myBack.myForward = myForward;
            myForward.myBack = myBack;
        }// delete

        @Override
        public String toString() {
            return myData.toString();
        }// toString

    }// DoubleLinkNode

}// DoubleLinkRing