package logica;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class HiloCalificacionNivel implements Runnable
{

    private final int respuestasCorrectas;
    private final int totalPreguntas;
    private final int tiempoTotalSegundos; // <-- NUEVO

    public HiloCalificacionNivel(int respuestasCorrectas, int totalPreguntas, int tiempoTotalSegundos)
    {
        this.respuestasCorrectas = respuestasCorrectas;
        this.totalPreguntas = totalPreguntas;
        this.tiempoTotalSegundos = tiempoTotalSegundos; // <-- NUEVO
    }

    @Override
    public void run()
    {
        System.out.println("Hilo 6 (Calificación) iniciado...");
        double calificacion = ((double) respuestasCorrectas / totalPreguntas) * 100;
        String nivel;

        if (calificacion < 60)
        {
            nivel = "Necesita mejorar";
        } else if (calificacion < 90)
        {
            nivel = "Aprobado";
        } else
        {
            nivel = "Excelente";
        }

        int minutos = tiempoTotalSegundos / 60;
        int segundos = tiempoTotalSegundos % 60;
        String tiempoFormateado = String.format("%02d:%02d", minutos, segundos);

        String mensaje = String.format(
                "Cuestionario finalizado.\n\n"
                + "Tiempo total: %s\n"
                + "Respuestas correctas: %d de %d\n"
                + "Calificación: %.2f%%\n"
                + "Nivel: %s",
                tiempoFormateado,
                respuestasCorrectas,
                totalPreguntas,
                calificacion,
                nivel
        );

        SwingUtilities.invokeLater(() ->
        {
            // Esta línea muestra la ventana de resultados y ESPERA a que el usuario haga clic en "OK".
            JOptionPane.showMessageDialog(null, mensaje, "Resultados Finales", JOptionPane.INFORMATION_MESSAGE);

            // Justo después de que el usuario cierra el diálogo, terminamos la aplicación.
            System.exit(0);
        });
        System.out.println("Hilo 6 (Calificación) terminado.");

    }
}
