package logica;

import datos.DatabaseManager;
import java.util.List; // Importante para las listas
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import modelo.Usuario;
import vistas.VistaCuestionario;

/**
 * Hilo modificado para controlar el flujo de login/registro, seleccionar el
 * tema y abrir la ventana del cuestionario.
 */
public class HiloInicioRegistro implements Runnable
{

    private final String nombreUsuario;
    private final String password;
    private final String accion;
    private final DatabaseManager dbManager;
    private final JFrame vistaLogin; // Referencia a la ventana de login para poder cerrarla

    /**
     * Constructor modificado que recibe la ventana de login.
     *
     * @param nombreUsuario El nombre de usuario.
     * @param password La contraseña.
     * @param accion "login" o "register".
     * @param vistaLogin La instancia del JFrame de login.
     */
    public HiloInicioRegistro(String nombreUsuario, String password, String accion, JFrame vistaLogin)
    {
        this.nombreUsuario = nombreUsuario;
        this.password = password;
        this.accion = accion;
        this.dbManager = new DatabaseManager();
        this.vistaLogin = vistaLogin; // Guardamos la referencia
    }

    @Override
    public void run()
    {
        if ("login".equalsIgnoreCase(accion))
        {
            boolean esValido = dbManager.validarUsuario(nombreUsuario, password);

            if (esValido)
            {
                // Obtenemos el objeto Usuario completo desde la BD
                Usuario usuario = dbManager.getUsuarioPorNombre(nombreUsuario);

                if (usuario != null)
                {
                    // Limpiamos cualquier respuesta de un intento anterior ANTES de empezar.
                    dbManager.limpiarRespuestasDeUsuario(usuario.getId());

                    // --- CAMBIO: Selector de Tema ---
                    // 1. Obtenemos los temas disponibles de la base de datos
                    List<String> temas = dbManager.obtenerTemas();
                    String temaSeleccionado = "Sistemas Distribuidos"; // Valor por defecto

                    // 2. Si hay temas, mostramos el selector
                    if (!temas.isEmpty())
                    {
                        Object seleccion = JOptionPane.showInputDialog(
                                vistaLogin,
                                "Selecciona el tema del cuestionario:",
                                "Elegir Tema",
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                temas.toArray(),
                                temas.get(0)
                        );

                        // Si el usuario selecciona algo, actualizamos la variable
                        if (seleccion != null)
                        {
                            temaSeleccionado = (String) seleccion;
                        } else
                        {
                            // Si el usuario da a Cancelar o cierra la ventana, no iniciamos sesión
                            return;
                        }
                    }

                    // Variable final para usar dentro del invokeLater
                    final String temaFinal = temaSeleccionado;

                    // Usamos invokeLater para asegurar que la UI se actualice en el hilo correcto
                    SwingUtilities.invokeLater(() ->
                    {
                        // Cerramos la ventana de login
                        vistaLogin.dispose();
                        // Creamos y mostramos la ventana del cuestionario pasando el TEMA seleccionado
                        // Nota: Recuerda que debes haber actualizado el constructor de VistaCuestionario
                        new VistaCuestionario(usuario, temaFinal).setVisible(true);
                    });
                } else
                {
                    JOptionPane.showMessageDialog(vistaLogin, "No se pudo recuperar la información del usuario.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } else
            {
                JOptionPane.showMessageDialog(vistaLogin, "Usuario o contraseña incorrectos.", "Error de inicio de sesión", JOptionPane.ERROR_MESSAGE);
            }

        } else if ("register".equalsIgnoreCase(accion))
        {
            boolean fueRegistrado = dbManager.registrarUsuario(nombreUsuario, password);

            if (fueRegistrado)
            {
                JOptionPane.showMessageDialog(vistaLogin, "Usuario registrado con éxito. Ahora puedes iniciar sesión.");
            } else
            {
                JOptionPane.showMessageDialog(vistaLogin, "El registro falló. El usuario ya podría existir.", "Error de Registro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
