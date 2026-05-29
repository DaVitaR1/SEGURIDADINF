package modelo;

public class CursoSugerido {
    private String nombre;
    private String descripcion;

    public CursoSugerido(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getDetalle() {
        return "• " + nombre + ": " + descripcion;
    }
}