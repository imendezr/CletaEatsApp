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
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cletaeatsapp.data.model.Pedido
import com.example.cletaeatsapp.utils.format
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdenCard(
    modifier: Modifier = Modifier,
    pedido: Pedido,
    onMarkDelivered: () -> Unit = {},
    onMarkInTransit: () -> Unit = {},
    onMarkSuspended: (() -> Unit)? = null,
    onClick: () -> Unit = {},
    isRepartidor: Boolean = false,
    isRestaurant: Boolean = false
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
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Restaurante: ${pedido.nombreRestaurante ?: "Desconocido"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Combos:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                pedido.combos.forEach { combo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Combo ${combo.numero}: ${combo.nombre}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "₡${combo.precio.format(2)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Distancia: ${pedido.distancia} km",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Subtotal: ₡${pedido.precio.format(2)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Transporte: ₡${pedido.costoTransporte.format(2)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "IVA (13%): ₡${pedido.iva.format(2)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Total: ₡${pedido.total.format(2)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
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
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                ) {
                    Text("Marcar como Entregado")
                }
            } else if (isRestaurant && pedido.estado == "en preparación") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onMarkInTransit,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Marcar como En Camino")
                    }
                    OutlinedButton(
                        onClick = { onMarkSuspended?.invoke() },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Suspender")
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

            "en camino" -> Icon(
                imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
                contentDescription = "En camino",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(24.dp)
            )

            "entregado" -> Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Entregado",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            "suspendido" -> Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Suspendido",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )

            else -> Spacer(modifier = Modifier.size(24.dp))
        }
    }
}
