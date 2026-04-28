package com.apkprevention.auth.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apkprevention.auth.ui.theme.DarkCard
import com.apkprevention.auth.ui.theme.InputBorder
import com.apkprevention.auth.ui.theme.TextSecondary

@Composable
fun SocialLoginButtons(
    onGoogleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        SocialButton(
            text = "Google",
            onClick = onGoogleClick,
            icon = { Text("G", color = Color(0xFFDB4437), fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SocialButton(
    text: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, InputBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = DarkCard.copy(alpha = 0.3f),
            contentColor = TextSecondary
        )
    ) {
        icon()
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DividerWithText(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        androidx.compose.material3.HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = InputBorder
        )
        Text(
            text = text.uppercase(),
            color = TextSecondary.copy(alpha = 0.5f),
            fontSize = 11.sp,
            letterSpacing = 0.5.sp,
            fontWeight = FontWeight.Medium
        )
        androidx.compose.material3.HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = InputBorder
        )
    }
}
