import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class PromedioHilo implements Callable<Double> {
    private List<Integer> numeros;

    public PromedioHilo(List<Integer> numeros) {
        this.numeros = numeros;
    }

    @Override
    public Double call() {
        int suma = 0;
        for (int numero : numeros) {
            suma += numero;
        }
        return (double) suma / numeros.size();
    }
}

public class PromedioHiloMain {
    public static void main(String[] args) {
        String archivo = "numeros.txt";
        List<Integer> numeros = new ArrayList<>();

        // Leer archivo y almacenar los números en una lista
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] valores = linea.split(",");
                for (String valor : valores) {
                    numeros.add(Integer.parseInt(valor));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Ejecucion sin hilos (lineal)
        long inicioTiempoLineal = System.nanoTime();
        double promedioLineal = calcularPromedioLineal(numeros);
        long finTiempoLineal = System.nanoTime();
        long tiempoLineal = finTiempoLineal - inicioTiempoLineal;
        System.out.println("El promedio total Lineal es: " + promedioLineal);
        System.out.println("Tiempo de ejecucion Lineal: " + tiempoLineal + " nanosegundos");

        // Ejecucion con hilos (paralelo)
        long inicioTiempoParalelo = System.nanoTime();
        double promedioParalelo = calcularPromedioConHilos(numeros, 4);  // 4 hilos
        long finTiempoParalelo = System.nanoTime();
        long tiempoParalelo = finTiempoParalelo - inicioTiempoParalelo;
        System.out.println("El promedio total Paralela es: " + promedioParalelo);
        System.out.println("Tiempo de ejecucion Paralela: " + tiempoParalelo + " nanosegundos");
    }

    // Método para calcular el promedio de forma lineal
    private static double calcularPromedioLineal(List<Integer> numeros) {
        int suma = 0;
        for (int numero : numeros) {
            suma += numero;
        }
        return (double) suma / numeros.size();
    }

    // Método para calcular el promedio usando hilos
    private static double calcularPromedioConHilos(List<Integer> numeros, int cantidadHilos) {
        int totalNumeros = numeros.size();
        int tamanoSegmento = totalNumeros / cantidadHilos;

        ExecutorService executor = Executors.newFixedThreadPool(cantidadHilos);
        List<Future<Double>> resultados = new ArrayList<>();

        for (int i = 0; i < cantidadHilos; i++) {
            int inicio = i * tamanoSegmento;
            int fin = (i == cantidadHilos - 1) ? totalNumeros : inicio + tamanoSegmento;
            PromedioHilo tarea = new PromedioHilo(numeros.subList(inicio, fin));
            Future<Double> resultado = executor.submit(tarea);
            resultados.add(resultado);
        }

        // Obtener los promedios y calcular el promedio total
        double promedioTotal = 0.0;

        try {
            for (Future<Double> resultado : resultados) {
                promedioTotal += resultado.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // Cerrar el executor y obtener el promedio final
        executor.shutdown();
        promedioTotal /= cantidadHilos;

        return promedioTotal;
    }
}