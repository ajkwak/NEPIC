package nepic.image;

import java.util.TreeMap;

/**
 * 
 * @author AJ Parmidge
 * @since AutoCBFinder_ALpha_v0-9_122212
 * @version AutoCBFinder_Alpha_v0-9-2013-01-29
 * @param <C>
 */
public class ConstraintMap<C extends Constraint<?>> {
    TreeMap<String, C> map;

    public ConstraintMap() {
        map = new TreeMap<String, C>();
    }

    private String makeKey(Object el) {
        return makeKey(el.getClass());
    }

    private String makeKey(Class<?> clazz) {
        return clazz.getName();
    }

    public ConstraintMap<C> addConstraints(C... constraints) {
        for (C constraint : constraints) {
            map.put(makeKey(constraint), constraint);
        }
        return this;
    }

    public <E> E getConstraint(Class<? extends Constraint<E>> type) {
        String key = makeKey(type);
        @SuppressWarnings("unchecked")
        Constraint<E> cons = (Constraint<E>) map.get(key);
        if (cons == null) {
            return null;
        }
        return cons.getVal();
    }

    @Override
    public String toString() {
        String toReturn = "";
        for (String key : map.keySet()) {
            toReturn += "[" + key + "\t\t" + map.get(key) + "]\n";
        }
        return toReturn;
    }

}
