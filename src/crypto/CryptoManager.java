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
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Base64;
import java.security.cert.X509Certificate;
import java.math.BigInteger;
import java.util.Date;
import java.security.Security;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class CryptoManager
{

    private static final int ITERACIONES_PBKDF2 = 310000;
    private static final int LONGITUD_CLAVE_BITS = 256;
    private static final int LONGITUD_SALT_BYTES = 16;
    private static final String SAL_AES = "QuizSystemSalt2025";
    private static final String CLAVE_MAESTRA_AES = "ClaveMaestraSistemaQuiz_2025";
    private static final int LONGITUD_IV_GCM = 12;
    private static final int LONGITUD_TAG_GCM = 128;

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
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
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
    // CERTIFICADO X.509
    // -------------------------------------------------------------------------

    public static X509Certificate generarCertificadoX509(KeyPair par)
    {
        try
        {
            X500Name nombre = new X500Name("CN=SistemaQuiz, O=Universidad, C=MX");
            BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
            Date desde = new Date(System.currentTimeMillis() - 86400000L);
            Date hasta = new Date(System.currentTimeMillis() + (365L * 10 * 24 * 60 * 60 * 1000));

            X509v3CertificateBuilder constructor = new JcaX509v3CertificateBuilder(
                nombre, serial, desde, hasta, nombre, par.getPublic()
            );

            ContentSigner firmante = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider("BC")
                .build(par.getPrivate());

            return new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(constructor.build(firmante));
        }
        catch (Exception e)
        {
            System.out.println("Error al generar certificado X509: " + e.getMessage());
            return null;
        }
    }

    public static X509Certificate cargarOGenerarCertificadoX509(KeyPair par, String rutaDir)
    {
        File archivoCert = new File(rutaDir + "/cert_x509.b64");

        if (!archivoCert.exists())
        {
            System.out.println("Certificado X509 no encontrado. Generando nuevo...");
            X509Certificate cert = generarCertificadoX509(par);
            if (cert != null)
            {
                try
                {
                    String certB64 = Base64.getEncoder().encodeToString(cert.getEncoded());
                    escribirArchivo(rutaDir + "/cert_x509.b64", certB64);
                }
                catch (Exception e)
                {
                    System.out.println("Error al guardar certificado X509: " + e.getMessage());
                }
            }
            return cert;
        }

        try
        {
            byte[] certBytes = Base64.getDecoder().decode(leerArchivo(archivoCert));
            java.security.cert.CertificateFactory fabrica =
                java.security.cert.CertificateFactory.getInstance("X.509");
            return (X509Certificate) fabrica.generateCertificate(
                new java.io.ByteArrayInputStream(certBytes));
        }
        catch (Exception e)
        {
            System.out.println("Error al cargar certificado X509: " + e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // AES-256-GCM — CIFRADO SIMÉTRICO
    // -------------------------------------------------------------------------

    private static SecretKey obtenerClaveAES()
    {
        try
        {
            PBEKeySpec spec = new PBEKeySpec(
                CLAVE_MAESTRA_AES.toCharArray(),
                SAL_AES.getBytes("UTF-8"),
                ITERACIONES_PBKDF2,
                LONGITUD_CLAVE_BITS
            );
            SecretKeyFactory fabrica = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] claveBytes = fabrica.generateSecret(spec).getEncoded();
            spec.clearPassword();
            return new SecretKeySpec(claveBytes, "AES");
        }
        catch (Exception e)
        {
            System.out.println("Error al derivar clave AES: " + e.getMessage());
            return null;
        }
    }

    public static String cifrarAES(String textoPlano)
    {
        try
        {
            if (textoPlano == null)
            {
                return null;
            }
            SecretKey clave = obtenerClaveAES();
            byte[] iv = new byte[LONGITUD_IV_GCM];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, clave, new GCMParameterSpec(LONGITUD_TAG_GCM, iv));
            byte[] cifrado = cipher.doFinal(textoPlano.getBytes("UTF-8"));

            byte[] combinado = new byte[iv.length + cifrado.length];
            System.arraycopy(iv, 0, combinado, 0, iv.length);
            System.arraycopy(cifrado, 0, combinado, iv.length, cifrado.length);

            return "AES:" + Base64.getEncoder().encodeToString(combinado);
        }
        catch (Exception e)
        {
            System.out.println("Error al cifrar AES: " + e.getMessage());
            return textoPlano;
        }
    }

    public static String descifrarAES(String textoCifrado)
    {
        try
        {
            if (textoCifrado == null || !textoCifrado.startsWith("AES:"))
            {
                return textoCifrado;
            }
            String base64 = textoCifrado.substring(4);
            byte[] combinado = Base64.getDecoder().decode(base64);

            byte[] iv = new byte[LONGITUD_IV_GCM];
            byte[] cifrado = new byte[combinado.length - LONGITUD_IV_GCM];
            System.arraycopy(combinado, 0, iv, 0, LONGITUD_IV_GCM);
            System.arraycopy(combinado, LONGITUD_IV_GCM, cifrado, 0, cifrado.length);

            SecretKey clave = obtenerClaveAES();
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, clave, new GCMParameterSpec(LONGITUD_TAG_GCM, iv));
            byte[] descifrado = cipher.doFinal(cifrado);

            return new String(descifrado, "UTF-8");
        }
        catch (Exception e)
        {
            System.out.println("Error al descifrar AES: " + e.getMessage());
            return textoCifrado;
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
