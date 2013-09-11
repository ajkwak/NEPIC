package nepic.util;

public class Pair<A, B> {
    /**
     * The first element in the {@link Pair}
     */
    public A first;
    /**
     * The second element in the {@link Pair}
     */
    public B second;

    /**
     * Creates a {@link Pair} of the two given objects
     * 
     * @param first the first element in the pair
     * @param second the second element in the pair
     */
    Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public static <A, B> Pair<A, B> newPair(A first, B second) {
        return new Pair<A, B>(first, second);
    }

    @Override
    public String toString() {
        return "Pair(" + first + ", " + second + ")";
    }

}
