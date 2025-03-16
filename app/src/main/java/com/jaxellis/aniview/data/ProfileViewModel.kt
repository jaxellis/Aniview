package com.jaxellis.aniview.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.apollographql.apollo.cache.normalized.normalizedCache
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.exception.ApolloNetworkException
import com.jaxellis.aniview.GetUserAnimeStatisticsQuery
import com.jaxellis.aniview.GetUserProfileQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class ProfileViewModel : ViewModel() {
    
    // Apollo client with caching setup
    private val apolloClient = ApolloClient.Builder()
        .serverUrl("https://graphql.anilist.co")
        .normalizedCache(
            SqlNormalizedCacheFactory("apollo.db")
        )
        .build()
    
    // User data state
    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()
    
    // Loading state
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    // Last refresh timestamp
    private var lastRefreshTime = 0L
    
    // Cache timeout (10 minutes)
    private val cacheTimeout = TimeUnit.MINUTES.toMillis(10)
    
    init {
        fetchUserProfile(false)
    }
    
    fun fetchUserProfile(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                
                // Try to load cached data first if available
                if (!forceRefresh) {
                    try {
                        val cachedResponse = apolloClient.query(GetUserProfileQuery(Optional.present("")))
                            .fetchPolicy(FetchPolicy.CacheOnly)
                            .execute()
                            
                        val cachedData = cachedResponse.data?.User
                        if (cachedData != null) {
                            // We have cached data, update UI while we fetch fresh data
                            _profileState.update { currentState ->
                                currentState.copy(
                                    username = cachedData.name,
                                    avatarUrl = cachedData.avatar?.medium,
                                    bannerUrl = cachedData.bannerImage,
                                    about = cachedData.about,
                                    animeCount = cachedData.statistics?.anime?.count ?: 0,
                                    mangaCount = cachedData.statistics?.manga?.count ?: 0,
                                    reviewCount = 0,
                                    error = null,
                                    isUserDataLoaded = true
                                )
                            }
                        }
                    } catch (e: Exception) {
                        // If we can't get cached data, just continue to network request
                        Log.d("ProfileViewModel", "No cache available, trying network")
                    }
                }
                
                // If forced refresh or cache timeout exceeded
                val shouldFetchFromNetwork = forceRefresh || 
                    (System.currentTimeMillis() - lastRefreshTime > cacheTimeout)
                
                // Choose fetch policy based on refresh state
                val fetchPolicy = if (shouldFetchFromNetwork) {
                    FetchPolicy.NetworkFirst
                } else {
                    FetchPolicy.CacheFirst
                }
                
                // Fetch basic profile info
                val profileResponse = apolloClient.query(GetUserProfileQuery(Optional.present("")))
                    .fetchPolicy(fetchPolicy)
                    .execute()
                
                // Fetch detailed statistics
                val userId = profileResponse.data?.User?.id
                val statsResponse = if (userId != null) {
                    apolloClient.query(GetUserAnimeStatisticsQuery(Optional.present(userId)))
                        .fetchPolicy(fetchPolicy)
                        .execute()
                } else null
                
                if (profileResponse.hasErrors()) {
                    Log.e("ProfileViewModel", "GraphQL errors: ${profileResponse.errors?.joinToString { it.message }}")
                    _profileState.update { it.copy(error = "Failed to load profile data") }
                    return@launch
                }
                
                val userData = profileResponse.data?.User
                val statsData = statsResponse?.data?.User?.statistics
                
                if (userData != null) {
                    _profileState.update { currentState ->
                        currentState.copy(
                            username = userData.name,
                            avatarUrl = userData.avatar?.medium,
                            bannerUrl = userData.bannerImage,
                            about = userData.about,
                            animeCount = userData.statistics?.anime?.count ?: 0,
                            mangaCount = userData.statistics?.manga?.count ?: 0,
                            reviewCount = 0, // Not available in basic query, would need separate query
                            error = null,
                            isUserDataLoaded = true
                        )
                    }
                    
                    // Update last refresh time if we fetched from network
                    if (shouldFetchFromNetwork) {
                        lastRefreshTime = System.currentTimeMillis()
                    }
                } else {
                    _profileState.update { it.copy(error = "No user data found") }
                }
                
            } catch (e: ApolloNetworkException) {
                Log.e("ProfileViewModel", "Network error", e)
                _profileState.update { it.copy(
                    error = "Cannot connect to server. Please check your internet connection.",
                    isUserDataLoaded = _profileState.value.isUserDataLoaded // Keep existing state
                )}
            } catch (e: UnknownHostException) {
                Log.e("ProfileViewModel", "Host not found", e)
                _profileState.update { it.copy(
                    error = "Cannot resolve host. Please check your internet connection.",
                    isUserDataLoaded = _profileState.value.isUserDataLoaded // Keep existing state
                )}
            } catch (e: ApolloException) {
                Log.e("ProfileViewModel", "Apollo exception", e)
                _profileState.update { it.copy(
                    error = "Error loading data: ${e.message}",
                    isUserDataLoaded = _profileState.value.isUserDataLoaded // Keep existing state
                )}
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Unexpected error", e)
                _profileState.update { it.copy(
                    error = "An unexpected error occurred",
                    isUserDataLoaded = _profileState.value.isUserDataLoaded // Keep existing state
                )}
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    
    fun refreshProfile() {
        fetchUserProfile(true)
    }
}

data class ProfileState(
    val username: String = "",
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    val about: String? = null,
    val animeCount: Int = 0,
    val mangaCount: Int = 0,
    val reviewCount: Int = 0,
    val error: String? = null,
    val isUserDataLoaded: Boolean = false
) 