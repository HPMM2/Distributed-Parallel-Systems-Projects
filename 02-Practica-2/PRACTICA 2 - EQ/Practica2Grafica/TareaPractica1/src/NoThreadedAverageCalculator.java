import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NoThreadedAverageCalculator {
     public static void main(String[] args) {
        String fileName = "aleatorios.txt";
        List<Integer> numbers = new ArrayList<>();

        long startTime = System.currentTimeMillis();
        
        // Leer el archivo y almacenar los números en una lista
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine();
            String[] numberStrings = line.split(",");
            for (String numberString : numberStrings) {
                numbers.add(Integer.parseInt(numberString));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Calcular la suma de todos los números
        long sum = 0;
        for (int num : numbers) {
            sum += num;
        }

        // Calcular el promedio
        double average = (double) sum / numbers.size();

        System.out.println("El promedio total es: " + average);

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        System.out.println("Tiempo de ejecucion: " + executionTime + " milisegundos");
    }
}
