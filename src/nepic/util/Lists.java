package nepic.util;

import java.util.ArrayList;

public class Lists {

    public static <E> ArrayList<E> newArrayList(E... elements) {
        ArrayList<E> list = new ArrayList<E>();
        for (int i = 0; i < elements.length; i++) {
            list.add(elements[i]);
        }
        return list;
    }

    public static <E> ArrayList<E> newArrayListStrict(E... elements) {
        ArrayList<E> list = new ArrayList<E>(elements.length);
        for (int i = 0; i < elements.length; i++) {
            list.add(elements[i]);
        }
        return list;
    }

    public static <E> ArrayList<E> newArrayList(Iterable<? extends E> elements) {
        ArrayList<E> list = new ArrayList<E>();
        for (E element : elements) {
            list.add(element);
        }
        return list;
    }

}
