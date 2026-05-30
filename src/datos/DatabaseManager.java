package datos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import modelo.Pregunta;
import modelo.Opcion;
import java.util.ArrayList;
import java.util.List;
import modelo.Usuario;
import crypto.CryptoManager;

public class DatabaseManager
{

    private static final String URL = "jdbc:sqlite:quiz.db";

    private Connection connect()
    {
        Connection conn = null;
        try
        {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e)
        {
            System.out.println("Error de conexión con la base de datos: " + e.getMessage());
        }
        return conn;
    }

    /**
     * Valida si un usuario y contraseña existen en la base de datos.
     *
     * @param username Nombre de usuario.
     * @param password Contraseña.
     * @return true si el usuario es válido, false en caso contrario.
     */
    public boolean validarUsuario(String username, String password)
    {
        String sql = "SELECT password FROM usuarios WHERE nombre_usuario = ?";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
            {
                String hashAlmacenado = rs.getString("password");
                return CryptoManager.verificarPassword(password, hashAlmacenado);
            }
            return false;
        } catch (SQLException e)
        {
            System.out.println("Error al validar usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Registra un nuevo usuario en la base de datos.
     *
     * @param username Nombre de usuario.
     * @param password Contraseña.
     * @return true si el registro fue exitoso, false si falló.
     */
    public boolean registrarUsuario(String username, String password)
    {
        String sql = "INSERT INTO usuarios(nombre_usuario, password) VALUES(?,?)";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            String passwordSegura = CryptoManager.hashPassword(password);
            pstmt.setString(1, username);
            pstmt.setString(2, passwordSegura);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e)
        {
            System.out.println("Error al registrar usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene una lista de todas las preguntas de un tema específico,
     * incluyendo sus respectivas opciones.
     *
     * @param tema El tema del cuestionario, ej. "Sistemas Distribuidos".
     * @return Una lista de objetos Pregunta.
     */
    public List<Pregunta> getPreguntasPorTema(String tema)
    {
        String sqlPreguntas = "SELECT * FROM preguntas WHERE tema = ?";
        String sqlOpciones = "SELECT * FROM opciones WHERE pregunta_id = ?";
        List<Pregunta> preguntas = new ArrayList<>();

        try (Connection conn = this.connect(); PreparedStatement pstmtPreguntas = conn.prepareStatement(sqlPreguntas))
        {

            pstmtPreguntas.setString(1, tema);
            ResultSet rsPreguntas = pstmtPreguntas.executeQuery();

            while (rsPreguntas.next())
            {
                int preguntaId = rsPreguntas.getInt("id");
                String textoPregunta = rsPreguntas.getString("texto_pregunta");

                // Ahora, por cada pregunta, obtenemos sus opciones
                List<Opcion> opciones = new ArrayList<>();
                try (PreparedStatement pstmtOpciones = conn.prepareStatement(sqlOpciones))
                {
                    pstmtOpciones.setInt(1, preguntaId);
                    ResultSet rsOpciones = pstmtOpciones.executeQuery();
                    while (rsOpciones.next())
                    {
                        opciones.add(new Opcion(
                                rsOpciones.getInt("id"),
                                rsOpciones.getString("texto_opcion"),
                                rsOpciones.getBoolean("es_correcta")
                        ));
                    }
                }

                preguntas.add(new Pregunta(preguntaId, textoPregunta, opciones));
            }
        } catch (SQLException e)
        {
            System.out.println("Error al obtener preguntas: " + e.getMessage());
        }
        return preguntas;
    }

    /**
     * Guarda la respuesta seleccionada por un usuario para una pregunta.
     *
     * @param usuarioId El ID del usuario que responde.
     * @param preguntaId El ID de la pregunta respondida.
     * @param opcionId El ID de la opción seleccionada.
     */
    public void guardarRespuesta(int usuarioId, int preguntaId, int opcionId)
    {
        String sql = "INSERT INTO respuestas_usuario(usuario_id, pregunta_id, opcion_seleccionada_id) VALUES(?,?,?)";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            pstmt.setInt(1, usuarioId);
            pstmt.setInt(2, preguntaId);
            pstmt.setInt(3, opcionId);
            pstmt.executeUpdate();
        } catch (SQLException e)
        {
            System.out.println("Error al guardar respuesta: " + e.getMessage());
        }
    }

    public Usuario getUsuarioPorNombre(String username)
    {
        String sql = "SELECT * FROM usuarios WHERE nombre_usuario = ?";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
            {
                return new Usuario(rs.getInt("id"), rs.getString("nombre_usuario"), rs.getString("password"));
            }
        } catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * Cuenta el número de respuestas correctas para un usuario específico.
     * Utiliza una consulta SQL con JOIN para ser más eficiente.
     *
     * @param usuarioId El ID del usuario.
     * @return El número total de respuestas correctas.
     */
    public int evaluarRespuestas(int usuarioId)
    {
        // La consulta une las respuestas del usuario con la tabla de opciones
        // y cuenta cuántas de las opciones seleccionadas tienen 'es_correcta' = 1 (true).
        String sql = "SELECT COUNT(*) FROM respuestas_usuario ru "
                + "JOIN opciones o ON ru.opcion_seleccionada_id = o.id "
                + "WHERE ru.usuario_id = ? AND o.es_correcta = 1";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql))
        {

            pstmt.setInt(1, usuarioId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next())
            {
                return rs.getInt(1); // Devuelve el primer (y único) valor del COUNT
            }

        } catch (SQLException e)
        {
            System.out.println("Error al evaluar respuestas: " + e.getMessage());
        }
        return 0; // Devuelve 0 si hay un error o no hay correctas
    }

    /**
     * Borra todas las respuestas anteriores de un usuario específico.
     *
     * @param usuarioId El ID del usuario cuyas respuestas se limpiarán.
     */
    public void limpiarRespuestasDeUsuario(int usuarioId)
    {
        String sql = "DELETE FROM respuestas_usuario WHERE usuario_id = ?";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            pstmt.setInt(1, usuarioId);
            pstmt.executeUpdate();
            System.out.println("Respuestas anteriores del usuario " + usuarioId + " eliminadas.");
        } catch (SQLException e)
        {
            System.out.println("Error al limpiar respuestas: " + e.getMessage());
        }
    }

    public java.util.List<String> obtenerTemas()
    {
        java.util.List<String> temas = new java.util.ArrayList<>();
        String sql = "SELECT DISTINCT tema FROM preguntas";
        try (java.sql.Connection conn = this.connect(); java.sql.PreparedStatement pstmt = conn.prepareStatement(sql); java.sql.ResultSet rs = pstmt.executeQuery())
        {
            while (rs.next())
            {
                temas.add(rs.getString("tema"));
            }
        } catch (java.sql.SQLException e)
        {
            System.out.println("Error al obtener temas: " + e.getMessage());
        }
        return temas;
    }

    public boolean guardarCertificado(int usuarioId, String tema, double calificacion,
                                      String hashCert, String firmaRsa)
    {
        String sql = "INSERT INTO certificados(usuario_id, tema, calificacion, hash_cert, firma_rsa, fecha_emision) VALUES(?,?,?,?,?,?)";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            pstmt.setInt(1, usuarioId);
            pstmt.setString(2, tema);
            pstmt.setDouble(3, calificacion);
            pstmt.setString(4, hashCert);
            pstmt.setString(5, firmaRsa);
            pstmt.setString(6, new java.util.Date().toString());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e)
        {
            System.out.println("Error al guardar certificado: " + e.getMessage());
            return false;
        }
    }

    public String[] buscarCertificadoPorHash(String hashCert)
    {
        String sql = "SELECT u.nombre_usuario, c.tema, c.calificacion, c.firma_rsa, c.fecha_emision "
                + "FROM certificados c JOIN usuarios u ON c.usuario_id = u.id "
                + "WHERE SUBSTR(c.hash_cert, 1, 16) = LOWER(?)";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql))
        {
            pstmt.setString(1, hashCert);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
            {
                return new String[]
                {
                    rs.getString("nombre_usuario"),
                    rs.getString("tema"),
                    String.format("%.2f", rs.getDouble("calificacion")),
                    rs.getString("firma_rsa"),
                    rs.getString("fecha_emision")
                };
            }
        } catch (SQLException e)
        {
            System.out.println("Error al buscar certificado: " + e.getMessage());
        }
        return null;
    }

}
