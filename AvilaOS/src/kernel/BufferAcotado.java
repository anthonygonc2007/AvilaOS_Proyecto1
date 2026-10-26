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

    // --- Contabilidad de los Días 9-11 ---
    private static final int COSTO_INSERCION_EXTRACCION = 1;
    private static final int COSTO_LATENCIA_RED = 5; // Ciclos de penalización por acceder a buffer remoto

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
     * Valida si el proceso está accediendo a un buffer en otro computador y cobra latencia.
     */
    private void aplicarLatenciaRed(Pcb proceso) {
        if (proceso != null && !proceso.getComputadorId().equals(this.computadorAnfitrionId)) {
            proceso.incrementarTiempoModoSO(COSTO_LATENCIA_RED);
        }
    }

    /**
     * Intenta producir un elemento utilizando los atributos del Pcb.
     */
    public boolean producir(Pcb proceso, Object item) {
        aplicarLatenciaRed(proceso);

        // Usamos las palabras clave "PRODUCTOR" y "MUTEX" para que el semáforo asigne el estado correcto automáticamente
        if (!espaciosVacios.semWait(proceso, "PRODUCTOR")) {
            return false; // El proceso se bloqueó
        }
        if (!mutex.semWait(proceso, "MUTEX")) {
            espaciosVacios.semSignal(proceso);
            return false;
        }

        // --- SECCIÓN CRÍTICA ---
        buffer[cola] = item;
        cola = (cola + 1) % capacidad;
        elementosActuales++;
        
        // Contabilizar inserción como 1 instrucción en modo SO
        proceso.incrementarTiempoModoSO(COSTO_INSERCION_EXTRACCION);
        proceso.setElementosProcesados(proceso.getElementosProcesados() + 1);
        
        mutex.semSignal(proceso);
        elementosLlenos.semSignal(proceso);

        return true;
    }

    /**
     * Intenta consumir un elemento utilizando los atributos del Pcb.
     */
    public Object consumir(Pcb proceso) {
        aplicarLatenciaRed(proceso);

        // Usamos "CONSUMIDOR" y "MUTEX" para los mapeos de estados de bloqueo
        if (!elementosLlenos.semWait(proceso, "CONSUMIDOR")) {
            return null; // El proceso se bloqueó
        }
        if (!mutex.semWait(proceso, "MUTEX")) {
            elementosLlenos.semSignal(proceso);
            return null;
        }

        // --- SECCIÓN CRÍTICA ---
        Object item = buffer[cabeza];
        buffer[cabeza] = null;
        cabeza = (cabeza + 1) % capacidad;
        elementosActuales--;
        
        // Contabilizar extracción como 1 instrucción en modo SO
        proceso.incrementarTiempoModoSO(COSTO_INSERCION_EXTRACCION);
        proceso.setElementosProcesados(proceso.getElementosProcesados() + 1);

        // Salida de sección crítica
        mutex.semSignal(proceso);
        espaciosVacios.semSignal(proceso);

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