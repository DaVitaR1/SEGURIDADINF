package crypto;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Base64;

public class CryptoManager
{

    private static final int ITERACIONES_PBKDF2 = 310000;
    private static final int LONGITUD_CLAVE_BITS = 256;
    private static final int LONGITUD_SALT_BYTES = 16;

    // -------------------------------------------------------------------------
    // CONTRASEÑAS — PBKDF2WithHmacSHA256
    // -------------------------------------------------------------------------

    public static String hashPassword(String password)
    {
        try
        {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[LONGITUD_SALT_BYTES];
            random.nextBytes(salt);

            PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERACIONES_PBKDF2,
                LONGITUD_CLAVE_BITS
            );
            SecretKeyFactory fabrica = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = fabrica.generateSecret(spec).getEncoded();
            spec.clearPassword();

            return "PBKDF2:" + bytesAHex(salt) + ":" + bytesAHex(hash);
        }
        catch (Exception e)
        {
            System.out.println("Error al hashear contraseña: " + e.getMessage());
            return null;
        }
    }

    public static boolean verificarPassword(String passwordIngresado, String hashAlmacenado)
    {
        try
        {
            String[] partes = hashAlmacenado.split(":");
            if (partes.length != 3 || !partes[0].equals("PBKDF2"))
            {
                return false;
            }

            byte[] salt = hexABytes(partes[1]);
            byte[] hashOriginal = hexABytes(partes[2]);

            PBEKeySpec spec = new PBEKeySpec(
                passwordIngresado.toCharArray(),
                salt,
                ITERACIONES_PBKDF2,
                LONGITUD_CLAVE_BITS
            );
            SecretKeyFactory fabrica = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hashNuevo = fabrica.generateSecret(spec).getEncoded();
            spec.clearPassword();

            return comparacionSegura(hashOriginal, hashNuevo);
        }
        catch (Exception e)
        {
            System.out.println("Error al verificar contraseña: " + e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // HASH SHA-256
    // -------------------------------------------------------------------------

    public static String sha256(String contenido)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(contenido.getBytes("UTF-8"));
            return bytesAHex(hashBytes);
        }
        catch (Exception e)
        {
            System.out.println("Error al calcular SHA-256: " + e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // RSA — FIRMA DIGITAL
    // -------------------------------------------------------------------------

    public static KeyPair generarParRSA()
    {
        try
        {
            KeyPairGenerator generador = KeyPairGenerator.getInstance("RSA");
            generador.initialize(2048, new SecureRandom());
            return generador.generateKeyPair();
        }
        catch (Exception e)
        {
            System.out.println("Error al generar par RSA: " + e.getMessage());
            return null;
        }
    }

    public static String firmar(String contenido, PrivateKey clavePrivada)
    {
        try
        {
            Signature firma = Signature.getInstance("SHA256withRSA");
            firma.initSign(clavePrivada);
            firma.update(contenido.getBytes("UTF-8"));
            byte[] bytesRaw = firma.sign();
            return Base64.getEncoder().encodeToString(bytesRaw);
        }
        catch (Exception e)
        {
            System.out.println("Error al firmar contenido: " + e.getMessage());
            return null;
        }
    }

    public static boolean verificarFirma(String contenido, String firmaBase64, PublicKey clavePublica)
    {
        try
        {
            Signature verificador = Signature.getInstance("SHA256withRSA");
            verificador.initVerify(clavePublica);
            verificador.update(contenido.getBytes("UTF-8"));
            byte[] bytesRaw = Base64.getDecoder().decode(firmaBase64);
            return verificador.verify(bytesRaw);
        }
        catch (Exception e)
        {
            System.out.println("Error al verificar firma: " + e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // GESTIÓN DE CLAVES EN DISCO
    // -------------------------------------------------------------------------

    public static void guardarClaves(KeyPair par, String rutaDir)
    {
        try
        {
            String clavePublicaB64 = Base64.getEncoder().encodeToString(
                par.getPublic().getEncoded()
            );
            String clavePrivadaB64 = Base64.getEncoder().encodeToString(
                par.getPrivate().getEncoded()
            );

            escribirArchivo(rutaDir + "/public_key.b64", clavePublicaB64);
            escribirArchivo(rutaDir + "/private_key.b64", clavePrivadaB64);
        }
        catch (Exception e)
        {
            System.out.println("Error al guardar claves RSA: " + e.getMessage());
        }
    }

    public static KeyPair cargarClaves(String rutaDir)
    {
        File archivoPublica = new File(rutaDir + "/public_key.b64");
        File archivoPrivada = new File(rutaDir + "/private_key.b64");

        if (!archivoPublica.exists() || !archivoPrivada.exists())
        {
            System.out.println("Claves RSA no encontradas. Generando nuevas claves...");
            KeyPair nuevoPar = generarParRSA();
            if (nuevoPar != null)
            {
                guardarClaves(nuevoPar, rutaDir);
            }
            return nuevoPar;
        }

        try
        {
            byte[] bytesPublica = Base64.getDecoder().decode(leerArchivo(archivoPublica));
            byte[] bytesPrivada = Base64.getDecoder().decode(leerArchivo(archivoPrivada));

            KeyFactory fabrica = KeyFactory.getInstance("RSA");
            PublicKey clavePublica = fabrica.generatePublic(new X509EncodedKeySpec(bytesPublica));
            PrivateKey clavePrivada = fabrica.generatePrivate(new PKCS8EncodedKeySpec(bytesPrivada));

            return new KeyPair(clavePublica, clavePrivada);
        }
        catch (Exception e)
        {
            System.out.println("Error al cargar claves RSA: " + e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // UTILIDADES INTERNAS
    // -------------------------------------------------------------------------

    private static boolean comparacionSegura(byte[] a, byte[] b)
    {
        if (a.length != b.length)
        {
            return false;
        }
        int diferencia = 0;
        for (int i = 0; i < a.length; i++)
        {
            diferencia |= a[i] ^ b[i];
        }
        return diferencia == 0;
    }

    private static String bytesAHex(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
        {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] hexABytes(String hex)
    {
        int longitud = hex.length();
        byte[] resultado = new byte[longitud / 2];
        for (int i = 0; i < longitud; i += 2)
        {
            resultado[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return resultado;
    }

    private static void escribirArchivo(String ruta, String contenido)
    {
        try
        {
            FileWriter escritor = new FileWriter(ruta);
            escritor.write(contenido);
            escritor.close();
        }
        catch (Exception e)
        {
            System.out.println("Error al escribir archivo " + ruta + ": " + e.getMessage());
        }
    }

    private static String leerArchivo(File archivo)
    {
        StringBuilder sb = new StringBuilder();
        try
        {
            BufferedReader lector = new BufferedReader(new FileReader(archivo));
            String linea;
            while ((linea = lector.readLine()) != null)
            {
                sb.append(linea);
            }
            lector.close();
        }
        catch (Exception e)
        {
            System.out.println("Error al leer archivo " + archivo.getName() + ": " + e.getMessage());
        }
        return sb.toString();
    }
}
