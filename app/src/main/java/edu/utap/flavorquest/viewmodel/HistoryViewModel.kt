package edu.utap.flavorquest.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.utap.flavorquest.data.model.SearchHistory
import edu.utap.flavorquest.data.repository.FlavorQuestRepository
import com.google.firebase.auth.FirebaseAuth
import edu.utap.flavorquest.data.local.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val selectedTab: Int = 0, // 0 = Cook at Home, 1 = Dine Out
    val historyItems: List<SearchHistory> = emptyList(),
    val filterPriceLevel: Int? = null,
    val filterRating: Float? = null
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = FlavorQuestRepository(
        database.recipeDao(),
        database.restaurantDao(),
        database.searchHistoryDao()
    )
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId: String get() = auth.currentUser?.uid ?: ""

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val type = if (_uiState.value.selectedTab == 0) "recipe" else "restaurant"
            repository.getHistoryByType(currentUserId, type).collect { items ->
                _uiState.value = _uiState.value.copy(historyItems = items)
            }
        }
    }

    fun deleteHistoryItem(item: SearchHistory) {
        viewModelScope.launch {
            repository.deleteHistory(item)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory(currentUserId)
        }
    }

    fun setFilterPrice(price: Int?) {
        _uiState.value = _uiState.value.copy(filterPriceLevel = price)
    }

    fun setFilterRating(rating: Float?) {
        _uiState.value = _uiState.value.copy(filterRating = rating)
    }
}
