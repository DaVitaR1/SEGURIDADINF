package modelo;

import java.util.List;

public class Pregunta
{

    private int id;
    private String textoPregunta;
    private List<Opcion> opciones; // Una pregunta tiene una lista de opciones

    public Pregunta(int id, String textoPregunta, List<Opcion> opciones)
    {
        this.id = id;
        this.textoPregunta = textoPregunta;
        this.opciones = opciones;
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

    public String getTextoPregunta()
    {
        return textoPregunta;
    }

    public void setTextoPregunta(String textoPregunta)
    {
        this.textoPregunta = textoPregunta;
    }

    public List<Opcion> getOpciones()
    {
        return opciones;
    }

    public void setOpciones(List<Opcion> opciones)
    {
        this.opciones = opciones;
    }
}
