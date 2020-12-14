import java.io.BufferedReader;
import java.io.InputStreamReader;

class Main {
    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        // start coding here
        int current = reader.read();
        int words = 0;
        boolean isWord = false;
        int space = ' ';

        while (current != -1) {
            if (current == space && isWord) {
                words++;
                isWord = false;
            } else if (current != space && !isWord) {
                isWord = true;
            }

            current = reader.read();
        }

        if (isWord) {
            words++;
        }

        reader.close();
        System.out.println(words);
    }
}