package vistas;

import java.awt.*;
import javax.swing.*;
import crypto.CryptoManager;
import datos.DatabaseManager;
import java.security.KeyPair;

public class VistaVerificador extends JFrame
{

    public VistaVerificador()
    {
        setTitle("Verificador de Certificados");
        setSize(420, 280);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(new Color(45, 45, 45));
        setLayout(null);

        JLabel instruccion = new JLabel("Ingresa el codigo de verificacion:");
        instruccion.setForeground(Color.WHITE);
        instruccion.setFont(new Font("SansSerif", Font.PLAIN, 13));
        instruccion.setBounds(20, 20, 380, 20);
        add(instruccion);

        JTextField txtCodigo = new JTextField();
        txtCodigo.setBackground(new Color(60, 60, 60));
        txtCodigo.setForeground(Color.WHITE);
        txtCodigo.setCaretColor(Color.WHITE);
        txtCodigo.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));
        txtCodigo.setBounds(20, 50, 370, 30);
        add(txtCodigo);

        JButton btnVerificar = new JButton("Verificar");
        btnVerificar.setBackground(new Color(218, 165, 32));
        btnVerificar.setForeground(Color.BLACK);
        btnVerificar.setFocusPainted(false);
        btnVerificar.setBounds(20, 100, 150, 35);
        add(btnVerificar);

        JLabel lblResultado = new JLabel("", SwingConstants.CENTER);
        lblResultado.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblResultado.setOpaque(true);
        lblResultado.setBackground(new Color(60, 60, 60));
        lblResultado.setForeground(Color.WHITE);
        lblResultado.setBounds(20, 155, 380, 80);
        add(lblResultado);

        btnVerificar.addActionListener(e ->
        {
            String codigoIngresado = txtCodigo.getText().trim().toLowerCase();

            DatabaseManager dbManager = new DatabaseManager();
            String[] datos = dbManager.buscarCertificadoPorHash(codigoIngresado);

            if (datos == null)
            {
                lblResultado.setBackground(new Color(100, 30, 30));
                lblResultado.setText("<html><center>\u274c Certificado no encontrado<br>o codigo incorrecto</center></html>");
            }
            else
            {
                String contenidoCert = datos[0] + "|" + datos[1] + "|" + datos[2] + "|" + datos[4];
                KeyPair parClaves = CryptoManager.cargarClaves(".");
                boolean esValido = CryptoManager.verificarFirma(contenidoCert, datos[3], parClaves.getPublic());

                if (esValido)
                {
                    lblResultado.setBackground(new Color(20, 80, 20));
                    lblResultado.setText("<html><center>\u2705 Certificado autentico<br>" +
                        datos[0] + " - " + datos[1] + "<br>" +
                        datos[2] + "% - " + datos[4] + "</center></html>");
                }
                else
                {
                    lblResultado.setBackground(new Color(100, 30, 30));
                    lblResultado.setText("<html><center>\u274c Firma invalida<br>El certificado fue alterado</center></html>");
                }
            }
        });
    }
}