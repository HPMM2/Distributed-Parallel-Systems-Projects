import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.List;
import java.io.File;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.HWDiskStore;
import oshi.hardware.NetworkIF;
import oshi.software.os.OperatingSystem;

public class Server {
    private static final int UPDATE_INTERVAL = 5000; // 5 segundos
    private static Map<String, ClientHandler> connectedClients = new ConcurrentHashMap<>();
    private static DefaultTableModel model;
    private static JTable table;

    public static void main(String[] args) {
        String[] columnNames = {
            "ID", "Cliente/Servidor", "Modelo Procesador", "Velocidad (GHz)", 
            "Nucleos", "Disco Duro Total (GB)", "SO", "CPU Libre (%)", 
            "Memoria Libre (GB)", "Ancho Banda Libre (%)", "Disco Libre (GB)", "Estado"
        };
        
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        
        // Configurar el frame
        JFrame frame = new JFrame("Monitor de Sistemas");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JScrollPane(table));
        frame.setSize(1200, 400);
        frame.setVisible(true);

        // Configurar el servidor
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            int port = serverSocket.getLocalPort();
            System.out.println("Servidor iniciado en el puerto: " + port);

            // Agregar datos del servidor
            addServerData();

            // Hilo para actualizar datos del servidor y ordenar filas
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
                updateServerData();
                updateClientsData();
                sortTableByCpuFree();
            }, 0, UPDATE_INTERVAL, TimeUnit.MILLISECONDS);

            // Aceptar conexiones de clientes
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addServerData() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        CentralProcessor processor = hal.getProcessor();
        OperatingSystem os = si.getOperatingSystem();
        List<HWDiskStore> diskStores = hal.getDiskStores();
        
        String processorModel = processor.getProcessorIdentifier().getName();
        double processorSpeed = processor.getProcessorIdentifier().getVendorFreq() / 1e9;
        int cores = processor.getLogicalProcessorCount();
        long diskSize = diskStores.get(0).getSize() / (1024 * 1024 * 1024);
        
        model.addRow(new Object[]{
            "SERVIDOR",
            "Servidor",
            processorModel,
            processorSpeed,
            cores,
            diskSize,
            os.toString(),
            0.0, // CPU Libre
            0.0, // Memoria Libre
            0.0, // Ancho de banda
            0.0, // Disco libre
            "CONECTADO"
        });
    }

    private static void updateServerData() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        CentralProcessor processor = hal.getProcessor();
        GlobalMemory memory = hal.getMemory();
        List<HWDiskStore> diskStores = hal.getDiskStores();
        
        // Calcular CPU libre
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        try { Thread.sleep(1000); } catch (InterruptedException e) { }
        double cpuFree = 100.0 - (processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100);

        // Calcular memoria libre
        double freeMemory = memory.getAvailable() / (1024.0 * 1024 * 1024);

        // Calcular ancho de banda libre (aproximado basado en la utilización de red)
        final double bandwidthFree;
        List<NetworkIF> networks = hal.getNetworkIFs();
        if (!networks.isEmpty()) {
            NetworkIF net = networks.get(0);
            net.updateAttributes();
            long totalBytes = net.getBytesRecv() + net.getBytesSent();
            bandwidthFree = 100.0 - (totalBytes % 100); // Simplificado para ejemplo
        } else {
            bandwidthFree = 100.0;
        }

        // Calcular espacio libre en disco
        File rootDisk = new File("/");
        double diskFree = rootDisk.getUsableSpace() / (1024.0 * 1024 * 1024); // Convertimos a GB

        // Actualizar fila del servidor
        SwingUtilities.invokeLater(() -> {
            model.setValueAt(String.format("%.2f", cpuFree), 0, 7);
            model.setValueAt(String.format("%.2f", freeMemory), 0, 8);
            model.setValueAt(String.format("%.2f", bandwidthFree), 0, 9);
            model.setValueAt(String.format("%.2f", diskFree), 0, 10);
        });
    }

    private static void updateClientsData() {
        // Crear una lista temporal de clientes a remover
        List<String> clientsToRemove = new ArrayList<>();
        
        // Verificar clientes desconectados
        connectedClients.forEach((id, handler) -> {
            if (!handler.isConnected()) {
                updateClientStatus(id, "DESCONECTADO");
                clientsToRemove.add(id);
            }
        });
        
        // Remover clientes desconectados
        clientsToRemove.forEach(connectedClients::remove);
        
        // Actualizar la tabla solo para clientes conectados
        for (int i = 0; i < model.getRowCount(); i++) {
            String clientId = (String) model.getValueAt(i, 0);
            String status = (String) model.getValueAt(i, 11);
            
            // Si es el servidor, continuar con el siguiente
            if (clientId.equals("SERVIDOR")) {
                continue;
            }
            
            // Si el cliente está desconectado, mantener los valores pero en gris
            if (status.equals("DESCONECTADO")) {
                table.setRowSelectionInterval(i, i);
                table.setSelectionBackground(Color.LIGHT_GRAY);
            }
        }
    }

    private static void updateClientStatus(String clientId, String status) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals(clientId)) {
                model.setValueAt(status, i, 11);
                
                if (status.equals("DESCONECTADO")) {
                    // Limpiar todos los campos excepto ID y tipo de cliente
                    model.setValueAt("", i, 2);  // Modelo Procesador
                    model.setValueAt(0.0, i, 3);  // Velocidad
                    model.setValueAt(0, i, 4);    // Núcleos
                    model.setValueAt(0.0, i, 5);  // Disco Duro Total
                    model.setValueAt("", i, 6);   // SO
                    model.setValueAt(0.0, i, 7);  // CPU Libre
                    model.setValueAt(0.0, i, 8);  // Memoria Libre
                    model.setValueAt(0.0, i, 9);  // Ancho Banda Libre
                    model.setValueAt(0.0, i, 10); // Disco Libre
                    
                    // Cambiar color de fondo a gris
                    table.setRowSelectionInterval(i, i);
                    table.setSelectionBackground(Color.LIGHT_GRAY);
                } else {
                    table.clearSelection();
                    table.setSelectionBackground(table.getBackground());
                }
                break;
            }
        }
        sortTableByCpuFree();
    }

    private static void sortTableByCpuFree() {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        
        // Configurar comparador personalizado
        sorter.setComparator(7, (value1, value2) -> {
            int row1 = -1, row2 = -1;
            
            // Encontrar las filas correspondientes a los valores
            for (int i = 0; i < model.getRowCount(); i++) {
                if (model.getValueAt(i, 7).equals(value1)) row1 = i;
                if (model.getValueAt(i, 7).equals(value2)) row2 = i;
            }
            
            // Obtener estados de conexión
            String status1 = model.getValueAt(row1, 11).toString();
            String status2 = model.getValueAt(row2, 11).toString();
            
            // Si uno está desconectado y el otro no, el desconectado va al final
            if (status1.equals("DESCONECTADO") && !status2.equals("DESCONECTADO")) return 1;
            if (!status1.equals("DESCONECTADO") && status2.equals("DESCONECTADO")) return -1;
            
            // Si ambos están desconectados, mantener el orden actual
            if (status1.equals("DESCONECTADO") && status2.equals("DESCONECTADO")) {
                return row1 - row2;
            }
            
            // Si ambos están conectados, ordenar por CPU libre
            try {
                double cpu1 = Double.parseDouble(value1.toString());
                double cpu2 = Double.parseDouble(value2.toString());
                return Double.compare(cpu2, cpu1); // Orden descendente
            } catch (NumberFormatException e) {
                return 0;
            }
        });
        
        table.setRowSorter(sorter);

        // Aplicar orden descendente en la columna 7 (CPU Libre)
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(7, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.sort();
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private String clientId;
        private volatile boolean connected = true;
        private ObjectInputStream ois;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                ois = new ObjectInputStream(socket.getInputStream());
                // Esperar hasta que se reciba el ID del cliente
                Object receivedObject = ois.readObject();
                if (receivedObject instanceof Map) {
                    Map<String, Object> clientSpecs = (Map<String, Object>) receivedObject;
                    clientId = (String) clientSpecs.get("ID");
                    // Agregar al mapa de clientes conectados
                    connectedClients.put(clientId, this);
                    
                    while (connected) {
                        receivedObject = ois.readObject();
                        if (receivedObject instanceof Map) {
                            clientSpecs = (Map<String, Object>) receivedObject;
                            handleClientData(clientSpecs);
                        }
                    }
                }
            } catch (EOFException e) {
                System.out.println("Cliente desconectado: " + clientId);
                connected = false;
            } catch (IOException e) {
                System.out.println("Error de conexión con el cliente: " + clientId);
                connected = false;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (ois != null) {
                        ois.close();
                    }
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Actualizar el estado del cliente en el servidor
                if (clientId != null) {
                    connected = false;
                    updateClientStatus(clientId, "DESCONECTADO");
                }
            }
        }

        private void handleClientData(final Map<String, Object> clientSpecs) {
            SwingUtilities.invokeLater(() -> {
                boolean clientExists = false;
                String clientId = (String) clientSpecs.get("ID");

                for (int i = 0; i < model.getRowCount(); i++) {
                    if (model.getValueAt(i, 0).equals(clientId)) {
                        // Si el cliente existe pero estaba desconectado, actualizar su estado
                        if (model.getValueAt(i, 11).equals("DESCONECTADO")) {
                            model.setValueAt("CONECTADO", i, 11);
                            table.clearSelection();
                            table.setSelectionBackground(table.getBackground());
                        }
                        updateRowData(i, clientSpecs);
                        clientExists = true;
                        break;
                    }
                }

                if (!clientExists) {
                    addNewClient(clientSpecs);
                }
                
                // Forzar reordenamiento después de actualizar
                sortTableByCpuFree();
            });
        }

        private void updateRowData(int row, Map<String, Object> specs) {
            // Solo actualizar si el cliente está conectado
            if (model.getValueAt(row, 11).equals("CONECTADO")) {
                model.setValueAt(specs.get("Modelo del Procesador"), row, 2);
                model.setValueAt(specs.get("Velocidad del Procesador (GHz)"), row, 3);
                model.setValueAt(specs.get("Nucleos"), row, 4);
                model.setValueAt(specs.get("Capacidad del Disco Duro (GB)"), row, 5);
                model.setValueAt(specs.get("Version del Sistema Operativo"), row, 6);
                model.setValueAt(specs.get("CPU Libre"), row, 7);
                model.setValueAt(specs.get("Memoria Libre"), row, 8);
                model.setValueAt(specs.get("Ancho de Banda Libre"), row, 9);
                model.setValueAt(specs.get("Disco Libre"), row, 10);
            }
        }

        private void addNewClient(Map<String, Object> specs) {
            model.addRow(new Object[]{
                specs.get("ID"),
                "Cliente",
                specs.get("Modelo del Procesador"),
                specs.get("Velocidad del Procesador (GHz)"),
                specs.get("Nucleos"),
                specs.get("Capacidad del Disco Duro (GB)"),
                specs.get("Version del Sistema Operativo"),
                specs.get("CPU Libre"),
                specs.get("Memoria Libre"),
                specs.get("Ancho de Banda Libre"),
                specs.get("Disco Libre"),
                "CONECTADO"
            });
        }

        public boolean isConnected() {
            return connected;
        }
    }
}