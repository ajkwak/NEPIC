package nepic.image;

import java.util.TreeMap;

/**
 *
 * @author AJ Parmidge
 * @since AutoCBFinder_ALpha_v0-9_122212
 * @version AutoCBFinder_Alpha_v0-9-2013-01-29
 * @param <C>
 */
public class ConstraintMap {
    TreeMap<String, Object> map;

    public ConstraintMap() {
        map = new TreeMap<String, Object>();
    }

    public ConstraintMap addConstraint(String key, Object constraint) {
        map.put(key, constraint);
        return this;
    }

    public Object getConstraint(String key) {
        return map.get(key);
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
