@file:OptIn(ExperimentalResourceApi::class)

package com.kurban.calory.features.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import calory.composeapp.generated.resources.Res
import calory.composeapp.generated.resources.add
import calory.composeapp.generated.resources.add_to_diary
import calory.composeapp.generated.resources.app_title
import calory.composeapp.generated.resources.dismiss
import calory.composeapp.generated.resources.empty_list
import calory.composeapp.generated.resources.food_macros_100
import calory.composeapp.generated.resources.food_macros_entry
import calory.composeapp.generated.resources.macro_carb_short
import calory.composeapp.generated.resources.macro_fat_short
import calory.composeapp.generated.resources.macro_protein_short
import calory.composeapp.generated.resources.meals_today
import calory.composeapp.generated.resources.pick_from_search
import calory.composeapp.generated.resources.portion_grams
import calory.composeapp.generated.resources.remove
import calory.composeapp.generated.resources.search_foods
import calory.composeapp.generated.resources.select
import calory.composeapp.generated.resources.subtitle
import calory.composeapp.generated.resources.today
import calory.composeapp.generated.resources.total_consumed
import calory.composeapp.generated.resources.grams_total
import com.kurban.calory.core.theme.CaloryTheme
import com.kurban.calory.features.main.domain.model.Food
import com.kurban.calory.features.main.ui.model.UITrackedFood
import com.kurban.calory.features.main.ui.model.MainUiState
import com.kurban.calory.features.main.ui.MainViewModel
import com.kurban.calory.features.main.ui.model.MainEffect
import com.kurban.calory.features.main.ui.model.MainIntent
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<MainViewModel>()
    val state by viewModel.uiState.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MainEffect.Error -> errorMessage = effect.message
            }
        }
    }

    MainContent(
        state = state,
        errorMessage = errorMessage,
        onQueryChanged = { viewModel.dispatch(MainIntent.QueryChanged(it)) },
        onSelectFood = { viewModel.dispatch(MainIntent.FoodSelected(it)) },
        onGramsChanged = { viewModel.dispatch(MainIntent.GramsChanged(it)) },
        onAddFood = { viewModel.dispatch(MainIntent.AddSelectedFood) },
        onRemoveEntry = { viewModel.dispatch(MainIntent.RemoveEntry(it)) },
        onErrorDismiss = {
            viewModel.dispatch(MainIntent.ClearError)
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
private fun MainContent(
    state: MainUiState,
    errorMessage: String?,
    modifier: Modifier = Modifier,
    onQueryChanged: (String) -> Unit,
    onSelectFood: (Food) -> Unit,
    onGramsChanged: (String) -> Unit,
    onAddFood: () -> Unit,
    onRemoveEntry: (Long) -> Unit,
    onErrorDismiss: () -> Unit
) {
    CaloryTheme {
        val colors = MaterialTheme.colorScheme
        val surfaceGradient = remember {
            Brush.linearGradient(
                listOf(
                    colors.background,
                    colors.surfaceVariant
                )
            )
        }
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = Color.Transparent
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(surfaceGradient)
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Header()
                    SummaryCard(
                        calories = state.totalCalories,
                        proteins = state.totalProteins,
                        fats = state.totalFats,
                        carbs = state.totalCarbs
                    )
                    SearchSection(
                        query = state.query,
                        results = state.searchResults,
                        isSearching = state.isSearching,
                        onQueryChanged = onQueryChanged,
                        onSelect = onSelectFood
                    )
                    SelectionSection(
                        selected = state.selectedFood,
                        gramsInput = state.gramsInput,
                        onGramsChanged = onGramsChanged,
                        onAdd = onAddFood
                    )
                    ConsumptionList(
                        items = state.tracked,
                        onRemove = onRemoveEntry
                    )
                }

                AnimatedVisibility(
                    visible = errorMessage != null,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    enter = fadeIn(animationSpec = tween(250, easing = FastOutSlowInEasing)),
                    exit = fadeOut(animationSpec = tween(250, easing = FastOutSlowInEasing))
                ) {
                    ErrorCard(message = errorMessage.orEmpty(), onDismiss = onErrorDismiss)
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.app_title),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(Res.string.subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun SummaryCard(
    calories: Double,
    proteins: Double,
    fats: Double,
    carbs: Double,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(Res.string.today),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${calories.roundToOne()} ккал",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(Res.string.total_consumed),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MacroPill(stringResource(Res.string.macro_protein_short), proteins, MaterialTheme.colorScheme.secondary)
                    MacroPill(stringResource(Res.string.macro_fat_short), fats, MaterialTheme.colorScheme.tertiary)
                    MacroPill(stringResource(Res.string.macro_carb_short), carbs, MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun MacroPill(label: String, value: Double, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = color)
        Text(
            text = value.roundToOne(),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun SearchSection(
    query: String,
    results: List<Food>,
    isSearching: Boolean,
    onQueryChanged: (String) -> Unit,
    onSelect: (Food) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .fillMaxWidth(),
            label = { Text(stringResource(Res.string.search_foods)) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
            singleLine = true
        )

        val itemsToShow = if (isSearching && results.isEmpty()) emptyList() else results.take(6)
        if (itemsToShow.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(itemsToShow) { food ->
                    FoodRow(food = food, onSelect = { onSelect(food) })
                }
            }
        }
    }
}

@Composable
private fun FoodRow(
    food: Food,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(
                        Res.string.food_macros_100,
                        food.calories.roundToOne(),
                        food.proteins.roundToOne(),
                        food.fats.roundToOne(),
                        food.carbs.roundToOne()
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            TextButton(onClick = onSelect) {
                Text(stringResource(Res.string.select))
            }
        }
    }
}

@Composable
private fun SelectionSection(
    selected: Food?,
    gramsInput: String,
    onGramsChanged: (String) -> Unit,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(Res.string.add_to_diary),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (selected != null) {
                Text(
                    text = selected.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = stringResource(Res.string.pick_from_search),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = gramsInput,
                    onValueChange = onGramsChanged,
                    label = { Text(stringResource(Res.string.portion_grams)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(
                    onClick = onAdd,
                    enabled = selected != null && gramsInput.isNotBlank(),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text(stringResource(Res.string.add))
                }
            }
        }
    }
}

@Composable
private fun ConsumptionList(
    items: List<UITrackedFood>,
    onRemove: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.meals_today),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(Res.string.grams_total, items.sumOf { it.grams }),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (items.isEmpty()) {
                Text(
                    text = stringResource(Res.string.empty_list),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.entryId }) { item ->
                        ConsumptionRow(item, onRemove)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsumptionRow(
    item: UITrackedFood,
    onRemove: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${item.grams} г",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(
                    Res.string.food_macros_entry,
                    item.calories.roundToOne(),
                    item.proteins.roundToOne(),
                    item.fats.roundToOne(),
                    item.carbs.roundToOne()
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = { onRemove(item.entryId) }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(Res.string.remove),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.errorContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.dismiss), color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}

private fun Double.roundToOne(): String {
    val rounded = (this * 10.0).roundToInt().toDouble() / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val previewState = remember {
        MainUiState(
            query = "apple",
            gramsInput = "150",
            searchResults = listOf(
                Food(1, 0, "Banana", 89.0, 1.1, 0.3, 23.0),
                Food(2, 0, "Chicken breast", 165.0, 31.0, 3.6, 0.0),
                Food(3, 0, "Oats", 379.0, 17.0, 7.0, 67.0)
            ),
            tracked = listOf(
                UITrackedFood(1, 1, "Banana", 120, 106.8, 1.3, 0.4, 27.6),
                UITrackedFood(2, 2, "Chicken breast", 200, 330.0, 62.0, 7.2, 0.0)
            ),
            totalCalories = 436.8,
            totalProteins = 63.3,
            totalFats = 7.6,
            totalCarbs = 27.6
        )
    }

    MainContent(
        state = previewState,
        errorMessage = null,
        onQueryChanged = {},
        onSelectFood = {},
        onGramsChanged = {},
        onAddFood = {},
        onRemoveEntry = {},
        onErrorDismiss = {}
    )
}
