//Tarea de Monitor Dormilon de Luis Pinillos
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

class MonitorDormilon {
    private Semaphore monitorAwake;    // Despertar al monitor
    private Semaphore monitorBusy;     // Monitor libre u ocupado
    private Semaphore chairsAvailables; // Disponibilidad de sillas en el corredor
    private Semaphore chairInOffice;   // Silla en la oficina (1 silla)

    public MonitorDormilon(int maxChairs) {
        this.monitorAwake = new Semaphore(0); // El monitor comienza dormido
        this.monitorBusy = new Semaphore(1);  // El monitor comienza libre
        this.chairsAvailables = new Semaphore(maxChairs); // Sillas del corredor
        this.chairInOffice = new Semaphore(1); // Silla en la oficina del monitor
    }

    // Método para que los estudiantes obtengan ayuda
    public void getHelp(int studentId) {
        try {
            // Si no hay sillas disponibles en el corredor, el estudiante se va a la sala de cómputo a porgramar
            if (!chairsAvailables.tryAcquire()) {
                System.out.println("Estudiante " + studentId + " no encontró silla en el corredor y se va a programar");
                return;
            }

            System.out.println("Estudiante " + studentId + " está esperando en una silla del corredor.");

            if(!monitorAwake.tryAcquire()){
                // Despertar al monitor si está dormido
                monitorAwake.release();
            }

            // Espera a que el monitor esté disponible para atender
            chairInOffice.acquire();
            monitorBusy.acquire();

            // El estudiante está siendo atendido
            System.out.println("Estudiante " + studentId + " está siendo atendido por el monitor");
            chairsAvailables.release();   // Libera una silla del corredor
            Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 4000)); // Simula el tiempo de ayuda

            // El estudiante se va
            System.out.println("Estudiante " + studentId + " ha terminado de recibir ayuda");
            chairInOffice.release();      // Libera la silla de la oficina del monitor

            // Libera al monitor para que pueda atender a otro estudiante
            monitorBusy.release();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Método para que el monitor espere o duerma si no hay estudiantes
    public void monitorWork() {
        try {
            while (true) {
                // El monitor duerme si no hay estudiantes esperando
                monitorAwake.acquire();

                System.out.println("Monitor despierto");

                // Espera para ver si hay un estudiante que necesite ayuda
                monitorBusy.acquire();

                //Se libera silla del corredor porque el estudiante va a ser atendido
                chairsAvailables.release();
                Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 4000));

                System.out.println("Monitor ha terminado de ayudar.");
                //Se desocupa el monitor
                monitorBusy.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Student implements Runnable {
    private MonitorDormilon monitor;
    private int id;

    public Student(MonitorDormilon monitor, int id) {
        this.monitor = monitor;
        this.id = id;
    }

    @Override
    public void run() {
        try {
            while (true) {
                System.out.println("Estudiante " + id + " está programando.");
                Thread.sleep(ThreadLocalRandom.current().nextInt(2000, 6000)); // Simula tiempo de programación

                // Va a buscar la ayuda del monitor
                monitor.getHelp(id);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Monitor implements Runnable {
    private MonitorDormilon monitor;

    public Monitor(MonitorDormilon monitor) {
        this.monitor = monitor;
    }

    @Override
    public void run() {
        monitor.monitorWork();
    }
}

public class MonitorDormilonProblem {
    public static void main(String[] args) {
        int numberOfStudents = 5;
        int numberOfChairs = 3;

        MonitorDormilon monitorDormilon = new MonitorDormilon(numberOfChairs);

        // Crear e iniciar el hilo del monitor
        Thread monitorThread = new Thread(new Monitor(monitorDormilon));
        monitorThread.start();

        // Crear e iniciar los hilos de los estudiantes
        for (int i = 1; i <= numberOfStudents; i++) {
            Thread studentThread = new Thread(new Student(monitorDormilon, i));
            studentThread.start();
        }
    }
}
