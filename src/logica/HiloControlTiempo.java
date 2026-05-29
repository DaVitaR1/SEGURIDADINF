package logica;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class HiloControlTiempo implements Runnable
{

    private final JLabel timerLabel;
    private volatile boolean running = true;
    private int segundosRestantes = 300; // 5 Minutos (Ajusta este valor si quieres más tiempo)
    private final Runnable accionAlTerminar;

    public HiloControlTiempo(JLabel timerLabel, Runnable accionAlTerminar)
    {
        this.timerLabel = timerLabel;
        this.accionAlTerminar = accionAlTerminar;
    }

    @Override
    public void run()
    {
        while (running && segundosRestantes > 0)
        {
            try
            {
                SwingUtilities.invokeLater(() ->
                {
                    int minutos = segundosRestantes / 60;
                    int seg = segundosRestantes % 60;
                    if (segundosRestantes < 60)
                    {
                        timerLabel.setForeground(java.awt.Color.RED);
                    }
                    timerLabel.setText(String.format("Tiempo: %02d:%02d", minutos, seg));
                });
                Thread.sleep(1000);
                segundosRestantes--;
            } catch (InterruptedException e)
            {
                running = false;
            }
        }
        if (running && segundosRestantes == 0)
        {
            SwingUtilities.invokeLater(accionAlTerminar);
        }
    }

    public int getSegundosTranscurridos()
    {
        return 300 - segundosRestantes; // Retorna cuánto tiempo usó
    }

    public void detener()
    {
        this.running = false;
    }
}
