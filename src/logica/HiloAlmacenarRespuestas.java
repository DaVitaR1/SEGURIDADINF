package logica;

import datos.DatabaseManager;
import javax.swing.JRadioButton;
import modelo.Opcion;
import modelo.Pregunta;

public class HiloAlmacenarRespuestas implements Runnable
{

    private final int usuarioId;
    private final Pregunta pregunta;
    private final JRadioButton[] radios;
    private final DatabaseManager dbManager;

    public HiloAlmacenarRespuestas(int usuarioId, Pregunta pregunta, JRadioButton[] radios, DatabaseManager dbManager)
    {
        this.usuarioId = usuarioId;
        this.pregunta = pregunta;
        this.radios = radios;
        this.dbManager = dbManager;
    }

    @Override
    public void run()
    {
        int opcionSeleccionadaId = -1;
        // Encontrar qué radio button fue seleccionado
        for (int i = 0; i < radios.length; i++)
        {
            if (radios[i].isSelected())
            {
                // Obtenemos el ID de la opción correspondiente
                opcionSeleccionadaId = pregunta.getOpciones().get(i).getId();
                break;
            }
        }

        if (opcionSeleccionadaId != -1)
        {
            System.out.println("Hilo 4: Guardando respuesta para pregunta ID " + pregunta.getId() + " y opción ID " + opcionSeleccionadaId);
            dbManager.guardarRespuesta(usuarioId, pregunta.getId(), opcionSeleccionadaId);
        } else
        {
            System.out.println("Hilo 4: No se seleccionó ninguna respuesta para la pregunta ID " + pregunta.getId());
        }
    }
}
