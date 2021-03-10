package pro.appcraft.lib.utils.dialog.bottom

import android.graphics.Typeface
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatTextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import pro.appcraft.lib.utils.dialog.R

class BottomDialogButtonItem(
    @LayoutRes buttonLayoutResId: Int,
    val bottomDialogAction: BottomDialogAction
) : AbstractItem<BottomDialogButtonItem.ViewHolder>() {
    override val type: Int = R.id.bottomDialogButtonItem

    override val layoutRes = buttonLayoutResId

    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

    class ViewHolder(view: View) : FastAdapter.ViewHolder<BottomDialogButtonItem>(view) {
        private val textViewName: AppCompatTextView = view.findViewById(R.id.textViewName)

        override fun bindView(item: BottomDialogButtonItem, payloads: List<Any>) {
            textViewName.text = item.bottomDialogAction.text
            if (item.bottomDialogAction.selected == true) {
                textViewName.setTypeface(textViewName.typeface, Typeface.BOLD)
            } else {
                textViewName.setTypeface(textViewName.typeface, Typeface.NORMAL)
            }
        }

        override fun unbindView(item: BottomDialogButtonItem) { }
    }
}