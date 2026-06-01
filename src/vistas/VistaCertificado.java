package vistas;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.List;
import javax.swing.*;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.InputStream;
import modelo.Usuario;
import modelo.CursoSugerido;
import logica.GestorSugerencias;
import crypto.CryptoManager;
import datos.DatabaseManager;
import java.security.KeyPair;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;

public class VistaCertificado extends JFrame
{

    private final JPanel panelCertificado;

    private String nombreUsuarioGuardado;
    private String temaGuardado;
    private double calificacionGuardada;
    private String hashCertGuardado;
    private String firmaRsaGuardada;
    private String contenidoCertGuardado;

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

        this.nombreUsuarioGuardado = usuario.getNombreUsuario();
        this.temaGuardado = tema;
        this.calificacionGuardada = calificacion;
        this.hashCertGuardado = hashCert;
        this.firmaRsaGuardada = firmaRsa;
        this.contenidoCertGuardado = contenidoCert;

        setTitle("Certificado Oficial");
        setSize(850, 720);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- 1. PANEL DEL CERTIFICADO ---
        panelCertificado = new JPanel();
        panelCertificado.setLayout(null);
        panelCertificado.setBackground(Color.WHITE);

        JPanel bordeMarco = new JPanel(null);
        bordeMarco.setBounds(10, 10, 815, 600);
        bordeMarco.setBackground(new Color(255, 255, 255));
        bordeMarco.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(20, 40, 80), 5),
                BorderFactory.createLineBorder(new Color(218, 165, 32), 3)
        ));
        panelCertificado.add(bordeMarco);

        JLabel lblEscudo = new JLabel("★", SwingConstants.CENTER);
        lblEscudo.setFont(new Font("Serif", Font.BOLD, 40));
        lblEscudo.setForeground(new Color(218, 165, 32));
        lblEscudo.setBounds(350, 15, 100, 40);
        bordeMarco.add(lblEscudo);

        JLabel lblTitulo = new JLabel("CERTIFICADO DE FINALIZACIÓN", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Serif", Font.BOLD, 30));
        lblTitulo.setForeground(new Color(20, 40, 80));
        lblTitulo.setBounds(50, 55, 700, 35);
        bordeMarco.add(lblTitulo);

        JLabel lblOtorgado = new JLabel("Se otorga el presente reconocimiento a:", SwingConstants.CENTER);
        lblOtorgado.setFont(new Font("SansSerif", Font.ITALIC, 15));
        lblOtorgado.setBounds(100, 100, 600, 20);
        bordeMarco.add(lblOtorgado);

        JLabel lblNombre = new JLabel(usuario.getNombreUsuario().toUpperCase(), SwingConstants.CENTER);
        lblNombre.setFont(new Font("Serif", Font.BOLD, 26));
        lblNombre.setForeground(new Color(0, 0, 0));
        lblNombre.setBounds(50, 125, 700, 35);
        bordeMarco.add(lblNombre);

        JSeparator lineaNombre = new JSeparator();
        lineaNombre.setForeground(Color.BLACK);
        lineaNombre.setBounds(200, 160, 400, 10);
        bordeMarco.add(lineaNombre);

        JLabel lblTexto = new JLabel("Por haber completado satisfactoriamente la evaluación sobre:", SwingConstants.CENTER);
        lblTexto.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblTexto.setBounds(100, 175, 600, 20);
        bordeMarco.add(lblTexto);

        JLabel lblTema = new JLabel(tema, SwingConstants.CENTER);
        lblTema.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTema.setForeground(new Color(20, 40, 80));
        lblTema.setBounds(50, 200, 700, 25);
        bordeMarco.add(lblTema);

        JLabel lblNota = new JLabel(String.format("Calificación Final: %.2f%%", calificacion), SwingConstants.CENTER);
        lblNota.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblNota.setForeground(new Color(100, 100, 100));
        lblNota.setBounds(250, 230, 300, 20);
        bordeMarco.add(lblNota);

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
        txtCursos.setFocusable(false);
        txtCursos.setBounds(50, 290, 715, 190);

        List<CursoSugerido> sugerencias = GestorSugerencias.obtenerSugerencias(calificacion);
        StringBuilder sb = new StringBuilder();
        for (CursoSugerido curso : sugerencias)
        {
            sb.append("• ").append(curso.getDetalle()).append("\n\n");
        }
        txtCursos.setText(sb.toString());
        bordeMarco.add(txtCursos);

        JSeparator lineaCodigo = new JSeparator();
        lineaCodigo.setForeground(new Color(200, 200, 200));
        lineaCodigo.setBounds(50, 475, 715, 5);
        bordeMarco.add(lineaCodigo);

        JLabel lblCodigo = new JLabel("Código de verificación: " + codigoVerificacion, SwingConstants.LEFT);
        lblCodigo.setFont(new Font("Monospaced", Font.PLAIN, 10));
        lblCodigo.setForeground(new Color(130, 130, 130));
        lblCodigo.setBounds(50, 480, 450, 15);
        bordeMarco.add(lblCodigo);

        PanelFirma firmaDibujada = new PanelFirma();
        firmaDibujada.setBounds(530, 490, 200, 50);
        bordeMarco.add(firmaDibujada);

        JSeparator lineaFirma = new JSeparator();
        lineaFirma.setForeground(Color.BLACK);
        lineaFirma.setBounds(530, 540, 200, 10);
        bordeMarco.add(lineaFirma);

        JLabel lblNombreDirector = new JLabel("Rodrigo Peña Vega", SwingConstants.CENTER);
        lblNombreDirector.setFont(new Font("Serif", Font.BOLD, 14));
        lblNombreDirector.setBounds(530, 545, 200, 20);
        bordeMarco.add(lblNombreDirector);

        JLabel lblCargo = new JLabel("Director General", SwingConstants.CENTER);
        lblCargo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblCargo.setBounds(530, 565, 200, 15);
        bordeMarco.add(lblCargo);

        add(panelCertificado, BorderLayout.CENTER);

        // --- 2. BOTONES ---
        JPanel panelBotones = new JPanel();
        panelBotones.setBackground(new Color(45, 45, 45));
        panelBotones.setPreferredSize(new Dimension(850, 50));

        JButton btnImprimir = new JButton("Guardar como PDF");
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
        btnImprimir.addActionListener(e -> generarPDF());
        btnVerificar.addActionListener(e -> new vistas.VistaVerificador().setVisible(true));
    }

    private void generarPDF()
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar certificado como PDF");
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos PDF", "pdf"));
        chooser.setSelectedFile(new File("certificado.pdf"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
        {
            return;
        }

        File archivoPDF = chooser.getSelectedFile();
        if (!archivoPDF.getName().endsWith(".pdf"))
        {
            archivoPDF = new File(archivoPDF.getAbsolutePath() + ".pdf");
        }

        try
        {
            PDDocument documento = new PDDocument();
            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);

            PDDocumentInformation info = documento.getDocumentInformation();
            info.setTitle("Certificado de Finalizacion");
            info.setAuthor(nombreUsuarioGuardado);
            info.setSubject(temaGuardado);
            info.setKeywords(contenidoCertGuardado);
            info.setCustomMetadataValue("firma_rsa", firmaRsaGuardada);
            info.setCustomMetadataValue("hash_cert", hashCertGuardado);

            PDPageContentStream contenido = new PDPageContentStream(documento, pagina);
            float ancho = pagina.getMediaBox().getWidth();
            float alto  = pagina.getMediaBox().getHeight();

            // --- BORDES DECORATIVOS ---
            contenido.setStrokingColor(new Color(20, 40, 80));
            contenido.setLineWidth(5);
            contenido.addRect(10, 10, ancho - 20, alto - 20);
            contenido.stroke();

            contenido.setStrokingColor(new Color(218, 165, 32));
            contenido.setLineWidth(3);
            contenido.addRect(18, 18, ancho - 36, alto - 36);
            contenido.stroke();

            // --- TÍTULO ---
            contenido.setNonStrokingColor(new Color(20, 40, 80));
            contenido.beginText();
            contenido.setFont(PDType1Font.HELVETICA_BOLD, 24);
            contenido.newLineAtOffset((ancho - 400) / 2, 780);
            contenido.showText("CERTIFICADO DE FINALIZACION");
            contenido.endText();

            // --- SUBTÍTULO ---
            contenido.setNonStrokingColor(new Color(0, 0, 0));
            contenido.beginText();
            contenido.setFont(PDType1Font.HELVETICA, 14);
            contenido.newLineAtOffset((ancho - 280) / 2, 750);
            contenido.showText("Se otorga el presente reconocimiento a:");
            contenido.endText();

            // --- NOMBRE ---
            String nombre = nombreUsuarioGuardado.toUpperCase();
            contenido.setNonStrokingColor(new Color(0, 0, 0));
            contenido.beginText();
            contenido.setFont(PDType1Font.HELVETICA_BOLD, 20);
            contenido.newLineAtOffset((ancho - (nombre.length() * 11)) / 2, 710);
            contenido.showText(nombre);
            contenido.endText();

            // Línea decorativa bajo el nombre
            contenido.setStrokingColor(new Color(0, 0, 0));
            contenido.setLineWidth(1);
            contenido.moveTo((ancho - 300) / 2, 703);
            contenido.lineTo((ancho + 300) / 2, 703);
            contenido.stroke();

            // --- TEXTO TEMA ---
            contenido.setNonStrokingColor(new Color(0, 0, 0));
            contenido.beginText();
            contenido.setFont(PDType1Font.HELVETICA, 13);
            contenido.newLineAtOffset((ancho - 380) / 2, 670);
            contenido.showText("Por haber completado satisfactoriamente la evaluacion sobre:");
            contenido.endText();

            // --- TEMA ---
            contenido.setNonStrokingColor(new Color(20, 40, 80));
            contenido.beginText();
            contenido.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contenido.newLineAtOffset((ancho - (temaGuardado.length() * 9)) / 2, 640);
            contenido.showText(temaGuardado);
            contenido.endText();

            // --- CALIFICACIÓN ---
            String calStr = String.format("Calificacion Final: %.2f%%", calificacionGuardada);
            contenido.setNonStrokingColor(new Color(100, 100, 100));
            contenido.beginText();
            contenido.setFont(PDType1Font.HELVETICA, 12);
            contenido.newLineAtOffset((ancho - 180) / 2, 605);
            contenido.showText(calStr);
            contenido.endText();

            // --- TÍTULO SECCIÓN CURSOS ---
            contenido.setNonStrokingColor(new Color(20, 40, 80));
            contenido.beginText();
            contenido.setFont(PDType1Font.HELVETICA_BOLD, 13);
            contenido.newLineAtOffset(50, 575);
            contenido.showText("Ruta de aprendizaje recomendada:");
            contenido.endText();

            // --- CURSOS CON TEXTO ENVUELTO ---
            contenido.setNonStrokingColor(new Color(0, 0, 0));
            List<CursoSugerido> sugerenciasPDF = GestorSugerencias.obtenerSugerencias(calificacionGuardada);
            float yCursos = 558;
            for (CursoSugerido curso : sugerenciasPDF)
            {
                yCursos = dibujarTextoEnvuelto(contenido, PDType1Font.HELVETICA, 11,
                    "- " + curso.getDetalle(), 50, yCursos, ancho - 100);
                yCursos -= 6;
            }

            // --- LÍNEA SEPARADORA INFERIOR ---
            contenido.setStrokingColor(new Color(200, 200, 200));
            contenido.setLineWidth(1);
            contenido.moveTo(50, 155);
            contenido.lineTo(ancho - 50, 155);
            contenido.stroke();

            // --- CÓDIGO DE VERIFICACIÓN ---
            contenido.setNonStrokingColor(new Color(130, 130, 130));
            contenido.beginText();
            contenido.setFont(PDType1Font.COURIER, 9);
            contenido.newLineAtOffset(50, 140);
            contenido.showText("Codigo de verificacion: " + hashCertGuardado.substring(0, 16).toUpperCase());
            contenido.endText();

            // --- TRAZO DE FIRMA ESTILIZADO ---
            contenido.setStrokingColor(new Color(0, 50, 150));
            contenido.setLineWidth(1.5f);
            float fx = ancho - 215;
            contenido.moveTo(fx, 150);
            contenido.curveTo(fx,       167, fx + 20, 167, fx + 10, 153);
            contenido.curveTo(fx,       143, fx,      143, fx + 15, 141);
            contenido.curveTo(fx + 25,  135, fx + 35, 147, fx + 45, 137);
            contenido.curveTo(fx + 55,  127, fx + 65, 147, fx + 75, 137);
            contenido.curveTo(fx + 85,  127, fx + 95, 143, fx + 115, 140);
            contenido.curveTo(fx + 125, 135, fx + 135, 153, fx + 150, 133);
            contenido.stroke();

            // --- LÍNEA DE FIRMA ---
            contenido.setStrokingColor(new Color(0, 0, 0));
            contenido.setLineWidth(1);
            contenido.moveTo(ancho - 230, 133);
            contenido.lineTo(ancho - 30, 133);
            contenido.stroke();

            // --- NOMBRE Y CARGO DEL DIRECTOR ---
            contenido.setNonStrokingColor(new Color(0, 0, 0));
            contenido.beginText();
            contenido.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contenido.newLineAtOffset(ancho - 220, 118);
            contenido.showText("Rodrigo Pena Vega");
            contenido.endText();

            contenido.beginText();
            contenido.setFont(PDType1Font.HELVETICA, 10);
            contenido.newLineAtOffset(ancho - 207, 103);
            contenido.showText("Director General");
            contenido.endText();

            contenido.close();

            // 5. Incrustar firma digital RSA como campo de firma nativa del PDF
            File archivoTemporal = new File(archivoPDF.getAbsolutePath() + ".tmp");
            documento.save(archivoTemporal);
            documento.close();

            PDDocument docParaFirmar = PDDocument.load(archivoTemporal);

            PDSignature firma = new PDSignature();
            firma.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            firma.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            firma.setName(nombreUsuarioGuardado);
            firma.setReason("Certificado de finalizacion emitido por el sistema");
            firma.setSignDate(java.util.Calendar.getInstance());

            final String firmaRsaFinal = firmaRsaGuardada;

            SignatureInterface firmante = (InputStream contenidoStream) ->
            {
                try
                {
                    return java.util.Base64.getDecoder().decode(firmaRsaFinal);
                }
                catch (Exception e)
                {
                    System.out.println("Error en firma PDF nativa: " + e.getMessage());
                    return new byte[0];
                }
            };

            SignatureOptions opciones = new SignatureOptions();
            opciones.setPreferredSignatureSize(SignatureOptions.DEFAULT_SIGNATURE_SIZE * 2);
            docParaFirmar.addSignature(firma, firmante, opciones);
            docParaFirmar.saveIncremental(new java.io.FileOutputStream(archivoPDF));
            docParaFirmar.close();
            archivoTemporal.delete();

            JOptionPane.showMessageDialog(this,
                "PDF guardado y firmado digitalmente en:\n" + archivoPDF.getAbsolutePath(),
                "PDF Generado", JOptionPane.INFORMATION_MESSAGE);
        }
        catch (Exception e)
        {
            System.out.println("Error al generar PDF: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                "Error al generar el PDF: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private float dibujarTextoEnvuelto(PDPageContentStream contenido, PDType1Font fuente,
        int tamanio, String texto, float x, float y, float anchoMax) throws Exception
    {
        String[] palabras = texto.split(" ");
        StringBuilder lineaActual = new StringBuilder();
        float altoLinea = tamanio * 1.4f;
        float yActual = y;

        for (String palabra : palabras)
        {
            String prueba = lineaActual.length() == 0 ? palabra : lineaActual + " " + palabra;
            float anchoTexto;
            try
            {
                anchoTexto = fuente.getStringWidth(prueba) / 1000 * tamanio;
            }
            catch (Exception ex)
            {
                anchoTexto = prueba.length() * tamanio * 0.5f;
            }

            if (anchoTexto > anchoMax && lineaActual.length() > 0)
            {
                contenido.beginText();
                contenido.setFont(fuente, tamanio);
                contenido.newLineAtOffset(x, yActual);
                contenido.showText(lineaActual.toString());
                contenido.endText();
                yActual -= altoLinea;
                lineaActual = new StringBuilder(palabra);
            }
            else
            {
                lineaActual = new StringBuilder(prueba);
            }
        }

        if (lineaActual.length() > 0)
        {
            contenido.beginText();
            contenido.setFont(fuente, tamanio);
            contenido.newLineAtOffset(x, yActual);
            contenido.showText(lineaActual.toString());
            contenido.endText();
            yActual -= altoLinea;
        }

        return yActual;
    }

    // --- CLASE INTERNA PARA DIBUJAR LA FIRMA FALSA ---
    private static class PanelFirma extends JPanel
    {

        public PanelFirma()
        {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(2.5f));
            g2.setColor(new Color(0, 50, 150));

            Path2D.Float firma = new Path2D.Float();

            firma.moveTo(40, 30);
            firma.curveTo(40, 0, 70, 0, 50, 25);
            firma.curveTo(40, 40, 40, 40, 60, 45);

            firma.curveTo(70, 35, 80, 25, 90, 35);
            firma.curveTo(100, 45, 110, 25, 120, 35);
            firma.curveTo(130, 45, 140, 25, 160, 40);

            firma.curveTo(170, 30, 180, 50, 190, 20);

            g2.draw(firma);
        }
    }
}
