package com.udb.mundoclima.network

import android.util.Log
import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException

class AppException(message: String, cause: Throwable? = null) : Exception(message, cause)

object ErrorMapper {
    fun message(e: Throwable): String = when (e) {
        is AppException -> e.message ?: "Error desconocido"
        is SocketTimeoutException -> "Tiempo de espera agotado. Intenta de nuevo."
        is UnknownHostException, is ConnectException -> "Sin conexión a internet."
        is HttpException -> http(e)
        is JsonParseException ->
            "La respuesta del servidor no tiene el formato esperado.\n(${e.message?.take(150)})"
        is IOException -> "Error de red. Verifica tu conexión."
        // TEMPORAL: muestra la causa real para depurar. Luego puedes volver al texto simple.
        else -> "Error inesperado: ${e.javaClass.simpleName}: ${e.message?.take(150)}"
    }

    private fun http(e: HttpException): String {
        val body = try { e.response()?.errorBody()?.string().orEmpty() } catch (_: Exception) { "" }
        Log.e("MundoClima", "HTTP ${e.code()} body=$body")
        return when (e.code()) {
            400 -> if (Regex("\"code\"\\s*:\\s*1006").containsMatchIn(body))
                "Ubicación no encontrada." else "Solicitud inválida."
            401, 403 -> "Token o API Key inválidos, o sin permisos."
            404 -> "Recurso no encontrado."
            429 -> "Se alcanzó el límite de solicitudes. Intenta más tarde."
            in 500..599 -> "El servidor no está disponible en este momento."
            else -> "Error HTTP ${e.code()}."
        }
    }
}

/** Ejecuta una llamada y la convierte en Result con mensaje amigable. */
suspend fun <T> safeCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Log.e("MundoClima", "safeCall falló", e)
    Result.failure(AppException(ErrorMapper.message(e), e))
}