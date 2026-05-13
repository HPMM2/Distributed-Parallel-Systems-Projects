import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.Scanner;

public class ThreadedAverageCalculator {
    public static void main(String[] args) throws Exception {
        String fileName = "aleatorios.txt";
        List<Integer> numbers = new ArrayList<>();

        // Lee número máximo de particiones del usuario
        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingrese el numero maximo de particiones (hilos): ");
        int maxPartitions = scanner.nextInt();
        scanner.close();

        // Lee los números aleatorios del archivo y los almacena en numbers
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine();
            String[] numberStrings = line.split(",");
            for (String numberString : numberStrings) {
                numbers.add(Integer.parseInt(numberString));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        int totalNumbers = numbers.size();

        // Ejecuta el cálculo con particiones desde 1 hasta el número máximo
        for (int partitionCount = 1; partitionCount <= maxPartitions; partitionCount++) {
            System.out.println("\n=== Ejecutando con " + partitionCount + " particion(es) ===");
            calculateAverageWithPartitions(numbers, partitionCount, totalNumbers);
        }
    }

    private static void calculateAverageWithPartitions(List<Integer> numbers, int partitionCount, int totalNumbers) throws Exception {
        int partitionSize = totalNumbers / partitionCount;
        ExecutorService executor = Executors.newFixedThreadPool(partitionCount);
        List<Future<Double>> results = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        // Asigna segmentos de la lista a los hilos
        for (int i = 0; i < partitionCount; i++) {
            int start = i * partitionSize;
            int end = (i == partitionCount - 1) ? totalNumbers : (i + 1) * partitionSize;
            List<Integer> segment = numbers.subList(start, end);
            Callable<Double> calculator = new AverageCalculator(segment);
            Future<Double> result = executor.submit(calculator);
            results.add(result);
        }

        try {
            // Obtiene los resultados de los hilos y calcular el promedio total
            double totalSum = 0;

            for (Future<Double> result : results) {
                totalSum += result.get() * partitionSize;
            }

            double overallAverage = totalSum / totalNumbers;
            System.out.println("El promedio total es: " + overallAverage);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        System.out.println("Tiempo de ejecucion: " + executionTime + " milisegundos");
    }
}

class AverageCalculator implements Callable<Double> {
    private List<Integer> numbersToCompute;

    public AverageCalculator(List<Integer> numbersToCompute) {
        this.numbersToCompute = numbersToCompute;
    }

    @Override
    public Double call() {
        double sum = 0;
        for (int num : numbersToCompute) {
            sum += num;
        }
        return sum / numbersToCompute.size();
    }
}