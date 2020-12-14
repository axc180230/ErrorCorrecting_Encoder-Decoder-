import java.io.CharArrayWriter;
import java.io.IOException;

class Converter {
    public static char[] convert(String[] words) throws IOException {
        // implement the method
        CharArrayWriter writer = new CharArrayWriter();

        for (String word : words) {
            writer.write(word);
        }

        return writer.toCharArray();
    }
}