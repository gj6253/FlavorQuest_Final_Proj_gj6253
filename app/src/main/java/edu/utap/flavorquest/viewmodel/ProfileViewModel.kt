package edu.utap.flavorquest.viewmodel

import androidx.lifecycle.ViewModel
import edu.utap.flavorquest.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val user = auth.currentUser ?: return
        val creationTimestamp = user.metadata?.creationTimestamp ?: 0L
        val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val memberSince = if (creationTimestamp > 0) {
            dateFormat.format(Date(creationTimestamp))
        } else {
            "Unknown"
        }

        _profile.value = UserProfile(
            uid = user.uid,
            displayName = user.displayName ?: "User",
            email = user.email ?: "",
            photoUrl = user.photoUrl?.toString() ?: "",
            memberSince = memberSince
        )
    }

    fun signOut() {
        auth.signOut()
    }
}
