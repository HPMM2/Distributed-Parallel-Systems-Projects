import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;

public class Servidor {
    public static void main(String[] args) throws Exception {
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;

        Socket s = null;
        ServerSocket ss = new ServerSocket(5433);

        
        // Pedimos al usuario el número de hilos
        Scanner scanner = new Scanner(System.in);
        System.out.print("Introduce el número de hilos (particiones): ");
        int numHilos = scanner.nextInt();

        System.out.println("Esperando conexiones");


        while (true) {
            try {
                s = ss.accept();

                System.out.println("Se conectaron desde la IP: " + s.getInetAddress());
                ois = new ObjectInputStream(s.getInputStream());
                oos = new ObjectOutputStream(s.getOutputStream());

                // Recibir el archivo
                File file = (File) ois.readObject();

                // Leer el archivo y almacenar los números en una lista
                BufferedReader reader = new BufferedReader(new FileReader(file));
                List<Integer> numeros = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    numeros.add(Integer.parseInt(line));
                }
                reader.close();

                // Calcular el promedio usando múltiples hilos
                double promedio = calcularPromedio(numeros, numHilos);

                // Enviar el promedio de vuelta al cliente
                oos.writeObject(promedio);
                System.out.println("Promedio enviado");

            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (oos != null) oos.close();
                if (ois != null) ois.close();
                if (s != null) s.close();
                System.out.println("Conexion cerrada!");
            }
        }
    }

    // Método para calcular el promedio utilizando varios hilos
    private static double calcularPromedio(List<Integer> numeros, int numHilos) throws InterruptedException {
        // Tamaño de la partición
        int size = numeros.size();
        int chunkSize = size / numHilos;
        int remainder = size % numHilos; // Para manejar los residuos al dividir
        
        List<ThreadPromedio> hilos = new ArrayList<>();
        int inicio = 0;
        
        // Crear y asignar los hilos
        for (int i = 0; i < numHilos; i++) {
            int fin = inicio + chunkSize + (i < remainder ? 1 : 0); // Asignar el residuo a las primeras particiones
            List<Integer> sublista = numeros.subList(inicio, fin);
            ThreadPromedio hilo = new ThreadPromedio(sublista);
            hilos.add(hilo);
            hilo.start();
            inicio = fin; // Mover el índice inicial para la siguiente partición
        }

        // Esperar a que todos los hilos terminen
        for (ThreadPromedio hilo : hilos) {
            hilo.join();
        }

        // Sumar todos los resultados de los hilos y calcular el promedio final
        double sumaTotal = 0;
        for (ThreadPromedio hilo : hilos) {
            sumaTotal += hilo.getSuma();
        }

        return sumaTotal / size; // Calcular el promedio total
    }
}

// Clase que extiende Thread para calcular la suma de una partición
class ThreadPromedio extends Thread {
    private List<Integer> numeros;
    private double suma = 0;

    public ThreadPromedio(List<Integer> numeros) {
        this.numeros = numeros;
    }

    @Override
    public void run() {
        for (int numero : numeros) {
            suma += numero;
        }
    }

    public double getSuma() {
        return suma;
    }
}
