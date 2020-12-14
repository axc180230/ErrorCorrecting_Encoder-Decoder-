import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        // put your code here
        Scanner scanner = new Scanner(System.in);

        int enemyCount = scanner.nextInt();
        String enemyArmy = "";

        if (enemyCount < 1) {
            enemyArmy = "no army";
        } else if (enemyCount <= 19) {
            enemyArmy = "pack";
        } else if (enemyCount <= 249) {
            enemyArmy = "throng";
        } else if (enemyCount <= 999) {
            enemyArmy = "zounds";
        } else {
            enemyArmy = "legion";
        }

        System.out.println(enemyArmy);
    }
}