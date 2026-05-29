package logica;

/**
 * Hilo 2: Mantenimiento de Sesión. Este hilo se ejecuta en segundo plano
 * durante el cuestionario para simular el control de una sesión activa,
 * imprimiendo un mensaje periódico.
 */
public class HiloSesion implements Runnable
{

    private final String nombreUsuario;
    private volatile boolean running = true; // Flag para detener el hilo de forma segura

    public HiloSesion(String nombreUsuario)
    {
        this.nombreUsuario = nombreUsuario;
    }

    @Override
    public void run()
    {
        System.out.println("Hilo 2 (Sesión) iniciado para el usuario: " + nombreUsuario);

        while (running)
        {
            try
            {
                // Imprime un mensaje para demostrar que está "monitoreando" la sesión
                System.out.println("[Sesión Activa] El usuario '" + nombreUsuario + "' sigue en el cuestionario.");

                // El hilo duerme durante 30 segundos antes de volver a chequear
                Thread.sleep(30000);

            } catch (InterruptedException e)
            {
                // Esto puede pasar si el hilo es interrumpido bruscamente
                System.out.println("Hilo 2 (Sesión) fue interrumpido.");
                running = false; // Asegura que el bucle termine
            }
        }

        System.out.println("Hilo 2 (Sesión) terminado para el usuario: " + nombreUsuario);
    }

    /**
     * Método para detener el hilo de forma segura desde el exterior.
     */
    public void detener()
    {
        this.running = false;
    }
}
