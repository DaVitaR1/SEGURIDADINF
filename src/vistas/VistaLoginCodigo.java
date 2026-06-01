package vistas;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import logica.HiloInicioRegistro;

public class VistaLoginCodigo extends JFrame
{

    // --- Componentes de la interfaz ---
    private final JLabel lblUsuario;
    private final JLabel lblPassword;
    private final JTextField txtUsuario;
    private final JPasswordField pwdPassword;
    private final JButton btnIniciar;
    private final JButton btnRegistrar;

    public VistaLoginCodigo()
    {
        // --- Paleta de Colores Estética Oscura ---
        Color colorFondo = new Color(45, 45, 45);
        Color colorComponente = new Color(60, 60, 60);
        Color colorTexto = Color.WHITE;
        Color colorBorde = Color.GRAY;

        // --- Configuración básica de la ventana (JFrame) ---
        setTitle("Inicio de Sesión");

        // --- CÓDIGO PARA EL ICONO ---
        ImageIcon icon = new ImageIcon(getClass().getResource("/recursos/logo.png"));
        setIconImage(icon.getImage());

        setSize(320, 200);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(colorFondo);

        // --- Inicialización de componentes ---
        lblUsuario = new JLabel("Usuario:");
        lblPassword = new JLabel("Contraseña:");
        txtUsuario = new JTextField();
        pwdPassword = new JPasswordField();
        btnIniciar = new JButton("Iniciar Sesión");
        btnRegistrar = new JButton("Registrarse");

        // --- Aplicar Estilos ---
        lblUsuario.setForeground(colorTexto);
        lblPassword.setForeground(colorTexto);

        txtUsuario.setBackground(colorComponente);
        txtUsuario.setForeground(colorTexto);
        txtUsuario.setCaretColor(colorTexto);
        txtUsuario.setBorder(BorderFactory.createLineBorder(colorBorde));

        pwdPassword.setBackground(colorComponente);
        pwdPassword.setForeground(colorTexto);
        pwdPassword.setCaretColor(colorTexto);
        pwdPassword.setBorder(BorderFactory.createLineBorder(colorBorde));

        btnIniciar.setBackground(colorComponente);
        btnIniciar.setForeground(colorTexto);
        btnIniciar.setFocusPainted(false);

        btnRegistrar.setBackground(colorComponente);
        btnRegistrar.setForeground(colorTexto);
        btnRegistrar.setFocusPainted(false);

        // --- Posicionamiento ---
        lblUsuario.setBounds(30, 30, 80, 25);
        txtUsuario.setBounds(120, 30, 150, 25);
        lblPassword.setBounds(30, 70, 80, 25);
        pwdPassword.setBounds(120, 70, 150, 25);
        btnIniciar.setBounds(30, 110, 120, 30);
        btnRegistrar.setBounds(160, 110, 110, 30);

        // --- Añadir Componentes ---
        add(lblUsuario);
        add(txtUsuario);
        add(lblPassword);
        add(pwdPassword);
        add(btnIniciar);
        add(btnRegistrar);

        // --- Listeners de los botones ---
        btnIniciar.addActionListener(e -> iniciarProceso("login"));
        btnRegistrar.addActionListener(e -> iniciarProceso("register"));
    }

    private void iniciarProceso(String accion)
    {
        String usuario = txtUsuario.getText();
        String password = new String(pwdPassword.getPassword());

        if (usuario.isEmpty() || password.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "El usuario y la contraseña no pueden estar vacíos.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Se le pasa 'this' como referencia a esta misma ventana
        HiloInicioRegistro tarea = new HiloInicioRegistro(usuario, password, accion, this);
        Thread hilo = new Thread(tarea);
        hilo.start();
    }

    /**
     * Método principal que ahora inicia la aplicación a través del
     * SplashScreen.
     */
    public static void main(String[] args)
    {
        // Inicializar claves RSA del sistema al arrancar
        crypto.CryptoManager.cargarClaves(".");
        SplashScreen splash = new SplashScreen();
        splash.setVisible(true);
        splash.startLoading();
    }
}
