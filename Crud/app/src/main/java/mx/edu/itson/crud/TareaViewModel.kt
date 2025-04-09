package mx.edu.itson.crud

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class TareaViewModel: ViewModel() {
    private val db = Firebase.firestore

    private var _listaTareas = MutableLiveData<List<Tarea>>(emptyList())
    val listaTareas: LiveData<List<Tarea>> = _listaTareas

    init {
        obtenerTareasTiempoReal()
    }

    // NUEVO: Listener en tiempo real
    private fun obtenerTareasTiempoReal() {
        db.collection("tareas").addSnapshotListener { snapshot, error ->
            if (error != null) {
                error.printStackTrace()
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                val tareas = snapshot.documents.mapNotNull { it.toObject(Tarea::class.java) }
                _listaTareas.postValue(tareas)
            } else {
                _listaTareas.postValue(emptyList())
            }
        }
    }

    fun agregarTareas(tarea: Tarea) {
        tarea.id = UUID.randomUUID().toString()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("tareas").document(tarea.id).set(tarea).await()
                // Ya no necesitas actualizar la lista aquí
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun actualizarTareas(tarea: Tarea) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("tareas").document(tarea.id).update(tarea.toMap()).await()
                // Ya no necesitas actualizar la lista aquí
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun borrarTareas(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("tareas").document(id).delete().await()
                // Ya no necesitas actualizar la lista aquí
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}