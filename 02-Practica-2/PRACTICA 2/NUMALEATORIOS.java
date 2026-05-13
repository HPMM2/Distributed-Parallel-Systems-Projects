import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class NUMALEATORIOS {
    public static void main(String[] args) {
        int cantidadNumeros = 100000; // numeros
        String archivo = "numeros.txt";

        try (FileWriter writer = new FileWriter(archivo)) {
            Random random = new Random();
            for (int i = 0; i < cantidadNumeros; i++) {
                int numero = random.nextInt(1000); // Números aleatorios entre 0 y 999
                writer.write(numero + (i < cantidadNumeros - 1 ? "," : ""));
            }
            System.out.println("Archivo listo.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
