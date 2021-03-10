package pro.appcraft.lib.utils.dialog

import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mikepenz.fastadapter.ClickListener
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.adapters.ItemAdapter
import pro.appcraft.lib.utils.dialog.alert.AlertDialogAction
import pro.appcraft.lib.utils.dialog.alert.AlertDialogInputType
import pro.appcraft.lib.utils.dialog.alert.AlertDialogParameters
import pro.appcraft.lib.utils.dialog.bottom.BottomDialogAction
import pro.appcraft.lib.utils.dialog.bottom.BottomDialogButtonItem
import pro.appcraft.lib.utils.dialog.bottom.BottomDialogParameters
import pro.appcraft.lib.utils.dialog.bottom.EmptyDividerDecoration

fun Context.showAlertDialog(
    parameters: AlertDialogParameters,
    header: String? = null,
    message: String,
    cancellable: Boolean = true,
    onCancelListener: (() -> Unit)? = null,
    onInputListener: ((String) -> Unit)? = null,
    actions: List<AlertDialogAction>
) : AlertDialog {
    val dialogView = View.inflate(this, parameters.layoutResId, null)

    val alertDialogBuilder = AlertDialog.Builder(this, R.style.AppTheme_Dialog_Alert)
    alertDialogBuilder.setView(dialogView)
    alertDialogBuilder.setCancelable(cancellable)
    alertDialogBuilder.setOnCancelListener {
        onCancelListener?.invoke()
    }
    val dialog = alertDialogBuilder.create()

    parameters.headerViewId?.let {
        val textViewDialogHeader = dialogView.findViewById<TextView>(it)
        textViewDialogHeader?.apply {
            if (header.isNullOrBlank()) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                text = header
            }
        }
    }
    parameters.messageViewId?.let {
        val textViewDialogMessage = dialogView.findViewById<TextView>(it)
        textViewDialogMessage?.apply {
            if (message.isBlank()) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                text = message
            }
        }
    }
    for (i in (parameters.buttonViewIds.indices)) {
        actions.getOrNull(i)?.let { action ->
            val button = dialogView.findViewById<TextView>(parameters.buttonViewIds[i])
            button.text = action.text
            button.setOnClickListener {
                action.callback(dialog)
            }
        }
    }
    var editTextDialogInput: EditText? = null
    parameters.inputViewId?.let {
        editTextDialogInput = dialogView.findViewById<EditText>(it)?.apply {
            inputType = parameters.inputType.value
            gravity = when (parameters.inputType) {
                AlertDialogInputType.STRING_MULTILINE -> Gravity.START
                else -> Gravity.CENTER
            }
            doOnTextChanged { _, _, _, _ ->
                onInputListener?.invoke(text.toString())
            }
        }
    }

    when (parameters.inputType) {
        AlertDialogInputType.NONE -> dialog.show()
        else -> {
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            dialog.show()
            editTextDialogInput?.requestFocus()
        }
    }
    return dialog
}

fun Context.showBottomDialog(
    parameters: BottomDialogParameters = BottomDialogParameters(
        paddingBetweenItems = resources.getDimensionPixelSize(R.dimen.bottom_dialog_items_padding)
    ),
    header: String? = null,
    cancellable: Boolean = true,
    onCancelListener: (() -> Unit)? = null,
    actions: List<BottomDialogAction>
) : BottomSheetDialog {
    val dialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialog)
    val dialogView = View.inflate(this, parameters.layoutResId, null)
    dialog.setContentView(dialogView)
    dialogView.setOnClickListener { dialog.cancel() }
    dialog.setCancelable(cancellable)
    dialog.setOnCancelListener {
        onCancelListener?.invoke()
    }

    val recyclerView: RecyclerView = dialogView.findViewById(parameters.recyclerViewId)
    val itemAdapter: ItemAdapter<IItem<*>> = ItemAdapter()
    val adapterActions: FastAdapter<IItem<*>> = FastAdapter.with(itemAdapter)
    adapterActions.setHasStableIds(true)
    adapterActions.onClickListener = object : ClickListener<IItem<*>> {
        override fun invoke(v: View?, adapter: IAdapter<IItem<*>>, item: IItem<*>, position: Int): Boolean {
            return if (item is BottomDialogButtonItem) {
                item.bottomDialogAction.callback.invoke(dialog)
                true
            } else {
                false
            }
        }
    }
    recyclerView.apply {
        layoutManager = LinearLayoutManager(context)
        adapter = adapterActions
        itemAnimator = null
        if (parameters.paddingBetweenItems > 0) {
            addItemDecoration(
                EmptyDividerDecoration(
                    parameters.paddingBetweenItems,
                    false
                )
            )
        }
    }
    itemAdapter.setNewList(
        actions.map {
            BottomDialogButtonItem(
                buttonLayoutResId = parameters.buttonLayoutResId,
                bottomDialogAction = it
            )
        }
    )

    parameters.headerViewId?.let {
        val textViewDialogHeader = dialogView.findViewById<TextView>(it)
        textViewDialogHeader?.apply {
            if (header.isNullOrBlank()) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                text = header
            }
        }
    }

    val behavior = BottomSheetBehavior.from(dialogView.parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    behavior.skipCollapsed = true
    behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(view: View, state: Int) {
            if (state == BottomSheetBehavior.STATE_HIDDEN) {
                dialog.cancel()
            }
        }

        override fun onSlide(view: View, v: Float) {}
    })

    dialog.window
        ?.findViewById<View>(com.google.android.material.R.id.container)
        ?.fitsSystemWindows = false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        dialog.window?.decorView?.let {
            it.systemUiVisibility = it.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
    }
    dialogView.findViewById<View>(parameters.contentViewId)?.apply {
        setOnApplyWindowInsetsListener { _, windowInsets ->
            setPaddingRelative(
                paddingStart,
                paddingTop,
                paddingEnd,
                windowInsets.systemWindowInsetBottom
            )
            windowInsets
        }
    }

    dialog.show()
    return dialog
}