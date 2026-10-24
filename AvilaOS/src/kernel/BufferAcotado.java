/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kernel;

public class BufferAcotado {
    private final String idBuffer;
    private final int capacidad;
    private final String computadorAnfitrionId;
    
    // Almacenamiento interno usando un arreglo nativo (prohibido usar ArrayList/Queue de Java)
    private Object[] buffer;
    private int cabeza;
    private int cola;
    private int elementosActuales;

    // Semáforos del productor-consumidor
    private final Semaforo mutex;
    private final Semaforo espaciosVacios;
    private final Semaforo elementosLlenos;

    public BufferAcotado(String idBuffer, int capacidad, String computadorAnfitrionId) {
        this.idBuffer = idBuffer;
        this.capacidad = capacidad;
        this.computadorAnfitrionId = computadorAnfitrionId;
        
        this.buffer = new Object[capacidad];
        this.cabeza = 0;
        this.cola = 0;
        this.elementosActuales = 0;

        this.mutex = new Semaforo(1);
        this.espaciosVacios = new Semaforo(capacidad);
        this.elementosLlenos = new Semaforo(0);
    }

    /**
     * Intenta producir un elemento utilizando los atributos del Pcb.
     */
    public boolean producir(Pcb proceso, Object item) {
        // 1. semWait(espaciosVacios)
        if (!espaciosVacios.semWait(proceso, "Buffer lleno (Esperando espacio en semáforo)")) {
            return false; // El proceso se bloqueó
        }

        // 2. semWait(mutex)
        if (!mutex.semWait(proceso, "Región crítica ocupada (Mutex)")) {
            // Si se bloquea en el mutex, devolvemos el espacio vacío reservado para evitar inconsistencias
            espaciosVacios.semSignal();
            return false;
        }

        // --- SECCIÓN CRÍTICA ---
        buffer[cola] = item;
        cola = (cola + 1) % capacidad;
        elementosActuales++;
        
        // Actualizamos los campos específicos de tu PCB
        proceso.setElementosProcesados(proceso.getElementosProcesados() + 1);

        // 3. Salida de sección crítica: semSignal(mutex) y semSignal(elementosLlenos)
        mutex.semSignal();
        elementosLlenos.semSignal();

        return true;
    }

    /**
     * Intenta consumir un elemento utilizando los atributos del Pcb.
     */
    public Object consumir(Pcb proceso) {
        // 1. semWait(elementosLlenos)
        if (!elementosLlenos.semWait(proceso, "Buffer vacío (Esperando elementos en semáforo)")) {
            return null; // El proceso se bloqueó
        }

        // 2. semWait(mutex)
        if (!mutex.semWait(proceso, "Región crítica ocupada (Mutex)")) {
            elementosLlenos.semSignal();
            return null;
        }

        // --- SECCIÓN CRÍTICA ---
        Object item = buffer[cabeza];
        buffer[cabeza] = null;
        cabeza = (cabeza + 1) % capacidad;
        elementosActuales--;

        // Actualizamos los campos específicos de tu PCB
        proceso.setElementosProcesados(proceso.getElementosProcesados() + 1);

        // 3. Salida de sección crítica: semSignal(mutex) y semSignal(espaciosVacios)
        mutex.semSignal();
        espaciosVacios.semSignal();

        return item;
    }

    public String getIdBuffer() { return idBuffer; }
    public int getCapacidad() { return capacidad; }
    public int getElementosActuales() { return elementosActuales; }
    public String getComputadorAnfitrionId() { return computadorAnfitrionId; }
    public Semaforo getMutex() { return mutex; }
    public Semaforo getEspaciosVacios() { return espaciosVacios; }
    public Semaforo getElementosLlenos() { return elementosLlenos; }
}