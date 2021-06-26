package com.yaksonn.shoppingapp.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yaksonn.shoppingapp.R
import com.yaksonn.shoppingapp.data.model.ShoppingList
import com.yaksonn.shoppingapp.databinding.ShoppingListItemBinding

class ShoppingListsAdapter constructor(
    private val listener: OnShoppingListClickListener,
    private val context: Context
) : ListAdapter<ShoppingList, ShoppingListsAdapter.ViewHolder>(Companion) {

    inner class ViewHolder(view: ShoppingListItemBinding) : RecyclerView.ViewHolder(view.root), View.OnClickListener {
        val title = view.title
        val secondText = view.secondText

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            listener.onShoppingListClick(getItem(adapterPosition).id, getItem(adapterPosition).isArchived)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ShoppingListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shoppingList = getItem(position)

        holder.itemView.animation = AnimationUtils.loadAnimation(context, R.anim.slide_from_left_to_right)
        holder.title.text = shoppingList.name
        val secondText = context.resources.getString(R.string.shopping_list_item_second_text_prefix) + " ${shoppingList.doneGroceries}/${shoppingList.allGroceries}"
        holder.secondText.text = secondText
    }

    interface OnShoppingListClickListener {
        fun onShoppingListClick(shoppingListId: Int, isArchived: Boolean)
    }

    companion object : DiffUtil.ItemCallback<ShoppingList>() {
        override fun areItemsTheSame(oldItem: ShoppingList, newItem: ShoppingList): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ShoppingList, newItem: ShoppingList): Boolean {
            return oldItem.doneGroceries == newItem.doneGroceries
                    && oldItem.allGroceries == newItem.allGroceries
                    && oldItem.isArchived == newItem.isArchived
        }
    }
}