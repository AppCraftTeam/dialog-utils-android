package pro.appcraft.lib.utils.dialog

import androidx.appcompat.app.AlertDialog

data class AlertDialogAction(
    val text: String,
    val callback: (dialog: AlertDialog) -> Unit
)
