package vistas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingWorker;

public class SplashScreen extends JWindow
{

    private final JProgressBar progressBar;

    public SplashScreen()
    {
        // --- CAMBIO: Tamaño cuadrado para la nueva imagen ---
        int windowSize = 500;
        setSize(windowSize, windowSize);

        // Centrar la ventana en la pantalla
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenDim.width / 2 - getWidth() / 2, screenDim.height / 2 - getHeight() / 2);

        // --- Creación de Componentes ---
        JPanel panel = new JPanel(new BorderLayout());
        // --- CAMBIO: Fondo oscuro para que combine con la imagen ---
        panel.setBackground(new Color(20, 20, 20));

        // --- CAMBIO: Cargar 'logo.jpg' y escalarlo ---
        // Cargar la imagen original
        ImageIcon originalIcon = new ImageIcon(getClass().getResource("/recursos/logo.png"));
        // Escalar la imagen para que quepa en la ventana
        Image scaledImage = originalIcon.getImage().getScaledInstance(windowSize, windowSize, Image.SCALE_SMOOTH);
        ImageIcon logoIcon = new ImageIcon(scaledImage);

        JLabel lblLogo = new JLabel(logoIcon);
        panel.add(lblLogo, BorderLayout.CENTER);

        // Barra de progreso
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        panel.add(progressBar, BorderLayout.SOUTH);

        add(panel);
    }

    // El resto de la clase no necesita cambios
    public void startLoading()
    {
        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>()
        {
            @Override
            protected Void doInBackground() throws Exception
            {
                for (int i = 0; i <= 100; i++)
                {
                    Thread.sleep(30);
                    publish(i);
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks)
            {
                int progress = chunks.get(chunks.size() - 1);
                progressBar.setValue(progress);
            }

            @Override
            protected void done()
            {
                dispose();
                new VistaLoginCodigo().setVisible(true);
            }
        };
        worker.execute();
    }
}
