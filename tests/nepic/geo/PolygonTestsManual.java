package nepic.geo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import nepic.geo.test.ExemplarPolygons;
import nepic.testing.util.StringsUtil;

public class PolygonTestsManual {

    /**
     * @param args
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static void main(String[] args) throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        for (Method method : ExemplarPolygons.class.getMethods()) {
            String methodName = method.getName();
            if (methodName.startsWith("make")) {
                String polygonDesignation = StringsUtil.toUpperSnakeCase(methodName.substring(4));
                System.out.println("\n\n" + polygonDesignation);
                Polygon polygon = (Polygon) method.invoke(null);
                System.out.println(polygon.drawAsAsciiArt());
            }
        }
    }

}
