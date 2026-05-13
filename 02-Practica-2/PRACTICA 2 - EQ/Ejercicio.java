// Ejemplo de Hilos en Java
public class EjemploHilos {
    
    // Clase que implementa Runnable para crear un hilo
    static class MiHilo implements Runnable {
        private String nombre;
        
        public MiHilo(String nombre) {
            this.nombre = nombre;
        }

        @Override
        public void run() {
            for (int i = 1; i <= 5; i++) {
                System.out.println(nombre + " - mensaje " + i);
                try {
                    // Pausa de 500ms entre mensajes para simular trabajo
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
            }
            System.out.println(nombre + " ha terminado.");
        }
    }

    public static void main(String[] args) {
        // Crear dos hilos
        Thread hilo1 = new Thread(new MiHilo("Hilo 1"));
        Thread hilo2 = new Thread(new MiHilo("Hilo 2"));
        
        // Iniciar los hilos
        hilo1.start();
        hilo2.start();
        
        // Esperar que los hilos terminen antes de continuar
        try {
            hilo1.join();
            hilo2.join();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        
        System.out.println("Ambos hilos han terminado.");
    }
}
