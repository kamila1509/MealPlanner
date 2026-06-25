package com.kam666.mealplanner.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kam666.mealplanner.presentation.theme.CoralPrimary
import com.kam666.mealplanner.presentation.theme.CoralPrimaryDark
import com.kam666.mealplanner.presentation.theme.CoralSecondary

@Composable
fun LanguageToggle(
    language: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(999.dp))
            .padding(3.dp)
            .shadow(1.dp, RoundedCornerShape(999.dp), spotColor = Color(0x142F2018)),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        LangChip("ES", selected = language == AppLanguage.ES) { onSelect(AppLanguage.ES) }
        LangChip("EN", selected = language == AppLanguage.EN) { onSelect(AppLanguage.EN) }
    }
}

@Composable
private fun LangChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ScreenHeader(
    brand: String,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    brand.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.4.sp
                )
                Text(
                    title,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            trailing?.invoke()
        }
        subtitle?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun FilterChipRow(
    chips: List<Pair<String, String>>,
    selectedKey: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chips.forEach { (key, label) ->
            val selected = selectedKey == key || (selectedKey == null && key == "all")
            FilterChip(
                selected = selected,
                onClick = { onSelect(if (key == "all") null else key) },
                label = {
                    Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = null,
                shape = RoundedCornerShape(999.dp)
            )
        }
    }
}

@Composable
fun MealPlannerBottomBar(
    selected: BottomTab,
    onSelect: (BottomTab) -> Unit,
    recipesLabel: String,
    planLabel: String,
    shoppingLabel: String,
    aiLabel: String = "IA",
    profileLabel: String = "Perfil"
) {
    Column(Modifier.navigationBarsPadding()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            BottomTabItem(BottomTab.Recipes, Icons.Filled.MenuBook, recipesLabel, selected, onSelect)
            BottomTabItem(BottomTab.WeeklyPlan, Icons.Filled.CalendarMonth, planLabel, selected, onSelect)
            BottomTabItem(BottomTab.Shopping, Icons.Filled.ShoppingCart, shoppingLabel, selected, onSelect)
            BottomTabItem(BottomTab.AiSuggestions, Icons.Filled.AutoAwesome, aiLabel, selected, onSelect)
            BottomTabItem(BottomTab.Profile, Icons.Filled.AccountCircle, profileLabel, selected, onSelect)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0x522F2018))
            )
        }
    }
}

enum class BottomTab { Recipes, WeeklyPlan, Shopping, AiSuggestions, Profile }

@Composable
private fun RowScope.BottomTabItem(
    tab: BottomTab,
    icon: ImageVector,
    label: String,
    selected: BottomTab,
    onSelect: (BottomTab) -> Unit
) {
    val isSelected = tab == selected
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable { onSelect(tab) }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(58.dp)
                .height(30.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (isSelected) CoralPrimaryDark else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PrimaryFab(onClick: () -> Unit, contentDescription: String) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .size(62.dp)
            .shadow(10.dp, RoundedCornerShape(20.dp), spotColor = Color(0x66E2603B)),
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
    ) {
        Icon(Icons.Filled.Add, contentDescription = contentDescription, modifier = Modifier.size(28.dp))
    }
}

@Composable
fun ShoppingBanner(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(CoralPrimary, CoralSecondary)))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("🛒", fontSize = 30.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Text(subtitle, color = Color.White.copy(alpha = 0.92f), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
        Text("›", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CircleIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(42.dp)
            .shadow(2.dp, CircleShape, spotColor = Color(0x142F2018))
            .background(Color.White.copy(alpha = 0.9f), CircleShape),
    ) {
        content()
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        modifier = modifier.padding(bottom = 10.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.dp, Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        content()
    }
}

@Composable
fun RecipeGridCard(
    name: String,
    emoji: String,
    tint: Color,
    timeLabel: String?,
    categoryLabel: String,
    categoryEmoji: String,
    ingredientCountLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .shadow(1.dp, RoundedCornerShape(22.dp), spotColor = Color(0x0A2F2018))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
                .background(tint),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 48.sp)
            timeLabel?.let {
                Text(
                    it,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color(0x9E1C1714), RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp)) {
            Text(
                name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "$categoryEmoji $categoryLabel · $ingredientCountLabel",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
