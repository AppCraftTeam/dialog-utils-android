package pro.appcraft.lib.utils.dialog

import android.content.Context
import android.os.Build
import android.text.InputType
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
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
import pro.appcraft.lib.utils.dialog.item.BottomDialogButtonItem
import pro.appcraft.lib.utils.dialog.view.EmptyDividerDecoration

fun Context.showAlertDialog(
    type: AlertDialogType,
    header: String? = null,
    message: String,
    cancellable: Boolean = true,
    onCancelListener: (() -> Unit)? = null,
    onInputListener: ((String) -> Unit)? = null,
    actions: List<AlertDialogAction>
) {
    val layout = when (type) {
        AlertDialogType.ALERT_HORIZONTAL_2_OPTIONS_LEFT_ACCENT -> R.layout.dialog_horizontal_2_options_left_accent
        AlertDialogType.ALERT_HORIZONTAL_2_OPTIONS_RIGHT_ACCENT -> R.layout.dialog_horizontal_2_options_right_accent
        AlertDialogType.ALERT_VERTICAL_3_OPTIONS_TOP_ACCENT -> R.layout.dialog_vertical_3_options_top_accent
        AlertDialogType.ALERT_VERTICAL_2_OPTIONS_TOP_ACCENT -> R.layout.dialog_vertical_2_options_top_accent
        AlertDialogType.ALERT_VERTICAL_1_OPTION_ACCENT -> R.layout.dialog_vertical_1_option_accent
        AlertDialogType.ALERT_VERTICAL_1_OPTION_NO_ACCENT -> R.layout.dialog_vertical_1_option_no_accent
        AlertDialogType.ALERT_INPUT_INT,
        AlertDialogType.ALERT_INPUT_FLOAT,
        AlertDialogType.ALERT_INPUT_STRING,
        AlertDialogType.ALERT_INPUT_STRING_MULTILINE -> R.layout.dialog_input_data
    }
    val dialogView = View.inflate(this, layout, null)

    val alertDialogBuilder = AlertDialog.Builder(this, R.style.AppTheme_Dialog_Alert)
    alertDialogBuilder.setView(dialogView)
    alertDialogBuilder.setCancelable(cancellable)
    alertDialogBuilder.setOnCancelListener {
        onCancelListener?.invoke()
    }
    val alertDialog = alertDialogBuilder.create()

    val textViewDialogHeader = dialogView.findViewById<AppCompatTextView>(R.id.textViewDialogHeader)
    if (TextUtils.isEmpty(header)) {
        textViewDialogHeader.visibility = View.GONE
    } else {
        textViewDialogHeader.visibility = View.VISIBLE
        textViewDialogHeader.text = header
    }

    val textViewDialogMessage = dialogView.findViewById<AppCompatTextView>(R.id.textViewDialogMessage)
    textViewDialogMessage.text = message

    val buttonIds = listOf(
        R.id.buttonDialogFirstAction,
        R.id.buttonDialogSecondAction,
        R.id.buttonDialogThirdAction
    )
    for (i in (0 until type.optionsCount)) {
        actions.getOrNull(i)?.let { action ->
            val button = dialogView.findViewById<AppCompatTextView>(buttonIds[i])
            button.text = action.text
            button.setOnClickListener {
                action.callback.invoke(alertDialog)
            }
        }
    }

    val editTextDialogInput = dialogView.findViewById<AppCompatEditText>(R.id.editTextDialogInput)
    editTextDialogInput?.inputType = when (type) {
        AlertDialogType.ALERT_INPUT_INT -> InputType.TYPE_CLASS_NUMBER
        AlertDialogType.ALERT_INPUT_FLOAT -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        AlertDialogType.ALERT_INPUT_STRING -> InputType.TYPE_CLASS_TEXT
        AlertDialogType.ALERT_INPUT_STRING_MULTILINE -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        else -> InputType.TYPE_NULL
    }
    editTextDialogInput?.gravity = when (type) {
        AlertDialogType.ALERT_INPUT_STRING_MULTILINE -> Gravity.START
        else -> Gravity.CENTER
    }
    editTextDialogInput?.doOnTextChanged { _, _, _, _ ->
        onInputListener?.invoke(editTextDialogInput.text.toString())
    }

    when (type) {
        AlertDialogType.ALERT_INPUT_INT,
        AlertDialogType.ALERT_INPUT_FLOAT,
        AlertDialogType.ALERT_INPUT_STRING,
        AlertDialogType.ALERT_INPUT_STRING_MULTILINE -> {
            alertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            alertDialog.show()
            editTextDialogInput?.requestFocus()
        }
        else -> {
            alertDialog.show()
        }
    }
}

fun Context.showBottomDialog(
    cancellable: Boolean = true,
    onCancelListener: (() -> Unit)? = null,
    actions: List<BottomDialogAction>
) {
    val bottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialog)
    val rootView = View.inflate(this, R.layout.bottom_dialog, null)
    bottomSheetDialog.setContentView(rootView)
    rootView.setOnClickListener { bottomSheetDialog.cancel() }

    val recyclerView: RecyclerView = rootView.findViewById(R.id.recyclerViewDialog)
    val itemAdapter: ItemAdapter<IItem<*>> = ItemAdapter()
    val adapterActions: FastAdapter<IItem<*>> = FastAdapter.with(itemAdapter)
    adapterActions.setHasStableIds(true)
    adapterActions.onClickListener = object : ClickListener<IItem<*>> {
        override fun invoke(v: View?, adapter: IAdapter<IItem<*>>, item: IItem<*>, position: Int): Boolean {
            return if (item is BottomDialogButtonItem) {
                item.bottomDialogAction.callback.invoke(bottomSheetDialog)
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
        addItemDecoration(
            EmptyDividerDecoration(
                this@showBottomDialog,
                R.dimen.baseline_grid_medium,
                false
            )
        )
    }
    itemAdapter.setNewList(actions.map { BottomDialogButtonItem(it) })

    val behavior = BottomSheetBehavior.from(rootView.parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    behavior.skipCollapsed = true
    behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(view: View, state: Int) {
            if (state == BottomSheetBehavior.STATE_HIDDEN) {
                bottomSheetDialog.cancel()
            }
        }

        override fun onSlide(view: View, v: Float) {}
    })

    bottomSheetDialog.setCancelable(cancellable)
    bottomSheetDialog.setOnCancelListener {
        onCancelListener?.invoke()
    }

    bottomSheetDialog.window
        ?.findViewById<View>(com.google.android.material.R.id.container)
        ?.fitsSystemWindows = false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        bottomSheetDialog.window?.decorView?.let {
            it.systemUiVisibility = it.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
    }
    val layoutDialogContent = rootView.findViewById<View>(R.id.layoutDialogContent)
    layoutDialogContent.setOnApplyWindowInsetsListener { _, windowInsets ->
        layoutDialogContent.setPaddingRelative(
            layoutDialogContent.paddingStart,
            layoutDialogContent.paddingTop,
            layoutDialogContent.paddingEnd,
            windowInsets.systemWindowInsetBottom
        )
        windowInsets
    }

    bottomSheetDialog.show()
}