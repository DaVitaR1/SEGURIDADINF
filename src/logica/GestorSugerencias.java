package logica;

import java.util.ArrayList;
import java.util.List;
import modelo.CursoSugerido;

public class GestorSugerencias
{

    public static List<CursoSugerido> obtenerSugerencias(double calificacion)
    {
        List<CursoSugerido> cursos = new ArrayList<>();

        // --- NIVEL: NECESITA MEJORAR (< 60) ---
        if (calificacion < 60)
        {
            cursos.add(new CursoSugerido("Fundamentos de Sistemas Distribuidos",
                    "En este curso exploraremos los pilares esenciales como la transparencia, escalabilidad y los modelos de comunicación que rigen los sistemas modernos."));

            cursos.add(new CursoSugerido("Redes de Computadoras y Protocolos",
                    "Aprenderás cómo viajan los datos a través de TCP/IP, el modelo OSI y cómo diagnosticar problemas de conectividad en entornos distribuidos complejos."));

            cursos.add(new CursoSugerido("Introducción a Bases de Datos NoSQL",
                    "Descubrirás el mundo de los datos no estructurados, el teorema CAP y cómo gestionar la consistencia eventual en bases como MongoDB o Cassandra."));

            cursos.add(new CursoSugerido("Administración Básica de Linux",
                    "Dominarás la línea de comandos, gestión de procesos y permisos, habilidades indispensables para cualquier administrador de servidores."));

            // --- NIVEL: APROBADO (60 - 89) ---
        } else if (calificacion < 90)
        {
            cursos.add(new CursoSugerido("Algoritmos de Consenso (Paxos y Raft)",
                    "Profundizaremos en cómo los nodos se ponen de acuerdo en entornos hostiles, estudiando la elección de líderes y la replicación de máquinas de estado."));

            cursos.add(new CursoSugerido("Arquitectura de Microservicios",
                    "Aprenderás a descomponer aplicaciones monolíticas en servicios independientes, gestionando la comunicación asíncrona y el despliegue continuo."));

            cursos.add(new CursoSugerido("Patrones de Diseño Distribuido",
                    "Implementarás patrones robustos como Circuit Breaker para fallos, Saga para transacciones distribuidas y CQRS para optimizar lecturas."));

            cursos.add(new CursoSugerido("Seguridad en Sistemas Distribuidos",
                    "Estudiarás autenticación con OAuth2, encriptación TLS y cómo proteger APIs expuestas en redes públicas contra ataques comunes."));

            // --- NIVEL: EXCELENTE (>= 90) ---
        } else
        {
            cursos.add(new CursoSugerido("Computación en la Nube Avanzada",
                    "Dominarás arquitecturas Serverless en AWS/Azure, gestión de costos y diseño de soluciones nativas de la nube de escala global."));

            cursos.add(new CursoSugerido("Sistemas de Alta Disponibilidad",
                    "Diseñarás sistemas que nunca se detienen mediante balanceo de carga global, replicación multirregión y estrategias de Disaster Recovery."));

            cursos.add(new CursoSugerido("Kubernetes y Orquestación",
                    "Te convertirás en experto gestionando clústeres de contenedores, automatizando el escalado y la sanación automática de servicios en producción."));

            cursos.add(new CursoSugerido("Tecnología Blockchain y Web3",
                    "Explorarás los sistemas distribuidos descentralizados, contratos inteligentes y cómo garantizar la inmutabilidad de los datos sin una autoridad central."));
        }

        return cursos;
    }
}
