package modelo;

public class Opcion
{

    private int id;
    private String textoOpcion;
    private boolean esCorrecta;

    public Opcion(int id, String textoOpcion, boolean esCorrecta)
    {
        this.id = id;
        this.textoOpcion = textoOpcion;
        this.esCorrecta = esCorrecta;
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

    public String getTextoOpcion()
    {
        return textoOpcion;
    }

    public void setTextoOpcion(String textoOpcion)
    {
        this.textoOpcion = textoOpcion;
    }

    public boolean isEsCorrecta()
    {
        return esCorrecta;
    }

    public void setEsCorrecta(boolean esCorrecta)
    {
        this.esCorrecta = esCorrecta;
    }
}
