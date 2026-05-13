package libro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.net.*;

public class Cliente {
    public static void main(String[] args) {
        String serverAddress = "25.47.153.25"; // Dirección IP del servidor
        int port = 5355; // Puerto del servidor

        // Crear una ventana para mostrar las características del cliente
        JFrame clientFrame = new JFrame("Cliente");
        clientFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Crear una tabla para mostrar las características
        DefaultTableModel tableModel = new DefaultTableModel();
        JTable table = new JTable(tableModel);
        tableModel.addColumn("Característica");
        tableModel.addColumn("Valor");

        clientFrame.add(new JScrollPane(table));
        clientFrame.pack();
        clientFrame.setVisible(true);

        try {
            Socket socket = new Socket(serverAddress, port);

            // Obtener entrada y salida de datos
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            // Enviar solicitud al servidor para obtener características
            outputStream.writeObject("request");

            // Obtener y enviar características del cliente al servidor
            String[] clientFeatures = getSystemInfo();
            outputStream.writeObject(clientFeatures);

            // Recibir características del servidor (en este caso, se ignoran)
            Object response = inputStream.readObject();
            if (response instanceof String[]) {
                // No mostrar características del servidor
            }

            // Mostrar características del cliente en la tabla
            for (String feature : clientFeatures) {
                String[] parts = feature.split(":");
                addRowToTable(tableModel, parts[0], parts.length > 1 ? parts[1] : "No disponible");
            }

            // Cierra la conexión con el servidor
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para agregar una fila a la tabla
    private static void addRowToTable(DefaultTableModel tableModel, String feature, String value) {
        tableModel.addRow(new String[]{feature, value});
    }

    // Método para obtener las características del sistema del cliente
    private static String[] getSystemInfo() {
        String[] features = new String[5]; // Ajustar el tamaño del arreglo para incluir el nombre de la máquina
        try {
            // Información del sistema operativo
            String osName = System.getProperty("os.name");
            String osVersion = System.getProperty("os.version");
            features[0] = "Nombre de la máquina: " + System.getProperty("user.name"); // Agregar nombre de la máquina
            features[1] = "Sistema operativo: " + osName + " " + osVersion;

            // Información del hardware
            String processorModel = System.getenv("PROCESSOR_IDENTIFIER");
            features[2] = "Modelo del procesador: " + (processorModel != null ? processorModel : "No disponible");

            // Obtener el número de núcleos
            String processorCores = System.getenv("NUMBER_OF_PROCESSORS");
            features[3] = "Número de núcleos: " + (processorCores != null ? processorCores : "No disponible");

            // Tamaño del disco duro
            File file = new File("/");
            long totalDiskSpace = file.getTotalSpace();
            features[4] = "Tamaño del disco duro: " + totalDiskSpace + " bytes";

        } catch (Exception e) {
            e.printStackTrace();
        }
        return features;
    }
}
