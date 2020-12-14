import java.io.BufferedReader;
import java.io.InputStreamReader;

class Main {
    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        // start coding here
        StringBuilder sb = new StringBuilder();
        int current = reader.read();

        while (current != -1) {
            sb.insert(0, (char) current);
            current = reader.read();
        }

        reader.close();

        System.out.println(sb.toString());
    }
}