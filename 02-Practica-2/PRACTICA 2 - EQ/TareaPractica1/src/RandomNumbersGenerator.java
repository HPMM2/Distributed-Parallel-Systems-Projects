import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class RandomNumbersGenerator {
    public static void main(String[] args) {
        Random random = new Random();
        int amountOfNumbers = 1000000;
        String fileName = "aleatorios.txt";

        try (FileWriter writer = new FileWriter(fileName)) {
            for (int i = 0; i < amountOfNumbers; i++) {
                int randomNumber = random.nextInt(1000); 
                writer.write(randomNumber + (i < amountOfNumbers - 1 ? "," : ""));
            }
            System.out.println("Archivo generado: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
