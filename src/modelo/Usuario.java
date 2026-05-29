package modelo;

public class Usuario
{

    private int id;
    private String nombreUsuario;
    private String password;

    // Constructor vacío
    public Usuario()
    {
    }

    // Constructor con parámetros
    public Usuario(int id, String nombreUsuario, String password)
    {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.password = password;
    }

    // Getters y Setters
    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getNombreUsuario()
    {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario)
    {
        this.nombreUsuario = nombreUsuario;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}
