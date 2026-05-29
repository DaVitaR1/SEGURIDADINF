package logica;

import datos.DatabaseManager;

public class HiloEvaluarRespuestas implements Runnable {

    private final int usuarioId;
    private final DatabaseManager dbManager;
    private int respuestasCorrectas = 0;

    public HiloEvaluarRespuestas(int usuarioId, DatabaseManager dbManager) {
        this.usuarioId = usuarioId;
        this.dbManager = dbManager;
    }

    @Override
    public void run() {
        System.out.println("Hilo 5 (Evaluación) iniciado...");
        this.respuestasCorrectas = dbManager.evaluarRespuestas(usuarioId);
        System.out.println("Hilo 5 (Evaluación) terminado. Respuestas correctas: " + this.respuestasCorrectas);
    }
    
    // Este método nos permitirá obtener el resultado después de que el hilo termine
    public int getRespuestasCorrectas() {
        return this.respuestasCorrectas;
    }
}