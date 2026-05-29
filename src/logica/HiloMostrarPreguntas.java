package logica;

import java.util.List;
import javax.swing.*;
import modelo.Opcion;
import modelo.Pregunta;

public class HiloMostrarPreguntas implements Runnable
{

    private final Pregunta pregunta;
    private final JLabel lblTitulo;
    private final JTextArea txtPregunta;
    private final JRadioButton[] radios;
    private final int numeroPregunta;

    public HiloMostrarPreguntas(Pregunta pregunta, JLabel lblTitulo, JTextArea txtPregunta, JRadioButton[] radios, int numeroPregunta)
    {
        this.pregunta = pregunta;
        this.lblTitulo = lblTitulo;
        this.txtPregunta = txtPregunta;
        this.radios = radios;
        this.numeroPregunta = numeroPregunta;
    }

    @Override
    public void run()
    {
        // Usamos SwingUtilities.invokeLater para actualizar la UI de forma segura desde un hilo
        SwingUtilities.invokeLater(() ->
        {
            lblTitulo.setText("Pregunta N° " + numeroPregunta);
            txtPregunta.setText(pregunta.getTextoPregunta());

            List<Opcion> opciones = pregunta.getOpciones();
            for (int i = 0; i < radios.length; i++)
            {
                if (i < opciones.size())
                {
                    radios[i].setText(opciones.get(i).getTextoOpcion());
                    radios[i].setVisible(true);
                } else
                {
                    radios[i].setVisible(false);
                }
            }
            // Limpia la selección anterior
            radios[0].getParent().getComponent(0).requestFocus(); // Hack para limpiar foco
        });
    }
}
