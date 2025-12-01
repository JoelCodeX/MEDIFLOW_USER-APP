package com.jotadev.mediflow.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TopBarTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun topBar_displaysTitle() {
        val title = "Título de prueba"
        composeRule.setContent {
            TopBar(title = title)
        }
        composeRule.onNodeWithText(title).assertExists()
    }

    @Test
    fun topBar_displaysActionsSlot() {
        composeRule.setContent {
            TopBar(title = "Acciones", actions = {
                IconButton(onClick = {}) {
                    Icon(imageVector = Icons.Default.Email, contentDescription = "actionEmail")
                }
            })
        }
        composeRule.onNodeWithContentDescription("actionEmail").assertExists()
    }
}