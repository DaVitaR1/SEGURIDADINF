package vistas;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.print.*;
import java.util.List;
import javax.swing.*;
import modelo.Usuario;
import modelo.CursoSugerido;
import logica.GestorSugerencias;
import crypto.CryptoManager;
import datos.DatabaseManager;
import java.security.KeyPair;

public class VistaCertificado extends JFrame
{

    private final JPanel panelCertificado;

    public VistaCertificado(Usuario usuario, double calificacion, String tema)
    {
        DatabaseManager dbManager = new DatabaseManager();
        String contenidoCert = usuario.getNombreUsuario() + "|" + tema + "|" +
                               String.format("%.2f", calificacion) + "|" +
                               new java.util.Date().toString();
        String hashCert = CryptoManager.sha256(contenidoCert);
        KeyPair parClaves = CryptoManager.cargarClaves(".");
        String firmaRsa = CryptoManager.firmar(contenidoCert, parClaves.getPrivate());
        dbManager.guardarCertificado(usuario.getId(), tema, calificacion, hashCert, firmaRsa);
        String codigoVerificacion = hashCert.substring(0, 16).toUpperCase();

        setTitle("Certificado Oficial");
        // Aumentamos un poco la altura para que todo quepa holgadamente
        setSize(850, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- 1. PANEL DEL CERTIFICADO ---
        panelCertificado = new JPanel();
        panelCertificado.setLayout(null);
        panelCertificado.setBackground(Color.WHITE);

        // Marco decorativo (ajustado al nuevo tamaño)
        JPanel bordeMarco = new JPanel(null);
        bordeMarco.setBounds(10, 10, 815, 600); // Más alto
        bordeMarco.setBackground(new Color(255, 255, 255));
        bordeMarco.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(20, 40, 80), 5),
                BorderFactory.createLineBorder(new Color(218, 165, 32), 3)
        ));
        panelCertificado.add(bordeMarco);

        // --- ENCABEZADO (Compactado para ganar espacio abajo) ---
        // Icono
        JLabel lblEscudo = new JLabel("★", SwingConstants.CENTER);
        lblEscudo.setFont(new Font("Serif", Font.BOLD, 40));
        lblEscudo.setForeground(new Color(218, 165, 32));
        lblEscudo.setBounds(350, 15, 100, 40); // Más arriba
        bordeMarco.add(lblEscudo);

        // Título
        JLabel lblTitulo = new JLabel("CERTIFICADO DE FINALIZACIÓN", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Serif", Font.BOLD, 30));
        lblTitulo.setForeground(new Color(20, 40, 80));
        lblTitulo.setBounds(50, 55, 700, 35); // Más arriba
        bordeMarco.add(lblTitulo);

        // Texto "Otorgado a"
        JLabel lblOtorgado = new JLabel("Se otorga el presente reconocimiento a:", SwingConstants.CENTER);
        lblOtorgado.setFont(new Font("SansSerif", Font.ITALIC, 15));
        lblOtorgado.setBounds(100, 100, 600, 20); // Subido considerablemente
        bordeMarco.add(lblOtorgado);

        // Nombre del Usuario
        JLabel lblNombre = new JLabel(usuario.getNombreUsuario().toUpperCase(), SwingConstants.CENTER);
        lblNombre.setFont(new Font("Serif", Font.BOLD, 26));
        lblNombre.setForeground(new Color(0, 0, 0));
        lblNombre.setBounds(50, 125, 700, 35);
        bordeMarco.add(lblNombre);

        // Línea decorativa
        JSeparator lineaNombre = new JSeparator();
        lineaNombre.setForeground(Color.BLACK);
        lineaNombre.setBounds(200, 160, 400, 10);
        bordeMarco.add(lineaNombre);

        // Descripción y Tema
        JLabel lblTexto = new JLabel("Por haber completado satisfactoriamente la evaluación sobre:", SwingConstants.CENTER);
        lblTexto.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblTexto.setBounds(100, 175, 600, 20);
        bordeMarco.add(lblTexto);

        JLabel lblTema = new JLabel(tema, SwingConstants.CENTER);
        lblTema.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTema.setForeground(new Color(20, 40, 80));
        lblTema.setBounds(50, 200, 700, 25);
        bordeMarco.add(lblTema);

        // Nota
        JLabel lblNota = new JLabel(String.format("Calificación Final: %.2f%%", calificacion), SwingConstants.CENTER);
        lblNota.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblNota.setForeground(new Color(100, 100, 100));
        lblNota.setBounds(250, 230, 300, 20);
        bordeMarco.add(lblNota);

        // --- SECCIÓN DE CURSOS (Ahora con mucho más espacio) ---
        JLabel lblSugerenciasTitulo = new JLabel("Ruta de aprendizaje recomendada:");
        lblSugerenciasTitulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblSugerenciasTitulo.setForeground(new Color(20, 40, 80));
        lblSugerenciasTitulo.setBounds(50, 260, 400, 20);
        bordeMarco.add(lblSugerenciasTitulo);

        JTextArea txtCursos = new JTextArea();
        txtCursos.setFont(new Font("SansSerif", Font.PLAIN, 12));
        txtCursos.setLineWrap(true);
        txtCursos.setWrapStyleWord(true);
        txtCursos.setOpaque(false);
        txtCursos.setEditable(false);
        txtCursos.setFocusable(false); // Sin cursor
        // Aumentamos la altura y posición para que quepan 4 cursos largos
        txtCursos.setBounds(50, 290, 715, 190);

        List<CursoSugerido> sugerencias = GestorSugerencias.obtenerSugerencias(calificacion);
        StringBuilder sb = new StringBuilder();
        for (CursoSugerido curso : sugerencias)
        {
            sb.append("• ").append(curso.getDetalle()).append("\n\n");
        }
        txtCursos.setText(sb.toString());
        bordeMarco.add(txtCursos);

        // --- CÓDIGO DE VERIFICACIÓN ---
        JSeparator lineaCodigo = new JSeparator();
        lineaCodigo.setForeground(new Color(200, 200, 200));
        lineaCodigo.setBounds(50, 475, 715, 5);
        bordeMarco.add(lineaCodigo);

        JLabel lblCodigo = new JLabel("Código de verificación: " + codigoVerificacion, SwingConstants.LEFT);
        lblCodigo.setFont(new Font("Monospaced", Font.PLAIN, 10));
        lblCodigo.setForeground(new Color(130, 130, 130));
        lblCodigo.setBounds(50, 480, 450, 15);
        bordeMarco.add(lblCodigo);

        // --- ZONA DE FIRMA (Actualizada) ---
        // 1. Componente de Firma Falsa (Dibujo)
        PanelFirma firmaDibujada = new PanelFirma();
        firmaDibujada.setBounds(530, 490, 200, 50); // Posición encima de la línea
        bordeMarco.add(firmaDibujada);

        // 2. Línea de firma
        JSeparator lineaFirma = new JSeparator();
        lineaFirma.setForeground(Color.BLACK);
        lineaFirma.setBounds(530, 540, 200, 10);
        bordeMarco.add(lineaFirma);

        // 3. Nombre del Director
        JLabel lblNombreDirector = new JLabel("Rodrigo Peña Vega", SwingConstants.CENTER);
        lblNombreDirector.setFont(new Font("Serif", Font.BOLD, 14));
        lblNombreDirector.setBounds(530, 545, 200, 20);
        bordeMarco.add(lblNombreDirector);

        // 4. Cargo
        JLabel lblCargo = new JLabel("Director General", SwingConstants.CENTER);
        lblCargo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblCargo.setBounds(530, 565, 200, 15);
        bordeMarco.add(lblCargo);

        add(panelCertificado, BorderLayout.CENTER);

        // --- 2. BOTONES ---
        JPanel panelBotones = new JPanel();
        panelBotones.setBackground(new Color(45, 45, 45));
        panelBotones.setPreferredSize(new Dimension(850, 50));

        JButton btnImprimir = new JButton("Guardar como PDF / Imprimir");
        btnImprimir.setBackground(new Color(218, 165, 32));
        btnImprimir.setForeground(Color.BLACK);
        btnImprimir.setFocusPainted(false);

        JButton btnSalir = new JButton("Salir");
        btnSalir.setBackground(new Color(200, 50, 50));
        btnSalir.setForeground(Color.WHITE);
        btnSalir.setFocusPainted(false);

        JButton btnVerificar = new JButton("Verificar Certificado");
        btnVerificar.setBackground(new Color(30, 100, 30));
        btnVerificar.setForeground(Color.WHITE);
        btnVerificar.setFocusPainted(false);

        panelBotones.add(btnImprimir);
        panelBotones.add(btnVerificar);
        panelBotones.add(btnSalir);

        add(panelBotones, BorderLayout.SOUTH);

        btnSalir.addActionListener(e -> System.exit(0));
        btnImprimir.addActionListener(e -> imprimirCertificado());
        btnVerificar.addActionListener(e -> new vistas.VistaVerificador().setVisible(true));
    }

    private void imprimirCertificado()
    {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Certificado_" + this.getTitle());

        job.setPrintable((Graphics graphics, PageFormat pageFormat, int pageIndex) ->
        {
            if (pageIndex > 0)
            {
                return Printable.NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            // Ajustar escala para A4
            double scaleX = pageFormat.getImageableWidth() / panelCertificado.getWidth();
            double scaleY = pageFormat.getImageableHeight() / panelCertificado.getHeight();
            double scale = Math.min(scaleX, scaleY);
            g2d.scale(scale, scale);

            panelCertificado.paint(g2d);

            return Printable.PAGE_EXISTS;
        });

        if (job.printDialog())
        {
            try
            {
                job.print();
            } catch (PrinterException e)
            {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    // --- CLASE INTERNA PARA DIBUJAR LA FIRMA FALSA ---
    private static class PanelFirma extends JPanel
    {

        public PanelFirma()
        {
            setOpaque(false); // Fondo transparente
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            // Configuración de "pluma"
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(2.5f)); // Grosor de la pluma
            g2.setColor(new Color(0, 50, 150)); // Color tinta azul oscuro

            // Dibujar un garabato elegante simulando "R. Peña"
            Path2D.Float firma = new Path2D.Float();

            // Simulación de la "R"
            firma.moveTo(40, 30);
            firma.curveTo(40, 0, 70, 0, 50, 25);
            firma.curveTo(40, 40, 40, 40, 60, 45);

            // Simulación del resto "Peña" (ondas)
            firma.curveTo(70, 35, 80, 25, 90, 35);
            firma.curveTo(100, 45, 110, 25, 120, 35);
            firma.curveTo(130, 45, 140, 25, 160, 40);

            // Línea final rápida
            firma.curveTo(170, 30, 180, 50, 190, 20);

            g2.draw(firma);
        }
    }
}
