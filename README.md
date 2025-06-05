# RETO 3 - Implementación de protocolo MCP con cliente y servidor 

## Opción A - Desarrollo completo del protocolo (cliente y servidor) 
## Índice
### · Requisitos
### · Configuración
### · Estructura del proyecto
### · Explicación
### · Notas técnicas
### · Advertencias

## Requisitos
- Java 17 o superior
- Maven
- Docker
- Quarkus
- Cuenta de Google
- API google calendar
- Credenciales de proyecto Google Cloud Console
- Client MCP (goose, )

## Configuración
1. Seguir los pasos de https://es.quarkus.io/get-started/
2. Clonar el repositorio
3. Crea un proyecto en Google Cloud https://console.cloud.google.com/welcome?inv=1&invt=AbzSaw&project=light-sunup-461210-i1, click en my first proyect y proyecto nuevo.
4. Dentro del proyecto, APIS y servicios -> Biblioteca -> Google Calendar API -> Habilitar.
5. Ir a: APIs y servicios -> Credenciales -> Crear credenciales -> Crear ID de cliente OAuth -> Configurar pantalla de consentimiento -> Crear cliente.
6. Descargar JSON y copiarlo en la carpeta Resources.

## Estructura del proyecto
```
src/
├── main/
│   ├── java/
│   │   └── org.acme/
│   │       ├── GoogleAuthService.java    # Manejo de OAuth2 con Google
│   │       ├── GoogleCalendarConfig.java # Cliente Calendar configurado
│   │       └── tools/
│   │           └── DeleteEvent.java      # Comandos para borrar eventos
│   └── resources/
│       └── credentials.json              # Clave de acceso OAuth
```
## Explicación
pom.xml
Archivo central de configuración del proyecto Maven.
Declara que es un proyecto Quarkus, con dependencias para REST y el cliente de API de Google.

### Servidor Quarkus
- GoogleAuthService: Se importan paquetes de Google OAuth, JSON y Jakarta para que sea compatible con entornos Quarkus.
Las constantes JSON_FACTORY y SCOPES contienen las credenciales y los permisos que dan acceso al calendario.

    Método authorize(): 

    1. lee las credenciales y realiza el proceso de autorización.
    2. Define como se va a autenticar el usuario, en este caso "setAccessType("offline")", permite el reaacceso sin tener que volver a auntenticar, guarda los tokens en un directorio llamado "tokens".
    3. Inicia un servidor en local y abre el navegador mediante HTTP en el puerto 8888.
    4. Autoriza al usuario, el usuario inicia sesión y guarda las credenciales.

- GoogleCalendarConfig: devuelve el nombre del calendario de Google que se usará, el calendario principal se guarda como "primary"
```
@ApplicationScoped
public class GoogleCalendarConfig {
    @Produces
    public String calendarId() {
        return "primary";
    }
}

```

### tools
- CreateEvent: crea un evento nuevo en el calendario de Google del usuario. Define un endpoint REST (una URL de tipo POST) que, al recibir un JSON con información del evento.
- DeleteEvent: elimina eventos existentes en el calendario de Google del usuario.
    - deleteEventsByQuery(): borra eventos que coincidan con un texto de búsqueda.
    - deleteEventsByDateRange(): borra todos los eventos que ocurran entre dos fechas.
    - deleteRecurringEvent(): borra eventos por eventId o instanceDate.
    - clearAllEvents(): Borra absolutamente todos los eventos
## Notas técnicas
- El calendario predeterminado usado es primary.
- El flujo de autenticación usa LocalServerReceiver en el puerto 8888.
- Los tokens se almacenan en tokens/ para permitir acceso "offline".

## Advertencias
- Eliminar eventos es irreversible.
- Asegúrate de tener permisos adecuados en el calendario.
- No compartas el archivo credentials.json.

