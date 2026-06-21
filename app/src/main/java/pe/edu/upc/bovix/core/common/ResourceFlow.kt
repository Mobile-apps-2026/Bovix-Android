package pe.edu.upc.bovix.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

/**
 * Emite Loading → cache (si existe) → resultado remoto.
 * Solo propaga error si no había cache disponible.
 *
 * @param loadCache  lee Room; devuelve null si no hay datos cacheados
 * @param fetch      llama al API y persiste en Room; devuelve el dato ya mapeado
 * @param errorMessage mensaje de fallback si la excepción no tiene mensaje
 */
fun <T> resourceFlow(
    loadCache: suspend () -> T?,
    fetch: suspend () -> T,
    errorMessage: String
): Flow<Resource<T>> = flow {
    emit(Resource.Loading)
    val cached = loadCache()
    if (cached != null) emit(Resource.Success(cached))
    try {
        emit(Resource.Success(fetch()))
    } catch (io: IOException) {
        if (cached == null) emit(Resource.Error("Sin conexión", io))
    } catch (e: Exception) {
        if (cached == null) emit(Resource.Error(e.localizedMessage ?: errorMessage, e))
    }
}
