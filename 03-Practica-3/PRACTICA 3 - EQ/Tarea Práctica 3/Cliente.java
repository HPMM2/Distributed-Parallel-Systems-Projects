import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Cliente {

    public static void main(String[] args) throws Exception {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        Socket s = null;

        try {
            // Conectarse al servidor
            s = new Socket("127.0.0.1", 5433);
            oos = new ObjectOutputStream(s.getOutputStream());
            ois = new ObjectInputStream(s.getInputStream());

            // Generar lista de números aleatorios y escribirlos en un archivo
            List<Integer> numeros = new ArrayList<>();
            Random random = new Random();
            File file = new File("aleatorios.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            for (int i = 0; i < 100000; i++) {
                int numero = random.nextInt(100) + 1;
                numeros.add(numero);
                writer.write(numero + "\n");
            }
            writer.close();

            // Enviar el archivo al servidor
            oos.writeObject(file);

            // Recibir el promedio calculado por el servidor
            Double promedio = (Double) ois.readObject();

            // Mostrar el resultado
            System.out.println("El promedio calculado por el servidor es: " + promedio);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            // Cerrar recursos
            if (ois != null) ois.close();
            if (oos != null) oos.close();
            if (s != null) s.close();
        }
    }
}