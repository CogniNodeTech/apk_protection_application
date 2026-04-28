package com.apkprevention.auth.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apkprevention.auth.ui.theme.ErrorRed
import com.apkprevention.auth.ui.theme.GreenLight
import com.apkprevention.auth.ui.theme.GreenPrimary
import com.apkprevention.auth.ui.theme.InputBackground
import com.apkprevention.auth.ui.theme.InputBorder
import com.apkprevention.auth.ui.theme.InputBorderFocused
import com.apkprevention.auth.ui.theme.TextHint
import com.apkprevention.auth.ui.theme.TextPrimary
import com.apkprevention.auth.ui.theme.TextSecondary

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    error: String? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    maxLength: Int = Int.MAX_VALUE
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = { if (it.length <= maxLength) onValueChange(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(placeholder, color = TextHint, fontSize = 14.sp)
            },
            label = { Text(placeholder, fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = placeholder,
                    tint = if (error != null) ErrorRed
                           else if (value.isNotEmpty()) GreenLight
                           else TextHint
                )
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                                         else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = TextHint
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation()
                                   else VisualTransformation.None,
            isError = error != null,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = GreenLight,
                focusedLabelColor = TextSecondary,
                unfocusedLabelColor = TextHint,
                errorLabelColor = ErrorRed,
                focusedPlaceholderColor = TextHint,
                unfocusedPlaceholderColor = TextHint,
                focusedBorderColor = InputBorderFocused,
                unfocusedBorderColor = InputBorder,
                errorBorderColor = ErrorRed,
                focusedContainerColor = GreenPrimary.copy(alpha = 0.04f),
                unfocusedContainerColor = InputBackground,
                errorContainerColor = ErrorRed.copy(alpha = 0.04f),
                focusedLeadingIconColor = GreenLight,
                unfocusedLeadingIconColor = TextHint,
                errorLeadingIconColor = ErrorRed
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = { onImeAction() },
                onNext = { onImeAction() }
            )
        )

        if (error != null) {
            Text(
                text = error,
                color = ErrorRed,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}
