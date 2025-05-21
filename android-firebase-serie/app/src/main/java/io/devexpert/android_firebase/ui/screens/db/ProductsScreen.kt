package io.devexpert.android_firebase.ui.screens.db

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.devexpert.android_firebase.model.Product
import io.devexpert.android_firebase.utils.CartManager
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import io.devexpert.android_firebase.R
import androidx.compose.runtime.collectAsState
import io.devexpert.android_firebase.ui.screens.BottomNavScreen
import io.devexpert.android_firebase.utils.RealtimeManager
import androidx.compose.runtime.getValue
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun ProductsScreen(realtime: RealtimeManager, navigation: NavController) {
    val products by realtime.getProductsFlow().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productos") },
                actions = {
                    IconButton(
                        onClick = { navigation.navigate(BottomNavScreen.Cart.route) }
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Ver carrito")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding)
        ) {
            items(products, key = { it.id!! }) { product ->
                ProductItem(product)
            }
        }
    }
}

@Composable
fun ProductItem(product: Product) {
    val name  = product.name.orEmpty()
    val price = product.price ?: 0.0
    val context = LocalContext.current

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(product.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = product.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.profile)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Precio: \$${"%.2f".format(price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = {
                CartManager.addToCart(product)
                Toast
                    .makeText(context, "✔️ \"$name\" agregado al carrito", Toast.LENGTH_SHORT)
                    .show()
            }) {
                Icon(
                    imageVector   = Icons.Default.AddShoppingCart,
                    contentDescription = "Agregar al carrito"
                )
            }
        }
    }
}
