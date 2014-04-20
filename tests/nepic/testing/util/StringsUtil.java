package nepic.testing.util;

public class StringsUtil {
    // Convert CamelCase to uppercase snake_case
    public static String toUpperSnakeCase(String variableName) {
        StringBuilder builder = new StringBuilder(); // TODO: max num snake_case chars?
        char[] nameChars = variableName.toCharArray();
        for (int i = 0; i < nameChars.length; i++) {
            char ch = nameChars[i];
            if (i != 0 && Character.isUpperCase(ch)) {
                builder.append('_').append(ch);
            } else {
                builder.append(Character.toUpperCase(ch));
            }
        }
        return builder.toString();
    }
}
