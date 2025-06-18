package com.example.cletaeatsapp.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cletaeatsapp.data.model.Pedido
import java.util.Locale

@Composable
fun OrdenCard(
    modifier: Modifier = Modifier,
    pedido: Pedido,
    onMarkDelivered: () -> Unit = {},
    onClick: () -> Unit = {},
    isRepartidor: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pedido #${pedido.id.takeLast(8)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                StatusIndicator(estado = pedido.estado)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Restaurante: ${pedido.restaurantName ?: "Desconocido"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Combos: ${pedido.combos.joinToString()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Subtotal: ₡${pedido.precio}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Transporte: ₡${pedido.costoTransporte}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "IVA (13%): ₡${pedido.iva}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Total: ₡${pedido.total}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Estado: ${
                            pedido.estado.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                            }
                        }",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Realizado: ${pedido.horaRealizado}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    pedido.horaEntregado?.let {
                        Text(
                            text = "Entregado: $it",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                if (isRepartidor && pedido.estado != "entregado") {
                    OutlinedButton(
                        onClick = onMarkDelivered,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text("Entregado")
                    }
                }
            }
        }
    }
}

@Composable
fun StatusIndicator(estado: String) {
    Crossfade(targetState = estado, label = "status_indicator") { state ->
        when (state) {
            "en preparación" -> Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = "En preparación",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )

            "entregado" -> Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Entregado",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            else -> Spacer(modifier = Modifier.size(24.dp))
        }
    }
}
