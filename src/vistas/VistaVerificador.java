package vistas;

import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileInputStream;
import java.security.cert.X509Certificate;
import crypto.CryptoManager;
import java.security.KeyPair;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

public class VistaVerificador extends JFrame
{
    public VistaVerificador()
    {
        setTitle("Verificador de Certificados");
        setSize(480, 320);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(new Color(45, 45, 45));
        setLayout(null);

        JLabel instruccion = new JLabel("Selecciona el archivo PDF del certificado:");
        instruccion.setForeground(Color.WHITE);
        instruccion.setFont(new Font("SansSerif", Font.PLAIN, 13));
        instruccion.setBounds(20, 20, 440, 20);
        add(instruccion);

        JTextField txtRuta = new JTextField();
        txtRuta.setBackground(new Color(60, 60, 60));
        txtRuta.setForeground(Color.WHITE);
        txtRuta.setCaretColor(Color.WHITE);
        txtRuta.setEditable(false);
        txtRuta.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));
        txtRuta.setBounds(20, 50, 310, 30);
        add(txtRuta);

        JButton btnSeleccionar = new JButton("Examinar...");
        btnSeleccionar.setBackground(new Color(60, 60, 60));
        btnSeleccionar.setForeground(Color.WHITE);
        btnSeleccionar.setFocusPainted(false);
        btnSeleccionar.setBounds(340, 50, 120, 30);
        add(btnSeleccionar);

        JButton btnVerificar = new JButton("Verificar Certificado");
        btnVerificar.setBackground(new Color(218, 165, 32));
        btnVerificar.setForeground(Color.BLACK);
        btnVerificar.setFocusPainted(false);
        btnVerificar.setBounds(20, 100, 200, 35);
        add(btnVerificar);

        JLabel lblResultado = new JLabel("", SwingConstants.CENTER);
        lblResultado.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblResultado.setOpaque(true);
        lblResultado.setBackground(new Color(60, 60, 60));
        lblResultado.setForeground(Color.WHITE);
        lblResultado.setBounds(20, 155, 440, 120);
        add(lblResultado);

        // Listener botón Examinar
        btnSeleccionar.addActionListener(e ->
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Seleccionar certificado PDF");
            chooser.setFileFilter(new FileNameExtensionFilter("Archivos PDF", "pdf"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                txtRuta.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        // Listener botón Verificar
        btnVerificar.addActionListener(e ->
        {
            String ruta = txtRuta.getText().trim();
            if (ruta.isEmpty())
            {
                JOptionPane.showMessageDialog(this,
                    "Primero selecciona un archivo PDF.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try
            {
                // 1. Abrir el PDF y leer metadatos
                PDDocument documento = PDDocument.load(new File(ruta));
                PDDocumentInformation info = documento.getDocumentInformation();

                String contenidoFirmado = info.getKeywords();
                String firmaRsa = info.getCustomMetadataValue("firma_rsa");
                documento.close();

                // 2. Validar que el PDF tiene los metadatos de seguridad
                if (contenidoFirmado == null || firmaRsa == null ||
                    contenidoFirmado.isEmpty() || firmaRsa.isEmpty())
                {
                    lblResultado.setBackground(new Color(100, 30, 30));
                    lblResultado.setText("<html><center>&#10060; Este PDF no es un certificado del sistema<br>" +
                        "o fue generado con una version anterior</center></html>");
                    return;
                }

                // CAPA 1: verificar firma RSA en metadatos
                KeyPair parClaves = CryptoManager.cargarClaves(".");
                boolean esValido = CryptoManager.verificarFirma(
                    contenidoFirmado, firmaRsa, parClaves.getPublic());

                // CAPA 2: verificar que el PDF tiene firma digital nativa de nuestro sistema
                boolean firmaPDFValida = false;
                try
                {
                    PDDocument docVerif = PDDocument.load(new File(ruta));
                    java.util.List<org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature>
                        firmasPDF = docVerif.getSignatureDictionaries();
                    docVerif.close();

                    if (firmasPDF.isEmpty())
                    {
                        lblResultado.setBackground(new Color(100, 30, 30));
                        lblResultado.setText("<html><center>&#10060; El PDF no tiene firma digital<br>" +
                            "o fue generado con una version anterior</center></html>");
                        return;
                    }

                    for (org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature sig
                         : firmasPDF)
                    {
                        String razon = sig.getReason();
                        if (razon != null && razon.contains("Certificado de finalizacion"))
                        {
                            firmaPDFValida = true;
                            break;
                        }
                    }
                }
                catch (Exception ex2)
                {
                    System.out.println("Error verificando firma PDF: " + ex2.getMessage());
                    firmaPDFValida = false;
                }

                if (esValido && firmaPDFValida)
                {
                    String[] partes = contenidoFirmado.split("\\|");
                    String nombre = partes.length > 0 ? partes[0] : "?";
                    String tema   = partes.length > 1 ? partes[1] : "?";
                    String cal    = partes.length > 2 ? partes[2] : "?";

                    lblResultado.setBackground(new Color(20, 80, 20));
                    lblResultado.setText("<html><center>&#10003; Certificado autentico<br>" +
                        "Firma RSA: valida &#10003; | Firma PDF: valida &#10003;<br>" +
                        nombre + " &mdash; " + tema + " &mdash; " + cal + "%</center></html>");
                }
                else if (esValido && !firmaPDFValida)
                {
                    lblResultado.setBackground(new Color(100, 60, 0));
                    lblResultado.setText("<html><center>&#9888; Metadatos intactos pero PDF modificado<br>" +
                        "Firma RSA: valida &#10003; | Firma PDF: invalida &#10060;<br>" +
                        "El contenido visual del PDF fue alterado</center></html>");
                }
                else
                {
                    lblResultado.setBackground(new Color(100, 30, 30));
                    lblResultado.setText("<html><center>&#10060; Certificado invalido<br>" +
                        "Firma RSA: invalida &#10060; | Los metadatos fueron alterados</center></html>");
                }
            }
            catch (Exception ex)
            {
                System.out.println("Error al verificar PDF: " + ex.getMessage());
                lblResultado.setBackground(new Color(100, 30, 30));
                lblResultado.setText("<html><center>&#10060; Error al leer el PDF<br>" +
                    ex.getMessage() + "</center></html>");
            }
        });
    }
}
