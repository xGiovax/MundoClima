# MundoClima

Aplicación Android desarrollada en Kotlin para el **Desafío Práctico #03** de *Desarrollo de Software para Móviles* (Universidad Don Bosco, Escuela de Computación).

MundoClima permite explorar el mundo por regiones: el usuario elige una región, consulta sus países y, al abrir uno, ve su ficha completa, un mapa interactivo con sus ciudades principales y el clima actual de cada una.

## Descarga del APK

[Descargar MundoClima (APK) desde Google Drive](https://drive.google.com/drive/folders/1kx-pKit-YM-t6j5U-20Mw2nt0FJd2fgn?usp=sharing)

## Autores

| Nombre | Carné |
|---|---|
| Giovanni Alberto Ruano Martínez | RM250065 |
| Odaly Rachel Cruz Franco | CF252175 |

## Funcionalidades

- Lista de regiones y de los países de cada región, con bandera y capital.
- Detalle del país: nombre común y oficial, capital, región, subregión, población, códigos ISO, monedas, idiomas y coordenadas.
- Hasta 10 ciudades principales por país, con la capital destacada.
- Clima actual de cada ciudad: temperatura en °C y °F, condición, viento, humedad y última actualización.
- Mapa interactivo con un marcador por ciudad (nombre y clima); al elegir una ciudad el mapa se centra en ella.
- Indicadores de carga y manejo de errores: token o API Key inválidos, falta de conexión, tiempo de espera agotado, límite de solicitudes, país sin ciudades y ubicación no encontrada. Si falla el clima de una ciudad, el resto de la pantalla sigue funcionando.

## APIs consumidas

| API | Uso | Autenticación |
|---|---|---|
| [REST Countries v5](https://restcountries.com) | Datos del país | Bearer token |
| [GeoNames](https://www.geonames.org) | Ciudades principales | Nombre de usuario |
| [WeatherAPI](https://www.weatherapi.com) | Clima actual | API Key |

## Tecnologías

Kotlin · Retrofit y OkHttp · Gson · Corrutinas · Coil · osmdroid (OpenStreetMap) · ViewBinding · Material Components

## Arquitectura (MVC)

```
com.udb.mundoclima
├── model        Clases de datos y CountryMapper
├── view         Activities, adaptadores y layouts
├── controller   Lógica de negocio por API
└── network      Cliente Retrofit, endpoints y manejo de errores
```

## Configuración del proyecto

Las credenciales **no** se incluyen en el repositorio.

1. Copia `local.properties.example` como `local.properties` en la raíz del proyecto.
2. Completa tus propias credenciales:

```
RESTCOUNTRIES_TOKEN=tu_bearer_token
GEONAMES_USER=tu_usuario_geonames
WEATHER_API_KEY=tu_api_key_weatherapi
```

3. Sincroniza Gradle y ejecuta la app (`Run`) o genera el APK con `./gradlew assembleDebug`.
