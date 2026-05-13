public class EjemploHilos {

    // Clase que implementa Runnable para crear un hilo con un tiempo de espera
    static class Tarea implements Runnable {
        private String nombre;
        private int tiempo;

        public Tarea(String nombre, int tiempo) {
            this.nombre = nombre;
            this.tiempo = tiempo; // Tiempo de ejecución simulado en milisegundos
        }

        @Override
        public void run() {
            long inicio = System.currentTimeMillis(); // Marca el tiempo de inicio
            System.out.println(nombre + " empezo a las: " + inicio + " ms");

            try {
                // Simula el tiempo que tarda la tarea
                Thread.sleep(tiempo);
            } catch (InterruptedException e) {
                System.out.println(e);
            }

            long fin = System.currentTimeMillis(); // Marca el tiempo de finalización
            System.out.println(nombre + " termino a las: " + fin + " ms");
            System.out.println("Duracion de " + nombre + ": " + (fin - inicio) + " ms\n");
        }
    }

    public static void main(String[] args) {
        // Crear tres hilos con diferentes tiempos de espera
        Thread hilo1 = new Thread(new Tarea("Hilo 1", 2000)); // 2 segundos
        Thread hilo2 = new Thread(new Tarea("Hilo 2", 4000)); // 4 segundos
        Thread hilo3 = new Thread(new Tarea("Hilo 3", 1000)); // 1 segundo

        // Iniciar los hilos
        long inicioPrograma = System.currentTimeMillis(); // Tiempo de inicio del programa
        System.out.println("Inicio del programa: " + inicioPrograma + " ms\n");

        hilo1.start();
        hilo2.start();
        hilo3.start();

        try {
            // Esperar a que terminen todos los hilos
            hilo1.join();
            hilo2.join();
            hilo3.join();
        } catch (InterruptedException e) {
            System.out.println(e);
        }

        long finPrograma = System.currentTimeMillis(); // Tiempo de finalización del programa
        System.out.println("Todos los hilos han terminado.");
        System.out.println("Tiempo total del programa: " + (finPrograma - inicioPrograma) + " ms");
    }
}
