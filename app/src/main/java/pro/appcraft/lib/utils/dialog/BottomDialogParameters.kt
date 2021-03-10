package pro.appcraft.lib.utils.dialog

import androidx.annotation.IdRes
import androidx.annotation.LayoutRes

data class BottomDialogParameters(
    @LayoutRes val layoutResId: Int = R.layout.bottom_dialog,
    @LayoutRes val buttonLayoutResId: Int = R.layout.item_bottom_dialog_button,
    @IdRes val contentViewId: Int = R.id.layoutDialogContent,
    @IdRes val recyclerViewId: Int = R.id.recyclerViewDialog,
    @IdRes val headerViewId: Int? = R.id.textViewDialogHeader,
    val paddingBetweenItems: Boolean = true
)
