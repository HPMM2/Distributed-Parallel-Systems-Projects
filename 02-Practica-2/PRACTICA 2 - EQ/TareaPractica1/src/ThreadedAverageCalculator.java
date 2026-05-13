import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
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
    private static List<Integer> partitionCounts = new ArrayList<>();
    private static List<Double> executionTimes = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        String fileName = "aleatorios.txt";
        List<Integer> numbers = new ArrayList<>();

        // Lee número máximo de particiones del usuario
        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingrese el numero maximo de particiones (hilos): ");
        int maxPartitions = scanner.nextInt();
        scanner.close();

        // Lee los números aleatorios del archivo y los almacena
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

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Ejecuta el cálculo con particiones desde 1 hasta el número máximo
        for (int partitionCount = 1; partitionCount <= maxPartitions; partitionCount++) {
            System.out.println("\n=== Ejecutando con " + partitionCount + " particion(es) ===");
            double executionTime = calculateAverageWithPartitions(numbers, partitionCount, totalNumbers);
            dataset.addValue(executionTime, "Tiempo de Ejecución (ms)", Integer.toString(partitionCount));
        }

        // Muestra gráfica
        showChart(dataset);
    }

    private static double calculateAverageWithPartitions(List<Integer> numbers, int partitionCount, int totalNumbers) throws Exception {
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

        double totalSum = 0;

        try {
            // Obtiene los resultados de los hilos y calcular el promedio total
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

        return executionTime;
    }

    private static void showChart(DefaultCategoryDataset dataset) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Tiempo de Ejecución con Diferentes Número de Particiones");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Crear gráfico
            JFreeChart chart = ChartFactory.createLineChart(
                    "Tiempo de Ejecución con Diferentes Número de Particiones",
                    "Número de Particiones",
                    "Tiempo de Ejecución (ms)",
                    dataset
            );

            // Crear y agregar panel de gráfico al marco
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
            frame.add(chartPanel);

            // Ajustar el tamaño del marco y hacerlo visible
            frame.pack();
            frame.setVisible(true);
        });
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
