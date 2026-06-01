package vistas;

import datos.DatabaseManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import logica.HiloAlmacenarRespuestas;
import logica.HiloControlTiempo;
import logica.HiloEvaluarRespuestas;
import logica.HiloMostrarPreguntas;
import logica.HiloSesion;
import modelo.Pregunta;
import modelo.Usuario;

public class VistaCuestionario extends JFrame
{

    // --- Componentes de la UI ---
    private final JLabel lblTituloPregunta;
    private final JTextArea txtPregunta;
    private final JRadioButton radioOpcion1, radioOpcion2, radioOpcion3, radioOpcion4;
    private final ButtonGroup grupoOpciones;
    private final JButton btnSiguiente;
    private final JLabel lblTimer;

    // --- Lógica del Cuestionario ---
    private final DatabaseManager dbManager;
    private final List<Pregunta> listaPreguntas;
    private int preguntaActualIndex = 0;
    private final Usuario usuarioActual;
    private final String temaActual; // NUEVO: Variable para el tema
    private final HiloControlTiempo tareaTiempo;
    private final HiloSesion tareaSesion;

    // Constructor modificado para recibir el tema
    public VistaCuestionario(Usuario usuario, String tema)
    {
        this.usuarioActual = usuario;
        this.temaActual = tema;
        this.dbManager = new DatabaseManager();

        // --- Paleta de Colores Estética Oscura ---
        Color colorFondo = new Color(45, 45, 45);
        Color colorComponente = new Color(60, 60, 60);
        Color colorTexto = Color.WHITE;
        Color colorBorde = Color.GRAY;

        // --- Carga de preguntas usando el TEMA seleccionado ---
        listaPreguntas = dbManager.getPreguntasPorTema(temaActual);

        if (listaPreguntas.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "No se encontraron preguntas para el tema: " + temaActual, "Error", JOptionPane.ERROR_MESSAGE);
            // Cerramos solo esta ventana si es posible, o salimos si es crítico
            System.exit(0);
        }
        Collections.shuffle(listaPreguntas);

        // --- Configuración de la Ventana ---
        setTitle("Cuestionario: " + temaActual + " - Usuario: " + usuarioActual.getNombreUsuario());

        // --- CÓDIGO PARA EL ICONO ---
        ImageIcon icon = new ImageIcon(getClass().getResource("/recursos/logo.png"));
        setIconImage(icon.getImage());

        setSize(600, 450); // Un poco más grande para que se vea mejor
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        getContentPane().setBackground(colorFondo);

        // --- Inicialización de Componentes ---
        lblTituloPregunta = new JLabel("Pregunta N° 1");
        txtPregunta = new JTextArea();
        radioOpcion1 = new JRadioButton();
        radioOpcion2 = new JRadioButton();
        radioOpcion3 = new JRadioButton();
        radioOpcion4 = new JRadioButton();
        grupoOpciones = new ButtonGroup();
        btnSiguiente = new JButton("Siguiente");
        lblTimer = new JLabel("Tiempo: 05:00");

        // --- Aplicar Estilos ---
        lblTituloPregunta.setForeground(colorTexto);
        lblTituloPregunta.setFont(new Font("Arial", Font.BOLD, 14));

        lblTimer.setFont(new Font("Arial", Font.BOLD, 16));
        lblTimer.setForeground(new Color(100, 150, 255));

        txtPregunta.setBackground(colorComponente);
        txtPregunta.setForeground(colorTexto);
        txtPregunta.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPregunta.setBorder(BorderFactory.createLineBorder(colorBorde));
        txtPregunta.setEditable(false);
        txtPregunta.setLineWrap(true);
        txtPregunta.setWrapStyleWord(true);

        JRadioButton[] radios =
        {
            radioOpcion1, radioOpcion2, radioOpcion3, radioOpcion4
        };
        for (JRadioButton radio : radios)
        {
            radio.setBackground(colorFondo);
            radio.setForeground(colorTexto);
            radio.setFont(new Font("Arial", Font.PLAIN, 13));
            grupoOpciones.add(radio);
        }

        btnSiguiente.setBackground(colorComponente);
        btnSiguiente.setForeground(colorTexto);
        btnSiguiente.setFocusPainted(false);

        // --- Posicionamiento ---
        lblTituloPregunta.setBounds(20, 20, 200, 25);
        lblTimer.setBounds(450, 20, 120, 25);
        txtPregunta.setBounds(20, 50, 540, 100);

        radioOpcion1.setBounds(30, 170, 500, 25);
        radioOpcion2.setBounds(30, 210, 500, 25);
        radioOpcion3.setBounds(30, 250, 500, 25);
        radioOpcion4.setBounds(30, 290, 500, 25);

        btnSiguiente.setBounds(220, 340, 120, 40);

        // --- Añadir Componentes ---
        add(lblTituloPregunta);
        add(lblTimer);
        add(txtPregunta);
        add(radioOpcion1);
        add(radioOpcion2);
        add(radioOpcion3);
        add(radioOpcion4);
        add(btnSiguiente);

        // --- Lógica de Eventos ---
        btnSiguiente.addActionListener(e ->
        {
            if (grupoOpciones.getSelection() == null)
            {
                JOptionPane.showMessageDialog(this, "Por favor, selecciona una respuesta.", "Respuesta Requerida", JOptionPane.WARNING_MESSAGE);
                return;
            }
            iniciarHiloAlmacenar();
            preguntaActualIndex++;
            mostrarSiguientePregunta();
        });

        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                if (JOptionPane.showConfirmDialog(VistaCuestionario.this,
                        "¿Estás seguro de que quieres salir? Tu progreso se perderá.", "Cerrar Cuestionario",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
                {
                    tareaTiempo.detener();
                    tareaSesion.detener();
                    System.exit(0);
                }
            }
        });

        // --- Iniciar Hilos de Fondo ---
        // Configurar qué pasa cuando se acaba el tiempo
        Runnable alTerminarTiempo = () ->
        {
            JOptionPane.showMessageDialog(this, "¡Se acabó el tiempo!", "Tiempo Agotado", JOptionPane.WARNING_MESSAGE);
            finalizarCuestionario();
        };

        tareaTiempo = new HiloControlTiempo(lblTimer, alTerminarTiempo);
        new Thread(tareaTiempo).start();

        tareaSesion = new HiloSesion(usuarioActual.getNombreUsuario());
        new Thread(tareaSesion).start();

        // --- Carga Inicial ---
        mostrarSiguientePregunta();
    }

    private void mostrarSiguientePregunta()
    {
        if (preguntaActualIndex < listaPreguntas.size())
        {
            Pregunta preguntaActual = listaPreguntas.get(preguntaActualIndex);
            JRadioButton[] radios =
            {
                radioOpcion1, radioOpcion2, radioOpcion3, radioOpcion4
            };

            // Hilo para actualizar la UI con la pregunta
            HiloMostrarPreguntas tareaMostrar = new HiloMostrarPreguntas(preguntaActual, lblTituloPregunta, txtPregunta, radios, preguntaActualIndex + 1);
            new Thread(tareaMostrar).start();
        } else
        {
            // Si se acabaron las preguntas, finalizamos
            finalizarCuestionario();
        }
    }

    /**
     * Método centralizado para finalizar el cuestionario, calcular nota y
     * mostrar el certificado. Se llama al terminar preguntas O al acabar el
     * tiempo.
     */
    private void finalizarCuestionario()
    {
        // Desactivar controles para evitar doble envío
        btnSiguiente.setEnabled(false);

        // Detener hilos
        tareaTiempo.detener();
        tareaSesion.detener();

        // Cerrar esta ventana
        this.dispose();

        try
        {
            // 1. Evaluar respuestas en un hilo separado (BD)
            HiloEvaluarRespuestas tareaEvaluar = new HiloEvaluarRespuestas(usuarioActual.getId(), dbManager);
            Thread hiloEvaluar = new Thread(tareaEvaluar);
            hiloEvaluar.start();
            hiloEvaluar.join(); // Esperamos a que termine la consulta

            int correctas = tareaEvaluar.getRespuestasCorrectas();
            // Calcular porcentaje
            double calificacion = ((double) correctas / listaPreguntas.size()) * 100;

            // 2. Abrir el CERTIFICADO
            SwingUtilities.invokeLater(() ->
            {
                // Asegúrate de tener la clase VistaCertificado creada como vimos en el Paso 4
                new VistaCertificado(usuarioActual, calificacion, temaActual).setVisible(true);
            });

        } catch (InterruptedException e)
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Ocurrió un error al procesar resultados.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void iniciarHiloAlmacenar()
    {
        if (preguntaActualIndex < listaPreguntas.size())
        {
            Pregunta preguntaRespondida = listaPreguntas.get(preguntaActualIndex);
            JRadioButton[] radios =
            {
                radioOpcion1, radioOpcion2, radioOpcion3, radioOpcion4
            };
            HiloAlmacenarRespuestas tareaAlmacenar = new HiloAlmacenarRespuestas(usuarioActual.getId(), preguntaRespondida, radios, dbManager);
            new Thread(tareaAlmacenar).start();
        }
    }
}
