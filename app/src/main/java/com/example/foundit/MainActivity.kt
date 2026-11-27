@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.foundit

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.example.foundit.viewmodel.RemoteFeedState
import com.example.foundit.viewmodel.ProfileViewModel
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.foundit.data.local.FoundItemDatabase
import com.example.foundit.data.local.FoundItemEntity
import com.example.foundit.data.local.UserEntity
import com.example.foundit.data.local.CommunityDao
import com.example.foundit.data.local.ChatMessageEntity
import com.example.foundit.data.local.ReviewEntity
import com.example.foundit.data.local.toFoundItem
import com.example.foundit.ui.theme.FoundItTheme
import com.example.foundit.location.LocationPermissionControl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.withContext
import com.maptiler.maptilersdk.MTConfig
import com.maptiler.maptilersdk.annotations.MTCustomAnnotationView
import com.maptiler.maptilersdk.map.LngLat
import com.maptiler.maptilersdk.map.MTMapOptions
import com.maptiler.maptilersdk.map.MTMapView
import com.maptiler.maptilersdk.map.MTMapViewController
import com.maptiler.maptilersdk.map.options.MTCameraOptions
import com.maptiler.maptilersdk.map.style.MTMapReferenceStyle
import com.maptiler.maptilersdk.map.style.MTMapStyleVariant
import com.maptiler.maptilersdk.map.types.MTPoint
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.foundit.viewmodel.ChatViewModel
import com.example.foundit.viewmodel.ExploreViewModel

private val Cream = Color(0xFFF5F1E8)
private val Paper = Color(0xFFFFFEFA)
private val Forest = Color(0xFF234B3A)
private val ForestLight = Color(0xFF35634D)
private val Sage = Color(0xFFDCE5D8)
private val Olive = Color(0xFF788477)
private val Terracotta = Color(0xFFB96F50)
private val Ink = Color(0xFF26342C)

private data class Category(val emoji: String, val label: String)
private val categories = listOf(
    Category("🔑", "Keys"), Category("📱", "Electronics"), Category("👛", "Wallets"),
    Category("🪪", "Documents"), Category("🎒", "Bags"), Category("💍", "Jewellery"),
    Category("👓", "Glasses"), Category("🧸", "Other")
)

data class FoundItem(
    val id: Int,
    val title: String,
    val location: String,
    val category: String,
    val date: String,
    val description: String = "No description provided.",
    val finderContact: String = "",
    val photoUri: String? = null,
    val isFavorite: Boolean = false,
    val ownerEmail: String = "",
    val status: String = "FOUND"
)

private enum class AppPage { LOGIN, REGISTER, EXPLORE, CREATE, DETAIL, FAVORITES, PROFILE, EDIT_PROFILE, PUBLIC_PROFILE, CHAT_INBOX, CHAT, REVIEW }


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        MTConfig.apiKey = BuildConfig.MAPTILER_API_KEY

        setContent {
            FoundItTheme {

                // Temporary local support recipient for chat testing
                val supportEmail = "support@foundit.local"

                val db = remember {
                    FoundItemDatabase.getDatabase(applicationContext)
                }
                val itemDao = remember { db.foundItemDao() }
                val userDao = remember { db.userDao() }
                val communityDao = remember { db.communityDao() }

                val prefs = remember {
                    getSharedPreferences("foundit_session", Context.MODE_PRIVATE)
                }

                val exploreViewModel: ExploreViewModel = viewModel()
                val remoteState by exploreViewModel.remoteState
                    .collectAsStateWithLifecycle()

                val profileViewModel: ProfileViewModel = viewModel()
                val chatViewModel: ChatViewModel = viewModel()

                val itemsDb by exploreViewModel.localListings
                    .collectAsStateWithLifecycle()

                val items = remember(itemsDb) {
                    itemsDb.map { it.toFoundItem() }
                }


                LaunchedEffect(Unit) {
                    val alreadySeeded = prefs.getBoolean(
                        "foundit_demo_listings_v1",
                        false
                    )

                    if (!alreadySeeded) {
                        val demoListings = listOf(
                            FoundItemEntity(
                                title = "Wireless Headphones",
                                location = "Sheffield Railway Station",
                                category = "Electronics",
                                date = "Today",
                                description = "Black wireless headphones found near the station.",
                                finderContact = "alex.demo@example.test",
                                ownerEmail = "alex.demo@example.test",
                                status = "FOUND"
                            ),
                            FoundItemEntity(
                                title = "Brown Leather Wallet",
                                location = "Kelham Island, Sheffield",
                                category = "Wallets",
                                date = "Yesterday",
                                description = "Brown leather wallet found in a coffee shop.",
                                finderContact = "mia.demo@example.test",
                                ownerEmail = "mia.demo@example.test",
                                status = "FOUND"
                            ),
                            FoundItemEntity(
                                title = "Set of House Keys",
                                location = "Broomhill, Sheffield",
                                category = "Keys",
                                date = "Today",
                                description = "Several keys with a blue keyring.",
                                finderContact = "oliver.demo@example.test",
                                ownerEmail = "oliver.demo@example.test",
                                status = "FOUND"
                            ),
                            FoundItemEntity(
                                title = "Black Backpack",
                                location = "Crookes, Sheffield",
                                category = "Bags",
                                date = "Yesterday",
                                description = "Black backpack left near a park.",
                                finderContact = "sophie.demo@example.test",
                                ownerEmail = "sophie.demo@example.test",
                                status = "FOUND"
                            ),
                            FoundItemEntity(
                                title = "Gold Necklace",
                                location = "Sheffield City Centre",
                                category = "Jewellery",
                                date = "Today",
                                description = "Small gold necklace with a pendant.",
                                finderContact = "noah.demo@example.test",
                                ownerEmail = "noah.demo@example.test",
                                status = "FOUND"
                            ),
                            FoundItemEntity(
                                title = "Black Smartphone",
                                location = "Ecclesall Road, Sheffield",
                                category = "Electronics",
                                date = "Today",
                                description = "Black smartphone found near a bus stop.",
                                finderContact = "emma.demo@example.test",
                                ownerEmail = "emma.demo@example.test",
                                status = "FOUND"
                            )
                        )

                        withContext(Dispatchers.IO) {
                            demoListings.forEach { item ->
                                itemDao.insertItem(item)
                            }
                        }

                        prefs.edit()
                            .putBoolean("foundit_demo_listings_v1", true)
                            .apply()
                    }
                }

                val scope = rememberCoroutineScope()

                var currentUser by remember {
                    mutableStateOf<UserEntity?>(null)
                }

                val profileUser by profileViewModel.profile
                    .collectAsStateWithLifecycle()

                LaunchedEffect(currentUser?.email) {
                    currentUser?.email?.let { email ->
                        profileViewModel.load(email)
                    }
                }

                var page by remember { mutableStateOf(AppPage.LOGIN) }
                var selectedItem by remember { mutableStateOf<FoundItem?>(null) }

                var detailReturnPage by remember {
                    mutableStateOf(AppPage.EXPLORE)
                }

                var detailReturnPageBeforeChat by remember {
                    mutableStateOf(AppPage.EXPLORE)
                }

                var detailReturnPageBeforePublicProfile by remember {
                    mutableStateOf(AppPage.EXPLORE)
                }

                var publicProfileReturnPage by remember {
                    mutableStateOf(AppPage.CHAT)
                }

                var selectedChatPartner by remember {
                    mutableStateOf("")
                }

                var viewedProfileUser by remember {
                    mutableStateOf<UserEntity?>(null)
                }

                var chatReturnPage by remember {
                    mutableStateOf(AppPage.DETAIL)
                }

                var authMessage by remember {
                    mutableStateOf("")
                }

                val chatMessages by chatViewModel.messages
                    .collectAsStateWithLifecycle()

                val allChatMessages by remember(currentUser?.email) {
                    communityDao.allMessagesFor(currentUser?.email.orEmpty())
                }.collectAsState(initial = emptyList())

                LaunchedEffect(
                    selectedItem?.id,
                    currentUser?.email,
                    selectedChatPartner
                ) {
                    val itemId = selectedItem?.id
                    val email = currentUser?.email
                    val partner = selectedChatPartner.ifBlank { supportEmail }

                    if (itemId != null && email != null) {
                        chatViewModel.loadConversation(
                            itemId = itemId,
                            currentUser = email,
                            otherUser = partner
                        )
                    }
                }

                val myReviews by remember(currentUser?.email) {
                    communityDao.reviewsFor(currentUser?.email.orEmpty())
                }.collectAsState(initial = emptyList())

                val viewedProfileReviews by remember(viewedProfileUser?.email) {
                    communityDao.reviewsFor(viewedProfileUser?.email.orEmpty())
                }.collectAsState(initial = emptyList())

                LaunchedEffect(Unit) {
                    val id = prefs.getInt("user_id", -1)

                    if (id > 0) {
                        currentUser = withContext(Dispatchers.IO) {
                            userDao.findById(id)
                        }

                        page = if (currentUser == null) {
                            AppPage.LOGIN
                        } else {
                            AppPage.EXPLORE
                        }
                    }
                }

                BackHandler(
                    enabled = page != AppPage.LOGIN &&
                            page != AppPage.EXPLORE
                ) {
                    when (page) {
                        AppPage.PUBLIC_PROFILE -> {
                            if (publicProfileReturnPage == AppPage.DETAIL) {
                                detailReturnPage =
                                    detailReturnPageBeforePublicProfile
                            }
                            page = publicProfileReturnPage
                        }

                        AppPage.DETAIL -> page = detailReturnPage

                        AppPage.CHAT -> {
                            detailReturnPage = detailReturnPageBeforeChat
                            page = chatReturnPage
                        }

                        AppPage.EDIT_PROFILE -> page = AppPage.PROFILE
                        AppPage.REVIEW -> page = AppPage.DETAIL

                        AppPage.CREATE,
                        AppPage.FAVORITES,
                        AppPage.PROFILE,
                        AppPage.CHAT_INBOX -> page = AppPage.EXPLORE

                        AppPage.REGISTER -> page = AppPage.LOGIN
                        else -> page = AppPage.EXPLORE
                    }
                }

                when (page) {

                    AppPage.LOGIN -> AuthScreen(
                        false,
                        authMessage,
                        onSwitch = {
                            authMessage = ""
                            page = AppPage.REGISTER
                        },
                        onSubmit = { email, password, _ ->
                            scope.launch {
                                val user = withContext(Dispatchers.IO) {
                                    userDao.findByEmail(
                                        email.trim().lowercase()
                                    )
                                }

                                if (
                                    user != null &&
                                    verifyPassword(
                                        password,
                                        user.passwordSalt,
                                        user.passwordHash
                                    )
                                ) {
                                    currentUser = user
                                    prefs.edit()
                                        .putInt("user_id", user.id)
                                        .apply()

                                    authMessage = ""
                                    page = AppPage.EXPLORE
                                } else {
                                    authMessage = "Incorrect email or password."
                                }
                            }
                        }
                    )

                    AppPage.REGISTER -> AuthScreen(
                        true,
                        authMessage,
                        onSwitch = {
                            authMessage = ""
                            page = AppPage.LOGIN
                        },
                        onSubmit = { email, password, name ->
                            scope.launch {
                                if (
                                    name.isBlank() ||
                                    !Patterns.EMAIL_ADDRESS.matcher(email).matches() ||
                                    password.length < 6
                                ) {
                                    authMessage =
                                        "Enter a name, a valid email and a password with at least 6 characters."
                                } else {
                                    try {
                                        val salt = newSalt()

                                        val user = UserEntity(
                                            name = name.trim(),
                                            email = email.trim().lowercase(),
                                            passwordHash = hashPassword(password, salt),
                                            passwordSalt = salt
                                        )

                                        val id = withContext(Dispatchers.IO) {
                                            userDao.insert(user).toInt()
                                        }

                                        currentUser = user.copy(id = id)

                                        prefs.edit()
                                            .putInt("user_id", id)
                                            .apply()

                                        authMessage = ""
                                        page = AppPage.EXPLORE

                                    } catch (_: Exception) {
                                        authMessage =
                                            "This email is already registered. Please sign in."
                                    }
                                }
                            }
                        }
                    )

                    AppPage.EXPLORE -> MainShell(
                        items = items,
                        user = currentUser,
                        onCreate = { page = AppPage.CREATE },
                        onFavorites = { page = AppPage.FAVORITES },
                        onProfile = { page = AppPage.PROFILE },
                        onChats = { page = AppPage.CHAT_INBOX },
                        chatUnreadCount = allChatMessages.count {
                            it.receiverEmail.equals(currentUser?.email, true) &&
                                    !it.isRead
                        },
                        onOpenItem = {
                            selectedItem = it
                            page = AppPage.DETAIL
                        },
                        onFavorite = { item, fav ->
                            scope.launch {
                                itemDao.setFavorite(item.id, fav)
                            }
                        },
                        remoteState = remoteState,
                        onRefreshRemote = {
                            exploreViewModel.refreshRemoteSample()
                        }
                    )

                    AppPage.CREATE -> CreateItemScreen(
                        user = currentUser,
                        onBack = { page = AppPage.EXPLORE },
                        onPublish = { item ->
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    itemDao.insertItem(item)
                                }
                                page = AppPage.EXPLORE
                            }
                        }
                    )

                    AppPage.DETAIL -> selectedItem?.let { item ->

                        ItemDetailScreen(
                            item,
                            onBack = { page = detailReturnPage },

                            onViewPosterProfile = {
                                scope.launch {
                                    viewedProfileUser =
                                        withContext(Dispatchers.IO) {
                                            userDao.findByEmail(item.ownerEmail)
                                        }

                                    publicProfileReturnPage = AppPage.DETAIL
                                    detailReturnPageBeforePublicProfile =
                                        detailReturnPage

                                    if (viewedProfileUser != null) {
                                        page = AppPage.PUBLIC_PROFILE
                                    }
                                }
                            },

                            onFavorite = { fav ->
                                scope.launch {
                                    itemDao.setFavorite(item.id, fav)
                                }
                            },

                            onChat = {
                                detailReturnPageBeforeChat = detailReturnPage

                                selectedChatPartner = item.ownerEmail
                                    .trim()
                                    .ifBlank { supportEmail }

                                android.util.Log.d(
                                    "FoundItChat",
                                    "Opening chat with '$selectedChatPartner'"
                                )

                                chatReturnPage = AppPage.DETAIL
                                page = AppPage.CHAT
                            },

                            onReview = { page = AppPage.REVIEW },
                            canReview = currentUser?.email != item.ownerEmail,
                            canContact = currentUser?.email != item.ownerEmail
                        )

                    } ?: run {
                        page = AppPage.EXPLORE
                    }

                    AppPage.FAVORITES -> SimpleItemsPage(
                        "Your saved items",
                        items.filter { it.isFavorite },
                        onBack = { page = AppPage.EXPLORE },
                        onOpen = {
                            selectedItem = it
                            page = AppPage.DETAIL
                        }
                    )

                    AppPage.PROFILE -> ProfileScreen(
                        user = currentUser,
                        ownItems = items.filter {
                            it.ownerEmail == currentUser?.email
                        },
                        reviews = myReviews,
                        onBack = { page = AppPage.EXPLORE },
                        onEdit = { page = AppPage.EDIT_PROFILE },
                        onLogout = {
                            prefs.edit().remove("user_id").apply()
                            currentUser = null
                            selectedChatPartner = ""
                            page = AppPage.LOGIN
                        },
                        onOpen = {
                            selectedItem = it
                            page = AppPage.DETAIL
                        }
                    )

                    AppPage.EDIT_PROFILE -> EditProfileScreen(
                        user = currentUser,
                        onBack = { page = AppPage.PROFILE },
                        onSave = { name, bio, avatarUri ->
                            val user = currentUser

                            if (user != null) {
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        userDao.updateProfile(
                                            user.id,
                                            name,
                                            bio,
                                            avatarUri
                                        )
                                    }

                                    currentUser = user.copy(
                                        name = name,
                                        bio = bio,
                                        avatarUri = avatarUri
                                    )

                                    page = AppPage.PROFILE
                                }
                            }
                        }
                    )

                    AppPage.PUBLIC_PROFILE -> viewedProfileUser?.let { profileUser ->

                        PublicProfileScreen(
                            user = profileUser,
                            listings = items.filter {
                                it.ownerEmail.equals(profileUser.email, true)
                            },
                            reviews = viewedProfileReviews,

                            onBack = {
                                if (publicProfileReturnPage == AppPage.DETAIL) {
                                    detailReturnPage =
                                        detailReturnPageBeforePublicProfile
                                }
                                page = publicProfileReturnPage
                            },

                            onOpenItem = {
                                selectedItem = it
                                detailReturnPage = AppPage.PUBLIC_PROFILE
                                page = AppPage.DETAIL
                            }
                        )

                    } ?: run {
                        page = AppPage.CHAT
                    }

                    AppPage.CHAT_INBOX -> ChatInboxScreen(
                        messages = allChatMessages,
                        items = items,
                        user = currentUser,
                        onBack = { page = AppPage.EXPLORE },
                        onExplore = { page = AppPage.EXPLORE },
                        onSaved = { page = AppPage.FAVORITES },
                        onProfile = { page = AppPage.PROFILE },

                        onOpenConversation = { item, partnerEmail ->
                            selectedItem = item

                            // Never allow an empty chat recipient
                            selectedChatPartner = partnerEmail
                                .trim()
                                .ifBlank { supportEmail }

                            chatReturnPage = AppPage.CHAT_INBOX

                            scope.launch {
                                communityDao.markConversationRead(
                                    item.id,
                                    currentUser?.email.orEmpty(),
                                    selectedChatPartner
                                )
                            }

                            page = AppPage.CHAT
                        }
                    )

                    AppPage.CHAT -> selectedItem?.let { item ->

                        // Final fallback protects the chat screen and send action
                        val chatRecipient = selectedChatPartner
                            .ifBlank { supportEmail }

                        ChatScreen(
                            item = item,
                            user = currentUser,
                            chatPartnerEmail = chatRecipient,
                            messages = chatMessages,

                            onBack = {
                                detailReturnPage = detailReturnPageBeforeChat
                                page = chatReturnPage
                            },

                            onOpenItem = {
                                selectedItem = item
                                detailReturnPage = AppPage.CHAT
                                page = AppPage.DETAIL
                            },

                            onOpenProfile = {
                                scope.launch {
                                    viewedProfileUser =
                                        withContext(Dispatchers.IO) {
                                            userDao.findByEmail(chatRecipient)
                                        }

                                    publicProfileReturnPage = AppPage.CHAT
                                    detailReturnPageBeforePublicProfile =
                                        detailReturnPage

                                    if (viewedProfileUser != null) {
                                        page = AppPage.PUBLIC_PROFILE
                                    }
                                }
                            },

                            onSend = { body ->
                                val sender = currentUser?.email.orEmpty()
                                val receiver = selectedChatPartner
                                    .ifBlank { supportEmail }

                                android.util.Log.d(
                                    "FoundItChat",
                                    "CURRENT USER = '$sender'"
                                )

                                android.util.Log.d(
                                    "FoundItChat",
                                    "CHAT PARTNER = '$receiver'"
                                )

                                chatViewModel.sendMessage(
                                    itemId = item.id,
                                    sender = sender,
                                    receiver = receiver,
                                    body = body
                                )
                            }
                        )

                    } ?: run {
                        page = AppPage.CHAT_INBOX
                    }

                    AppPage.REVIEW -> selectedItem?.let { item ->

                        ReviewScreen(
                            item,
                            currentUser,
                            onBack = { page = AppPage.DETAIL },
                            onSubmit = { rating, comment ->
                                scope.launch {
                                    communityDao.addReview(
                                        ReviewEntity(
                                            itemId = item.id,
                                            reviewerEmail = currentUser?.email.orEmpty(),
                                            reviewedEmail = item.ownerEmail,
                                            rating = rating,
                                            comment = comment
                                        )
                                    )

                                    page = AppPage.PROFILE
                                }
                            }
                        )

                    } ?: run {
                        page = AppPage.EXPLORE
                    }
                }
            }
        }
    }
}


@Composable
private fun AuthScreen(register: Boolean, message: String, onSwitch: () -> Unit,
                       onSubmit: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(Cream).verticalScroll(rememberScrollState()).padding(horizontal = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(70.dp))
        Image(
            painter = painterResource(com.example.foundit.R.drawable.ic_foundit_mark),
            contentDescription = "FoundIt logo",
            modifier = Modifier.size(82.dp)
        )
        Spacer(Modifier.height(22.dp))
        Text("FoundIt", color = Forest, fontSize = 34.sp, fontWeight = FontWeight.Bold)
        Text(if (register) "Create an account and help reunite people with their belongings" else "The things that matter find their way back", color = Olive,
            style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(34.dp))
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), color = Paper, shadowElevation = 3.dp) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(if (register) "Join FoundIt" else "Welcome back", color = Forest, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                if (register) OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Name") }, singleLine = true, shape = RoundedCornerShape(15.dp))
                OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email address") }, singleLine = true, shape = RoundedCornerShape(15.dp))
                OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), label = { Text("Password") }, singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(), shape = RoundedCornerShape(15.dp),
                    trailingIcon = { TextButton(onClick = { showPassword = !showPassword }) { Text(if (showPassword) "Hide" else "Show") } })
                if (message.isNotBlank()) Text(message, color = Terracotta, style = MaterialTheme.typography.bodySmall)
                Button(onClick = { onSubmit(email, password, name) }, Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Forest), shape = RoundedCornerShape(16.dp)) {
                    Text(if (register) "Create account" else "Sign in", color = Cream, fontWeight = FontWeight.SemiBold)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (register) "Already have an account?" else "New to FoundIt?", color = Olive)
                    TextButton(onClick = onSwitch) { Text(if (register) "Sign in" else "Sign up", color = Forest, fontWeight = FontWeight.Bold) }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Your account details are saved on this device.", color = Olive, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun MainShell(
    items: List<FoundItem>,
    user: UserEntity?,
    onCreate: () -> Unit,
    onFavorites: () -> Unit,
    onProfile: () -> Unit,
    onChats: () -> Unit,
    chatUnreadCount: Int,
    onOpenItem: (FoundItem) -> Unit,
    onFavorite: (FoundItem, Boolean) -> Unit,
    remoteState: RemoteFeedState,
    onRefreshRemote: () -> Unit
){
    var tab by remember { mutableStateOf(0) }
    var showMapResults by remember { mutableStateOf(false) }
    var searchLocation by remember { mutableStateOf("") }
    var searchCategories by remember { mutableStateOf(setOf<String>()) }
    var deviceSearchCenter by remember { mutableStateOf<Pair<Double, Double>?>(null) } // latitude, longitude
    Scaffold(containerColor = Cream, bottomBar = {
        NavigationBar(containerColor = Paper, tonalElevation = 4.dp) {
            val labels = listOf("Explore", "Chats", "Saved", "Profile")
            val icons = listOf("⌕", "☏", "♡", "◉")
            labels.forEachIndexed { index, label ->
                NavigationBarItem(selected = tab == index, onClick = {
                    tab = index
                    when (index) {
                        0 -> showMapResults = false
                        1 -> onChats()
                        2 -> onFavorites()
                        3 -> onProfile()
                    }
                }, icon = {
                    if (index == 1 && chatUnreadCount > 0) {
                        BadgedBox(badge = { Badge { Text(if (chatUnreadCount > 9) "9+" else chatUnreadCount.toString()) } }) {
                            Text(icons[index], fontSize = 22.sp)
                        }
                    } else Text(icons[index], fontSize = 22.sp)
                }, label = { Text(label) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Cream, selectedTextColor = Forest,
                        indicatorColor = Forest, unselectedIconColor = Olive, unselectedTextColor = Olive))
            }
        }
    }) { padding ->
        if (showMapResults) {
            ExploreMapScreen(
                items, user, Modifier.padding(padding), onCreate, onOpenItem, onFavorite,
                initialQuery = searchLocation, initialCategories = searchCategories,
                deviceCenter = deviceSearchCenter,
                onBackToExplore = { showMapResults = false }
            )
        } else {
            ExploreLandingScreen(
                remoteState = remoteState,
                onRefreshRemote = onRefreshRemote,
                items = items,
                user = user,
                modifier = Modifier.padding(padding),
                location = searchLocation,
                onLocationChange = { searchLocation = it },
                onDeviceLocation = { location ->
                    deviceSearchCenter = location.latitude to location.longitude
                    searchLocation = ""
                    showMapResults = true
                },
                selectedCategories = searchCategories,
                onCategoryToggle = { category -> searchCategories = if (category in searchCategories) searchCategories - category else searchCategories + category },
                onSearch = { showMapResults = true },
                onCreate = onCreate,
                onOpenItem = onOpenItem,
                onFavorite = onFavorite
            )
        }
    }
}

@Composable
private fun ExploreLandingScreen(

    remoteState: RemoteFeedState,
    onRefreshRemote: () -> Unit,
    items: List<FoundItem>,
    user: UserEntity?,
    modifier: Modifier,
    location: String,
    onLocationChange: (String) -> Unit,
    onDeviceLocation: (Location) -> Unit,
    selectedCategories: Set<String>,
    onCategoryToggle: (String) -> Unit,
    onSearch: () -> Unit,
    onCreate: () -> Unit,
    onOpenItem: (FoundItem) -> Unit,
    onFavorite: (FoundItem, Boolean) -> Unit
) {
    val hasInternet = rememberInternetConnection()
    val citySuggestions = listOf(
        "Lodz", "London", "Madrid", "Barcelona",
        "Warsaw", "Sheffield", "Paris"
    )

    if (!hasInternet) {

        NoConnectionScreen(
            onRetry = onRefreshRemote
        )

    } else {

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Cream)
        ) {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 22.dp,
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {

                // HEADER
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "FoundIt",
                                color = Forest,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "LOST & FOUND, RECONNECTED",
                                color = Olive,
                                fontSize = 10.sp,
                                letterSpacing = 2.sp
                            )
                        }

                        Text(
                            "Hi, ${user?.name?.substringBefore(" ") ?: "there"} ✦",
                            color = Forest,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                // INTRODUCTION
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Text(
                            "Find what you have lost",
                            color = Forest,
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "Search by place, item or category",
                            color = Olive,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // LOCATION SEARCH
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Paper,
                        shadowElevation = 4.dp
                    ) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Where did you lose it?",
                                color = Forest,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = location,
                                onValueChange = onLocationChange,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text("City, neighbourhood or place")
                                },
                                leadingIcon = {
                                    LocationPermissionControl(
                                        onLocation = onDeviceLocation,
                                        onManualFallback = {
                                            // Manual search remains available.
                                        },
                                        onUnavailable = {
                                            // Manual search remains available.
                                        }
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Sage,
                                    unfocusedBorderColor = Sage,
                                    focusedContainerColor = Cream,
                                    unfocusedContainerColor = Cream
                                )
                            )

                            Text(
                                "Popular places",
                                color = Olive,
                                style = MaterialTheme.typography.labelMedium
                            )

                            Text(
                                "Search results are limited to a 10 km radius",
                                color = Olive,
                                style = MaterialTheme.typography.labelSmall
                            )

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                citySuggestions.forEach { city ->
                                    FilterChip(
                                        selected = location.equals(city, true),
                                        onClick = {
                                            onLocationChange(city)
                                        },
                                        label = {
                                            Text(city)
                                        },
                                        shape = CircleShape,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Forest,
                                            selectedLabelColor = Cream,
                                            containerColor = Cream,
                                            labelColor = Forest
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // CATEGORIES
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "What are you looking for?",
                            color = Forest,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "Choose one or more categories to narrow your search",
                            color = Olive,
                            style = MaterialTheme.typography.bodySmall
                        )

                        categories.chunked(2).forEach { rowCategories ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowCategories.forEach { category ->
                                    val active =
                                        category.label in selectedCategories

                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                onCategoryToggle(category.label)
                                            },
                                        shape = RoundedCornerShape(18.dp),
                                        color = if (active) Forest else Paper,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (active) Forest else Sage
                                        ),
                                        shadowElevation = if (active) 2.dp else 0.dp
                                    ) {
                                        Column(
                                            Modifier.padding(
                                                vertical = 15.dp,
                                                horizontal = 12.dp
                                            ),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                category.emoji,
                                                fontSize = 27.sp
                                            )

                                            Spacer(Modifier.height(5.dp))

                                            Text(
                                                category.label,
                                                color = if (active) Cream else Forest,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                if (rowCategories.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCategories.toList()
                                        .forEach(onCategoryToggle)
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = if (selectedCategories.isEmpty()) Sage else Paper
                        ) {
                            Text(
                                "✦  Clear category filters · All categories",
                                modifier = Modifier.padding(14.dp),
                                color = Forest,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // SEARCH BUTTON
                item {
                    Button(
                        onClick = onSearch,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Forest
                        ),
                        shape = RoundedCornerShape(17.dp)
                    ) {
                        Text(
                            "Find items  →",
                            color = Cream,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // RECENT LISTINGS HEADER
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Recent listings",
                                color = Forest,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "Items shared by the community",
                                color = Olive,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        TextButton(onClick = onSearch) {
                            Text("See all", color = Forest)
                        }
                    }
                }

                // RECENT LISTINGS
                items(
                    items.take(5),
                    key = { "landing-${it.id}" }
                ) { item ->
                    ItemMiniCard(
                        item,
                        onClick = {
                            onOpenItem(item)
                        },
                        onFavorite = {
                            onFavorite(item, !item.isFavorite)
                        }
                    )
                }

                // CREATE ITEM
                item {
                    OutlinedButton(
                        onClick = onCreate,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "＋ Post an item",
                            color = Forest
                        )
                    }
                }
            }
        }
    }
}

private fun geocodeWithMapTiler(placeName: String): LngLat? {
    val apiKey = BuildConfig.MAPTILER_API_KEY
    if (apiKey.isBlank()) return null

    val encodedPlace = java.net.URLEncoder.encode(placeName.trim(), "UTF-8")
    val url = URL("https://api.maptiler.com/geocoding/$encodedPlace.json?key=$apiKey&limit=1")
    val connection = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 10_000
        readTimeout = 10_000
        setRequestProperty("User-Agent", "FoundIt-Android/1.0")
    }

    return try {
        if (connection.responseCode !in 200..299) return null
        val body = connection.inputStream.bufferedReader().use { it.readText() }
        val features = JSONObject(body).optJSONArray("features") ?: return null
        if (features.length() == 0) return null
        val center = features.getJSONObject(0).optJSONArray("center") ?: return null
        if (center.length() < 2) return null
        LngLat(center.getDouble(0), center.getDouble(1))
    } catch (_: Exception) {
        null
    } finally {
        connection.disconnect()
    }
}

private data class LocatedFoundItem(
    val item: FoundItem,
    val latitude: Double,
    val longitude: Double
)

private fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
        kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
        kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
    return earthRadius * 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
}

@Composable
private fun ExploreMapScreen(items: List<FoundItem>, user: UserEntity?, modifier: Modifier,
                             onCreate: () -> Unit, onOpenItem: (FoundItem) -> Unit, onFavorite: (FoundItem, Boolean) -> Unit,
                             initialQuery: String = "", initialCategories: Set<String> = emptySet(),
                             deviceCenter: Pair<Double, Double>? = null, onBackToExplore: () -> Unit = {}) {
    val context = LocalContext.current
    var query by remember(initialQuery) { mutableStateOf(initialQuery) }
    var activeCategories by remember(initialCategories) { mutableStateOf(initialCategories) }
    var selected by remember { mutableStateOf<FoundItem?>(null) }
    var message by remember { mutableStateOf("Explore lost and found items") }
    var locatedItems by remember { mutableStateOf<List<LocatedFoundItem>>(emptyList()) }
    var initialCenter by remember { mutableStateOf(deviceCenter?.let { LngLat(it.second, it.first) }) }
    var initialPlaceResolved by remember { mutableStateOf(initialQuery.isBlank()) }
    var hasSearchedLocation by remember { mutableStateOf(initialQuery.isNotBlank() || deviceCenter != null) }

    val controller = remember(context) { MTMapViewController(context) }
    val mapScope = rememberCoroutineScope()
    val mapOptions = remember(initialCenter) {
        MTMapOptions(center = initialCenter ?: LngLat(-0.1278, 51.5074), zoom = 12.0)
    }

    // Resolve the requested city with MapTiler before creating the map, so it never
    // flashes the default London camera when the user searched for another city.
    LaunchedEffect(initialQuery, deviceCenter) {
        if (deviceCenter != null) {
            initialCenter = LngLat(deviceCenter.second, deviceCenter.first)
            hasSearchedLocation = true
            initialPlaceResolved = true
            message = "Showing listings within 10 km of your current location"
        } else if (initialQuery.isNotBlank()) {
            initialPlaceResolved = false
            val place = withContext(Dispatchers.IO) { geocodeWithMapTiler(initialQuery) }
            if (place != null) {
                initialCenter = place
                message = "Showing listings within 10 km of ${initialQuery.trim()}"
            } else {
                message = "We could not find ${initialQuery.trim()}. Try another place."
            }
            initialPlaceResolved = true
        } else {
            initialPlaceResolved = true
        }
    }

    // Listings are geocoded and filtered to a strict 10 km radius around the searched place.
    LaunchedEffect(items) {
        locatedItems = withContext(Dispatchers.IO) {
            items.mapNotNull { item ->
                geocodeWithMapTiler(item.location)?.let { point -> LocatedFoundItem(item, point.lat, point.lng) }
            }
        }
    }

    val radiusItems = remember(locatedItems, initialCenter, hasSearchedLocation) {
        if (!hasSearchedLocation) locatedItems
        else if (initialCenter == null) emptyList()
        else locatedItems.filter { distanceKm(initialCenter!!.lat, initialCenter!!.lng, it.latitude, it.longitude) <= 10.0 }
    }
    val visibleLocated = radiusItems.filter { activeCategories.isEmpty() || it.item.category in activeCategories }
    val visible = visibleLocated.map { it.item }

    fun searchCity() {
        if (query.isBlank()) return
        val placeName = query.trim()
        hasSearchedLocation = true
        message = "Searching for $placeName…"
        mapScope.launch {
            val place = withContext(Dispatchers.IO) { geocodeWithMapTiler(placeName) }
            if (place != null) {
                initialCenter = place
                controller.easeTo(MTCameraOptions(place))
                message = "Showing listings within 10 km of $placeName"
            } else {
                message = "Location not found. Try adding the country."
            }
        }
    }

    Box(modifier.fillMaxSize().background(Cream)) {
        if (initialPlaceResolved) {
            MTMapView(
                referenceStyle = MTMapReferenceStyle.STREETS,
                options = mapOptions,
                controller = controller,
                modifier = Modifier.fillMaxSize(),
                styleVariant = MTMapStyleVariant.LIGHT
            )
        } else {
            Box(Modifier.fillMaxSize().background(Cream), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Forest)
            }
        }

        // Compose annotations are anchored to geographic coordinates and move with the map.
        if (initialPlaceResolved) visibleLocated.forEach { located ->
            key(located.item.id) {
                val item = located.item
                MTCustomAnnotationView(
                    controller = controller,
                    coordinates = LngLat(located.longitude, located.latitude),
                    offset = MTPoint(0.0, -8.0),
                    anchor = Alignment.BottomCenter
                ) {
                    Surface(
                        modifier = Modifier.clickable { selected = item },
                        shape = CircleShape,
                        color = if (item.status == "LOST") Terracotta else Forest,
                        border = androidx.compose.foundation.BorderStroke(2.dp, Paper),
                        shadowElevation = 5.dp
                    ) {
                        Text(
                            categories.find { it.label == item.category }?.emoji ?: "✦",
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                            fontSize = 19.sp,
                            color = Paper
                        )
                    }
                }
            }
        }

        Text("© MapTiler · © OpenStreetMap contributors", modifier = Modifier.align(Alignment.BottomStart)
            .padding(start = 12.dp, bottom = 215.dp).clip(RoundedCornerShape(6.dp))
            .background(Paper.copy(alpha = .90f)).padding(horizontal = 7.dp, vertical = 4.dp),
            color = Olive, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)

        Column(Modifier.align(Alignment.TopCenter).fillMaxWidth().background(Cream.copy(alpha = .97f)).padding(horizontal = 18.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBackToExplore) { Text("‹", color = Forest, fontSize = 27.sp) }
                Column(Modifier.weight(1f)) {
                    Text("FoundIt", color = Forest, fontSize = 29.sp, fontWeight = FontWeight.Bold)
                    Text("LOST & FOUND, RECONNECTED", color = Olive, fontSize = 9.sp, letterSpacing = 1.8.sp)
                }
                Text("Hi, ${user?.name?.substringBefore(" ") ?: "there"}  ✦", color = Forest, style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Paper).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("⌕", color = Forest, fontSize = 25.sp)
                OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.weight(1f),
                    placeholder = { Text("Search for a city or area") }, singleLine = true, shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent, focusedContainerColor = Color.Transparent))
                TextButton(onClick = { searchCity() }) { Text("Go", color = Forest, fontWeight = FontWeight.Bold) }
            }
            Text(message, color = Olive, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 5.dp, start = 4.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                (listOf("All" to "✦") + categories.map { it.label to it.emoji }).forEach { (label, emoji) ->
                    FilterChip(selected = if (label == "All") activeCategories.isEmpty() else label in activeCategories, onClick = { activeCategories = if (label == "All") emptySet() else if (label in activeCategories) activeCategories - label else activeCategories + label },
                        label = { Text("$emoji $label") }, shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Forest, selectedLabelColor = Cream,
                            containerColor = Paper, labelColor = Forest))
                }
            }
        }

        // No hay panel permanente: el mapa ocupa toda la pantalla.
        // The listing sheet appears only after the user taps a map pin.
        if (selected != null) {
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .animateContentSize(),
                shape = RoundedCornerShape(26.dp),
                color = Cream,
                shadowElevation = 16.dp
            ) {
                Column(Modifier.padding(14.dp)) {
                    Box(
                        Modifier.width(38.dp).height(4.dp).clip(CircleShape)
                            .background(Sage).align(Alignment.CenterHorizontally)
                    )
                    Spacer(Modifier.height(9.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Listings in this area", color = Forest,
                                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${visible.size} listings within 10 km · ${selected!!.location}",
                                color = Olive, style = MaterialTheme.typography.labelSmall)
                        }
                        TextButton(onClick = { selected = null }) {
                            Text("Close ✕", color = Forest, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    val sheetItems = listOfNotNull(selected) + visible.filter { it.id != selected!!.id }
                    LazyColumn(
                        Modifier.heightIn(max = 250.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        items(sheetItems.distinctBy { it.id }, key = { it.id }) { item ->
                            ItemMiniCard(
                                item,
                                onClick = { onOpenItem(item) },
                                onFavorite = { onFavorite(item, !item.isFavorite) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemMiniCard(item: FoundItem, onClick: () -> Unit, onFavorite: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Paper).clickable { onClick() }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (item.photoUri != null) AsyncImage(item.photoUri, item.title, Modifier.size(54.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
        else Box(Modifier.size(54.dp).clip(RoundedCornerShape(12.dp)).background(Sage), contentAlignment = Alignment.Center) { Text(categories.find { it.label == item.category }?.emoji ?: "✦", fontSize = 24.sp) }
        Column(Modifier.weight(1f).padding(start = 10.dp)) {
            Text(item.title, color = Forest, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text("${item.category} · ${item.location}", color = Olive, style = MaterialTheme.typography.labelSmall, maxLines = 1)
            Text(if (item.status == "LOST") "🔎 Lost" else "🤝 Found", color = if (item.status == "LOST") Terracotta else ForestLight, style = MaterialTheme.typography.labelSmall)
        }
        TextButton(onClick = onFavorite) { Text(if (item.isFavorite) "♥" else "♡", color = Terracotta, fontSize = 21.sp) }
    }
}

@Composable
private fun CreateItemScreen(user: UserEntity?, onBack: () -> Unit, onPublish: (FoundItemEntity) -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf(user?.email ?: "") }
    var status by remember { mutableStateOf("FOUND") }
    var photo by remember { mutableStateOf<Uri?>(null) }
    var error by remember { mutableStateOf("") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> photo = uri }
    Scaffold(containerColor = Cream, topBar = { TopAppBar(title = { Text("Create a listing", color = Forest, fontWeight = FontWeight.Bold) }, navigationIcon = { TextButton(onClick = onBack) { Text("‹ Back", color = Forest) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
            Text("Help reunite people with their lost belongings.", color = Olive)
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Paper).padding(5.dp)) {
                listOf("FOUND" to "🤝 Found", "LOST" to "🔎 Lost").forEach { (key, label) ->
                    Surface(Modifier.weight(1f).clickable { status = key }, color = if (status == key) Forest else Color.Transparent, shape = RoundedCornerShape(13.dp)) {
                        Text(label, Modifier.padding(vertical = 13.dp), color = if (status == key) Cream else Forest, fontWeight = FontWeight.SemiBold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
            Text("1. Add a photo", color = Forest, fontWeight = FontWeight.Bold)
            Surface(Modifier.fillMaxWidth().height(175.dp).clickable { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, shape = RoundedCornerShape(20.dp), color = Sage) {
                if (photo != null) AsyncImage(photo, "Item photo", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text("📷", fontSize = 36.sp); Text("Tap to add a photo", color = Forest, fontWeight = FontWeight.SemiBold); Text("A clear photo makes it easier to identify", color = Olive, style = MaterialTheme.typography.labelSmall) }
            }
            Text("2. What is the item?", color = Forest, fontWeight = FontWeight.Bold)
            OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Item name *") }, shape = RoundedCornerShape(15.dp), singleLine = true)
            Text("Choose a category", color = Olive, style = MaterialTheme.typography.labelMedium)
            categories.chunked(4).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    row.forEach { cat ->
                        val active = category == cat.label
                        Surface(Modifier.weight(1f).clickable { category = cat.label }, color = if (active) Forest else Paper,
                            shape = RoundedCornerShape(15.dp), border = if (active) null else androidx.compose.foundation.BorderStroke(1.dp, Sage)) {
                            Column(Modifier.padding(vertical = 10.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(cat.emoji, fontSize = 23.sp); Text(cat.label, color = if (active) Cream else Forest, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                        }
                    }
                    repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            Text("3. Where did it happen?", color = Forest, fontWeight = FontWeight.Bold)
            OutlinedTextField(location, { location = it }, Modifier.fillMaxWidth(), label = { Text("City, neighbourhood or place *") }, shape = RoundedCornerShape(15.dp), singleLine = true)
            Text("4. Tell us a little more", color = Forest, fontWeight = FontWeight.Bold)
            OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Description (colour, brand, details…)") }, shape = RoundedCornerShape(15.dp), minLines = 3)
            OutlinedTextField(contact, { contact = it }, Modifier.fillMaxWidth(), label = { Text("Contact email or phone *") }, shape = RoundedCornerShape(15.dp), singleLine = true)
            if (error.isNotBlank()) Text(error, color = Terracotta)
            Button(onClick = {
                if (title.isBlank() || category.isBlank() || location.isBlank() || contact.isBlank()) error = "Please add an item name, category, location and contact details."
                else {
                    val uri = photo?.toString()
                    if (photo != null) try { context.contentResolver.takePersistableUriPermission(photo!!, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) { }
                    onPublish(FoundItemEntity(title = title.trim(), location = location.trim(), category = category,
                        date = "Just now", description = description.ifBlank { "No additional description." }, finderContact = contact.trim(),
                        photoUri = uri, ownerEmail = user?.email.orEmpty(), status = status))
                }
            }, Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = Forest), shape = RoundedCornerShape(16.dp)) { Text("Post on FoundIt", color = Cream, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ItemDetailScreen(item: FoundItem, onBack: () -> Unit, onViewPosterProfile: () -> Unit, onFavorite: (Boolean) -> Unit, onChat: () -> Unit, onReview: () -> Unit, canReview: Boolean, canContact: Boolean) {
    Scaffold(containerColor = Cream, topBar = { TopAppBar(title = { Text("Item details", color = Forest) }, navigationIcon = { TextButton(onClick = onBack) { Text("‹ Back", color = Forest) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            if (item.photoUri != null) AsyncImage(item.photoUri, item.title, Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(24.dp)), contentScale = ContentScale.Crop)
            else Box(Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(24.dp)).background(Sage), contentAlignment = Alignment.Center) { Text(categories.find { it.label == item.category }?.emoji ?: "✦", fontSize = 60.sp) }
            Spacer(Modifier.height(20.dp))
            Text(if (item.status == "LOST") "🔎 ITEM LOST" else "🤝 ITEM FOUND", color = ForestLight, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.title, Modifier.weight(1f), color = Forest, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = { onFavorite(!item.isFavorite) }) { Text(if (item.isFavorite) "♥" else "♡", color = Terracotta, fontSize = 27.sp) }
            }
            Text("${categories.find { it.label == item.category }?.emoji ?: "✦"} ${item.category}    ·    ${item.date}", color = Olive)
            Spacer(Modifier.height(18.dp))
            InfoBlock("📍 Location", item.location)
            InfoBlock("📝 Description", item.description)
            InfoBlock("✉️ Contact", item.finderContact)
            TextButton(onClick = onViewPosterProfile) { Text("View poster profile  ›", color = Forest, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(12.dp))
            if (canContact) Button(onClick = onChat, modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Forest), shape = RoundedCornerShape(16.dp)) {
                Text("Message the poster", color = Cream, fontWeight = FontWeight.Bold)
            }
            if (canReview) OutlinedButton(onClick = onReview, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) {
                Text("Rate your experience", color = Forest)
            }
        }
    }
}

@Composable
private fun InfoBlock(title: String, body: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 9.dp)) { Text(title, color = Forest, fontWeight = FontWeight.Bold); Spacer(Modifier.height(4.dp)); Text(body, color = Ink) }
}

@Composable
private fun SimpleItemsPage(title: String, items: List<FoundItem>, onBack: () -> Unit, onOpen: (FoundItem) -> Unit) {
    Scaffold(containerColor = Cream, topBar = { TopAppBar(title = { Text(title, color = Forest, fontWeight = FontWeight.Bold) }, navigationIcon = { TextButton(onClick = onBack) { Text("‹ Back", color = Forest) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)) }) { padding ->
        if (items.isEmpty()) Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("♡", color = Forest, fontSize = 48.sp); Text("No saved items yet", color = Forest, fontWeight = FontWeight.Bold); Text("Save items you want to keep track of.", color = Olive) } }
        else LazyColumn(Modifier.padding(padding).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { items(items, key = { it.id }) { ItemMiniCard(it, { onOpen(it) }, {}) } }
    }
}

@Composable
private fun ProfileScreen(
    user: UserEntity?,
    ownItems: List<FoundItem>,
    reviews: List<ReviewEntity>,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onLogout: () -> Unit,
    onOpen: (FoundItem) -> Unit
) {
    Scaffold(containerColor = Cream, topBar = {
        TopAppBar(
            title = { Text("My profile", color = Forest, fontWeight = FontWeight.Bold) },
            navigationIcon = { TextButton(onClick = onBack) { Text("‹ Back", color = Forest) } },
            actions = { TextButton(onClick = onEdit) { Text("Edit", color = Forest, fontWeight = FontWeight.Bold) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
        )
    }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(25.dp), color = Forest) {
                Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    ProfileAvatar(user, 92)
                    Spacer(Modifier.height(12.dp))
                    Text(user?.name ?: "FoundIt member", color = Cream, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(user?.email.orEmpty(), color = Sage)
                    if (!user?.bio.isNullOrBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(user?.bio.orEmpty(), color = Cream, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(14.dp))
                    OutlinedButton(onClick = onEdit, colors = ButtonDefaults.outlinedButtonColors(contentColor = Cream), border = androidx.compose.foundation.BorderStroke(1.dp, Sage)) {
                        Text("Personalise profile")
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("Ratings & reviews", color = Forest, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            val averageRating = if (reviews.isEmpty()) 0.0 else reviews.map { it.rating }.average()
            Text(if (reviews.isEmpty()) "No verified reviews yet" else "★ ${"%.1f".format(averageRating)} average · ${reviews.size} reviews received", color = Olive)
            if (reviews.isNotEmpty()) reviews.take(5).forEach { review ->
                Surface(Modifier.fillMaxWidth().padding(vertical = 4.dp), color = Paper, shape = RoundedCornerShape(14.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("★ ${review.rating}/5 · Listing #${review.itemId}", color = Forest, fontWeight = FontWeight.Bold)
                        if (review.comment.isNotBlank()) Text(review.comment, color = Ink)
                    }
                }
            }
            // Clearly labelled sample content, separate from verified reviews.
            Surface(Modifier.fillMaxWidth().padding(top = 10.dp), color = Paper, shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("★★★★★", color = Terracotta, fontSize = 18.sp)
                        Spacer(Modifier.weight(1f))
                        Surface(color = Sage, shape = RoundedCornerShape(20.dp)) {
                            Text("DEMO EXAMPLE", color = Forest, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                    Text("Example review · FoundIt demo profile", color = Forest, fontWeight = FontWeight.Bold)
                    Text("“Friendly, punctual and easy to communicate with. The handover went smoothly!”", color = Ink)
                    Text("Sample text for preview only — not a real user review.", color = Olive, style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("My listings · ${ownItems.size}", color = Forest, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (ownItems.isEmpty()) Text("You have not posted any items yet.", color = Olive, modifier = Modifier.padding(vertical = 18.dp))
            else ownItems.forEach { item -> ItemMiniCard(item, { onOpen(item) }, {}) ; Spacer(Modifier.height(8.dp)) }
            Spacer(Modifier.height(18.dp))
            OutlinedButton(onClick = onLogout, Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Terracotta)) { Text("Sign out") }
            Text("FoundIt · Your lost and found community", color = Olive, style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 25.dp))
        }
    }
}

@Composable
private fun ProfileAvatar(user: UserEntity?, size: Int) {
    if (!user?.avatarUri.isNullOrBlank()) {
        AsyncImage(
            model = user?.avatarUri,
            contentDescription = "${user?.name.orEmpty()} profile photo",
            modifier = Modifier.size(size.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(Modifier.size(size.dp).clip(CircleShape).background(Sage), contentAlignment = Alignment.Center) {
            Text(user?.name?.firstOrNull()?.uppercase() ?: "F", color = Forest, fontSize = (size * 0.38).sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EditProfileScreen(
    user: UserEntity?,
    onBack: () -> Unit,
    onSave: (String, String, String?) -> Unit
) {
    var name by remember(user?.id) { mutableStateOf(user?.name.orEmpty()) }
    var bio by remember(user?.id) { mutableStateOf(user?.bio.orEmpty()) }
    var avatarUri by remember(user?.id) { mutableStateOf(user?.avatarUri) }
    val context = LocalContext.current
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            try { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) { }
            avatarUri = uri.toString()
        }
    }
    Scaffold(containerColor = Cream, topBar = {
        TopAppBar(title = { Text("Personalise profile", color = Forest, fontWeight = FontWeight.Bold) },
            navigationIcon = { TextButton(onClick = onBack) { Text("‹ Back", color = Forest) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream))
    }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            ProfileAvatar(user?.copy(name = name, avatarUri = avatarUri), 112)
            OutlinedButton(onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, shape = RoundedCornerShape(14.dp)) {
                Text("Change profile photo", color = Forest)
            }
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Display name") }, singleLine = true, shape = RoundedCornerShape(15.dp))
            OutlinedTextField(bio, { bio = it }, Modifier.fillMaxWidth(), label = { Text("About me") }, placeholder = { Text("Tell the community a little about yourself…") }, minLines = 4, maxLines = 6, shape = RoundedCornerShape(15.dp))
            Text("Your email address cannot be changed here.", color = Olive, style = MaterialTheme.typography.labelSmall, modifier = Modifier.fillMaxWidth())
            Button(onClick = { if (name.isNotBlank()) onSave(name.trim(), bio.trim(), avatarUri) }, Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = Forest), shape = RoundedCornerShape(16.dp), enabled = name.isNotBlank()) {
                Text("Save changes", color = Cream, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PublicProfileScreen(
    user: UserEntity,
    listings: List<FoundItem>,
    reviews: List<ReviewEntity>,
    onBack: () -> Unit,
    onOpenItem: (FoundItem) -> Unit
) {
    Scaffold(containerColor = Cream, topBar = {
        TopAppBar(title = { Text("Member profile", color = Forest, fontWeight = FontWeight.Bold) },
            navigationIcon = { TextButton(onClick = onBack) { Text("‹ Back to chat", color = Forest) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream))
    }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Surface(Modifier.fillMaxWidth(), color = Forest, shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    ProfileAvatar(user, 88)
                    Spacer(Modifier.height(10.dp))
                    Text(user.name, color = Cream, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    if (user.bio.isNotBlank()) Text(user.bio, color = Sage, modifier = Modifier.padding(top = 8.dp))
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("Ratings & reviews", color = Forest, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (reviews.isEmpty()) Text("No reviews yet", color = Olive, modifier = Modifier.padding(vertical = 10.dp))
            else {
                Text("★ ${"%.1f".format(reviews.map { it.rating }.average())} · ${reviews.size} reviews", color = Olive)
                reviews.take(8).forEach { review ->
                    Surface(Modifier.fillMaxWidth().padding(top = 8.dp), color = Paper, shape = RoundedCornerShape(14.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("★ ${review.rating}/5", color = Forest, fontWeight = FontWeight.Bold)
                            if (review.comment.isNotBlank()) Text(review.comment, color = Ink)
                        }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("Listings · ${listings.size}", color = Forest, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (listings.isEmpty()) Text("No active listings", color = Olive, modifier = Modifier.padding(vertical = 12.dp))
            else listings.forEach { item -> ItemMiniCard(item, { onOpenItem(item) }, {}) ; Spacer(Modifier.height(8.dp)) }
        }
    }
}


private data class ChatConversation(
    val item: FoundItem,
    val partnerEmail: String,
    val latestMessage: ChatMessageEntity,
    val unreadCount: Int
)

@Composable
private fun ChatInboxScreen(
    messages: List<ChatMessageEntity>,
    items: List<FoundItem>,
    user: UserEntity?,
    onBack: () -> Unit,
    onExplore: () -> Unit,
    onSaved: () -> Unit,
    onProfile: () -> Unit,
    onOpenConversation: (FoundItem, String) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val userEmail = user?.email.orEmpty()
    val conversations = remember(messages, items, userEmail, search) {
        messages.groupBy { message ->
            val partner = if (message.senderEmail.equals(userEmail, true)) message.receiverEmail else message.senderEmail
            message.itemId to partner
        }.mapNotNull { (key, threadMessages) ->
            val item = items.firstOrNull { it.id == key.first } ?: return@mapNotNull null
            val latest = threadMessages.maxByOrNull { it.createdAt } ?: return@mapNotNull null
            val unread = threadMessages.count { it.receiverEmail.equals(userEmail, true) && !it.isRead }
            ChatConversation(item, key.second, latest, unread)
        }.filter { conversation ->
            val query = search.trim()
            query.isBlank() || conversation.partnerEmail.contains(query, true) ||
                conversation.item.title.contains(query, true) || conversation.latestMessage.body.contains(query, true)
        }.sortedByDescending { it.latestMessage.createdAt }
    }
    val totalUnread = conversations.sumOf { it.unreadCount }

    Scaffold(
        containerColor = Cream,
        bottomBar = {
            NavigationBar(containerColor = Paper, tonalElevation = 4.dp) {
                val tabs = listOf("Explore", "Chats", "Saved", "Profile")
                val glyphs = listOf("⌕", "☏", "♡", "◉")
                tabs.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = index == 1,
                        onClick = when (index) {
                            0 -> onExplore
                            1 -> ({ })
                            2 -> onSaved
                            else -> onProfile
                        },
                        icon = { Text(glyphs[index], fontSize = 22.sp) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Cream, selectedTextColor = Forest,
                            indicatorColor = Forest, unselectedIconColor = Olive, unselectedTextColor = Olive
                        )
                    )
                }
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Chats", color = Forest, fontWeight = FontWeight.Bold)
                        Text(
                            if (conversations.isEmpty()) "Your conversations" else "${conversations.size} conversations${if (totalUnread > 0) " · $totalUnread unread" else ""}",
                            color = Olive, style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                navigationIcon = { TextButton(onClick = onBack) { Text("‹", color = Forest, fontSize = 28.sp) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp)) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 14.dp),
                placeholder = { Text("Search conversations") },
                leadingIcon = { Text("⌕", color = Forest, fontSize = 23.sp) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Forest, unfocusedBorderColor = Sage,
                    focusedContainerColor = Paper, unfocusedContainerColor = Paper
                )
            )
            if (conversations.isEmpty()) {
                Column(
                    Modifier.fillMaxSize().padding(bottom = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(Modifier.size(82.dp).clip(CircleShape).background(Sage), contentAlignment = Alignment.Center) {
                        Text("☏", color = Forest, fontSize = 38.sp)
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(if (search.isBlank()) "No conversations yet" else "No matching chats", color = Forest, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (search.isBlank()) "When you message someone about an item, your chat will appear here."
                        else "Try another name, item or message.",
                        color = Olive, style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(conversations, key = { "${it.item.id}_${it.partnerEmail}" }) { conversation ->
                        ChatConversationRow(conversation) {
                            onOpenConversation(conversation.item, conversation.partnerEmail)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatConversationRow(conversation: ChatConversation, onClick: () -> Unit) {
    val partnerName = conversation.partnerEmail.substringBefore("@").replace('.', ' ')
        .split(' ').joinToString(" ") { part -> part.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }
    val time = remember(conversation.latestMessage.createdAt) {
        val now = System.currentTimeMillis()
        val sameDay = now - conversation.latestMessage.createdAt < 24L * 60L * 60L * 1000L
        SimpleDateFormat(if (sameDay) "HH:mm" else "dd MMM", Locale.getDefault()).format(Date(conversation.latestMessage.createdAt))
    }
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick)
            .background(if (conversation.unreadCount > 0) Color(0xFFF0F4EC) else Paper)
            .padding(horizontal = 12.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!conversation.item.photoUri.isNullOrBlank()) {
            AsyncImage(
                model = conversation.item.photoUri,
                contentDescription = conversation.item.title,
                modifier = Modifier.size(58.dp).clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(Modifier.size(58.dp).clip(RoundedCornerShape(16.dp)).background(Sage), contentAlignment = Alignment.Center) {
                val emoji = categories.firstOrNull { it.label.equals(conversation.item.category, true) }?.emoji ?: "📦"
                Text(emoji, fontSize = 27.sp)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(partnerName.ifBlank { "FoundIt member" }, color = Forest, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1)
                Text(time, color = Olive, style = MaterialTheme.typography.labelSmall)
            }
            Text(conversation.item.title, color = Ink, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(
                conversation.latestMessage.body,
                color = if (conversation.unreadCount > 0) Ink else Olive,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (conversation.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1
            )
        }
        if (conversation.unreadCount > 0) {
            Spacer(Modifier.width(8.dp))
            Box(Modifier.size(23.dp).clip(CircleShape).background(Forest), contentAlignment = Alignment.Center) {
                Text(if (conversation.unreadCount > 9) "9+" else conversation.unreadCount.toString(), color = Cream, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
    HorizontalDivider(color = Sage.copy(alpha = 0.7f), thickness = 0.7.dp)
}

@Composable
private fun ChatScreen(
    item: FoundItem,
    user: UserEntity?,
    chatPartnerEmail: String,
    messages: List<ChatMessageEntity>,
    onBack: () -> Unit,
    onOpenItem: () -> Unit,
    onOpenProfile: () -> Unit,
    onSend: (String) -> Unit
) {
    var draft by remember { mutableStateOf("") }
    val partnerName = chatPartnerEmail.substringBefore("@").replace('.', ' ')
        .split(' ').joinToString(" ") { it.replaceFirstChar { ch -> ch.titlecase(Locale.getDefault()) } }
    Scaffold(containerColor = Cream, topBar = {
        TopAppBar(
            title = {
                Column(Modifier.clickable(onClick = onOpenItem)) {
                    Text(item.title, color = Forest, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text("Open listing · tap to view", color = Olive, style = MaterialTheme.typography.labelSmall)
                }
            },
            navigationIcon = { TextButton(onClick = onBack) { Text("Back", color = Forest) } },
            actions = { TextButton(onClick = onOpenProfile) { Text("Profile", color = Forest, fontWeight = FontWeight.Bold) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
        )
    }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(14.dp)) {
            Surface(Modifier.fillMaxWidth().clickable(onClick = onOpenProfile), color = Paper, shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(CircleShape).background(Sage), contentAlignment = Alignment.Center) {
                        Text(partnerName.firstOrNull()?.uppercase() ?: "F", color = Forest, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(partnerName.ifBlank { "FoundIt member" }, color = Forest, fontWeight = FontWeight.Bold)
                        Text("View member profile", color = Olive, style = MaterialTheme.typography.labelSmall)
                    }
                    Text("›", color = Forest, fontSize = 24.sp)
                }
            }
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(messages, key = { it.id }) { message ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.senderEmail == user?.email) Arrangement.End else Arrangement.Start) {
                        Surface(color = if (message.senderEmail == user?.email) Forest else Paper, shape = RoundedCornerShape(16.dp)) {
                            Text(message.body, Modifier.padding(12.dp), color = if (message.senderEmail == user?.email) Cream else Ink)
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(draft, { draft = it }, Modifier.weight(1f), placeholder = { Text("Write a message…") }, shape = RoundedCornerShape(16.dp), maxLines = 3)
                Spacer(Modifier.width(8.dp))
                Button(onClick = { if (draft.isNotBlank()) { onSend(draft.trim()); draft = "" } }, colors = ButtonDefaults.buttonColors(containerColor = Forest)) { Text("Send", color = Cream) }
            }
            Text("Messages are currently stored on this device. Cross-device chat needs a shared backend.", color = Olive, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 6.dp))
        }
    }
}


@Composable
private fun ReviewScreen(item: FoundItem, user: UserEntity?, onBack: () -> Unit, onSubmit: (Int, String) -> Unit) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    Scaffold(containerColor = Cream, topBar = {
        TopAppBar(title = { Text("Rate your experience", color = Forest, fontWeight = FontWeight.Bold) },
            navigationIcon = { TextButton(onClick = onBack) { Text("Back", color = Forest) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream))
    }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("How did the handover go?", color = Forest, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Rate communication, punctuality and how smoothly you met to return the item.", color = Olive)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { value -> TextButton(onClick = { rating = value }) { Text(if (value <= rating) "★" else "☆", color = Terracotta, fontSize = 34.sp) } }
            }
            OutlinedTextField(comment, { comment = it }, Modifier.fillMaxWidth(), label = { Text("Share details (optional)") }, minLines = 4, shape = RoundedCornerShape(16.dp))
            Button(onClick = { onSubmit(rating, comment.trim()) }, Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = Forest), shape = RoundedCornerShape(16.dp)) {
                Text("Submit review", color = Cream, fontWeight = FontWeight.Bold)
            }
            Text("Reviews are saved locally in this version.", color = Olive, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun newSalt(): String {
    val bytes = ByteArray(16); SecureRandom().nextBytes(bytes)
    return android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
}

private fun hashPassword(password: String, salt: String): String {
    val spec = PBEKeySpec(password.toCharArray(), android.util.Base64.decode(salt, android.util.Base64.NO_WRAP), 120_000, 256)
    return try { android.util.Base64.encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded, android.util.Base64.NO_WRAP) }
    finally { spec.clearPassword() }
}

private fun verifyPassword(password: String, salt: String, expected: String): Boolean =
    java.security.MessageDigest.isEqual(hashPassword(password, salt).toByteArray(), expected.toByteArray())

@Composable
private fun RemoteFeedPanel(
    state: RemoteFeedState,
    onRefresh: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(18.dp),
        color = Paper,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Network connection test",
                color = Forest,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Demo service: JSONPlaceholder (not a FoundIt! backend)",
                color = Olive,
                fontSize = 11.sp
            )

            Button(
                onClick = onRefresh,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Forest
                )
            ) {
                Text("Load sample feed", color = Cream)
            }

            when (state) {
                RemoteFeedState.Idle -> {
                    Text("No request made yet", color = Olive)
                }

                RemoteFeedState.Loading -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Forest
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Loading sample posts...")
                    }
                }

                is RemoteFeedState.Success -> {
                    Text(
                        "Success: ${state.posts.size} sample posts received",
                        color = Forest,
                        fontWeight = FontWeight.SemiBold
                    )

                    state.posts.take(3).forEach { post ->
                        Text(
                            post.title,
                            color = Forest,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            post.body,
                            color = Olive,
                            fontSize = 12.sp,
                            maxLines = 3
                        )
                    }
                }

                RemoteFeedState.Empty -> {
                    Text(
                        "The service returned no posts.",
                        color = Olive
                    )
                }

                is RemoteFeedState.Error -> {
                    Text(
                        "Network error: ${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )

                    Button(
                        onClick = onRefresh,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Forest
                        )
                    ) {
                        Text("Retry", color = Cream)
                    }
                }
            }
        }
    }
}

@Composable
private fun NoConnectionScreen(
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                "⌁",
                fontSize = 64.sp,
                color = Olive
            )

            Text(
                "No connection",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Forest
            )

            Text(
                "We couldn't connect to the internet. " +
                        "Check your connection and try again.",
                color = Olive,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Forest
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Try again", color = Cream)
            }
        }
    }
}


@Composable
private fun rememberInternetConnection(): Boolean {
    val context = LocalContext.current

    var hasInternet by remember {
        mutableStateOf(false)
    }

    DisposableEffect(context) {
        val manager = context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        fun checkConnection() {
            val network = manager.activeNetwork
            val capabilities =
                manager.getNetworkCapabilities(network)

            hasInternet =
                capabilities?.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                ) == true &&
                        capabilities.hasCapability(
                            NetworkCapabilities.NET_CAPABILITY_VALIDATED
                        )
        }

        val callback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                checkConnection()
            }

            override fun onLost(network: Network) {
                checkConnection()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities
            ) {
                hasInternet =
                    capabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_INTERNET
                    ) &&
                            capabilities.hasCapability(
                                NetworkCapabilities.NET_CAPABILITY_VALIDATED
                            )
            }
        }

        checkConnection()

        manager.registerDefaultNetworkCallback(callback)

        onDispose {
            manager.unregisterNetworkCallback(callback)
        }
    }

    return hasInternet
}

