package nepic.util;

import java.util.Comparator;

/**
 * A {@link Pair} whose elements are comparable. The natural ordering of the {@link ComparablePair}
 * is the ordering of the first elements in the pairs.
 * 
 * @author AJ Parmidge
 * 
 * @param <A> The type of the first element in the pair
 * @param <B> The type of the second element in the pair
 */
public class ComparablePair<A extends Comparable<A>, B extends Comparable<B>> extends Pair<A, B>
        implements Comparable<Pair<A, B>> {

    /**
     * Creates a {@link ComparablePair} of the two given {@link Comparable} objects
     * 
     * @param first the first element in the pair
     * @param second the second element in the pair
     */
    public ComparablePair(A first, B second) {
        super(first, second);
    }

    public Comparator<A> firstComparator() {
        return new Comparator<A>() {
            @Override
            public int compare(A arg0, A arg1) {
                return arg0.compareTo(arg1);
            }
        };
    }

    /**
     * Natural ordering: order by first element in the {@link ComparablePair}
     */
    @Override
    public int compareTo(Pair<A, B> other) {
        return first.compareTo(other.first);
    }

    /**
     * @return comparator for ordering {@link ComparablePair} objects by their second elements
     */
    public Comparator<B> secondComparator() {
        return new Comparator<B>() {
            @Override
            public int compare(B arg0, B arg1) {
                return arg0.compareTo(arg1);
            }
        };
    }

}
