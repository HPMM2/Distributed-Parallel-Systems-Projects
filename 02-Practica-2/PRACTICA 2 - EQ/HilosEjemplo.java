class MiHilo implements Runnable {
    private int id;
    
    public MiHilo(int id) {
        this.id = id;
    }

    @Override
    public void run() {
        long inicio = System.currentTimeMillis();  // Tiempo de inicio
        System.out.println("Hilo " + id + " ha comenzado.");
        
        try {
            // Simulamos una tarea que toma tiempo
            Thread.sleep((long)(Math.random() * 1000));  // Tiempo aleatorio de 0 a 1 segundo
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        long fin = System.currentTimeMillis();  // Tiempo de fin
        System.out.println("\nHilo " + id + " ha terminado.");
        System.out.println("Tiempo: " + (fin - inicio) + " ms.\n");
    }
}

public class HilosEjemplo {
    public static void main(String[] args) {
        int numHilos = 5;  // Número de hilos
        Thread[] hilos = new Thread[numHilos];
        
        // Creación y arranque de los hilos
        for (int i = 0; i < numHilos; i++) {
            hilos[i] = new Thread(new MiHilo(i + 1));  // Crear un hilo con ID
            hilos[i].start();  // Iniciar hilo
        }

        // Esperar a que todos los hilos terminen
        for (int i = 0; i < numHilos; i++) {
            try {
                hilos[i].join();  // Esperar a que termine cada hilo
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        

    }
}
