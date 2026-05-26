package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Playful custom colors for Neo-Brutalist accents
object BrutalistColors {
    val Yellow = Color(0xFFFFD900)
    val Orange = Color(0xFFFF6D00)
    val Pink = Color(0xFFFF4081)
    val Cyan = Color(0xFF00E5FF)
    val Purple = Color(0xFF9D00FF)
    val Lime = Color(0xFF00E676)
    val Black = Color(0xFF121212)
    val OffWhite = Color(0xFFF9F9FA)
    val Grey = Color(0xFFECEFF1)
}

@Composable
fun BrutalistButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = BrutalistColors.Yellow,
    shadowOffset: Dp = 4.dp,
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier
            .padding(bottom = shadowOffset, end = shadowOffset)
            .background(BrutalistColors.Black, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .offset(x = (-shadowOffset), y = (-shadowOffset))
                .background(backgroundColor, RoundedCornerShape(8.dp))
                .border(2.5.dp, BrutalistColors.Black, RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

@Composable
fun BrutalistIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = BrutalistColors.Cyan,
    contentDescription: String = "action",
    shadowOffset: Dp = 4.dp,
    size: Dp = 44.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(BrutalistColors.Black, RoundedCornerShape(8.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = -shadowOffset, y = -shadowOffset)
                .background(backgroundColor, RoundedCornerShape(8.dp))
                .border(2.5.dp, BrutalistColors.Black, RoundedCornerShape(8.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = BrutalistColors.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun BrutalistCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = BrutalistColors.OffWhite,
    shadowColor: Color = BrutalistColors.Black,
    shadowOffset: Dp = 6.dp,
    borderWidth: Dp = 3.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .padding(bottom = shadowOffset, end = shadowOffset)
            .background(shadowColor, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .offset(x = -shadowOffset, y = -shadowOffset)
                .background(backgroundColor, RoundedCornerShape(12.dp))
                .border(borderWidth, BrutalistColors.Black, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun BrutalistTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    backgroundColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(BrutalistColors.Black, RoundedCornerShape(10.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = (-3).dp, y = (-3).dp)
                .background(backgroundColor, RoundedCornerShape(10.dp))
                .border(2.5.dp, BrutalistColors.Black, RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                Text(
                    text = placeholder,
                    color = Color.Gray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = BrutalistColors.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                cursorBrush = SolidColor(BrutalistColors.Black),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
