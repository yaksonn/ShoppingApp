package com.yaksonn.shoppingapp.ui.grocery;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.yaksonn.shoppingapp.R;
import com.yaksonn.shoppingapp.data.model.Grocery;
import com.yaksonn.shoppingapp.databinding.GroceryItemBinding;

import org.jetbrains.annotations.NotNull;

public class GroceryAdapter extends ListAdapter<Grocery, GroceryAdapter.ViewHolder> {
    private final Context context;
    private final OnGroceryClickListener onGroceryClick;
    private final Boolean isArchived;

    public GroceryAdapter(Context context, OnGroceryClickListener onGroceryClick, Boolean isArchived) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.onGroceryClick = onGroceryClick;
        this.isArchived = isArchived;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        OnGroceryClickListener onGroceryClickListener;
        TextView title, secondText, thirdText;
        ImageView statusIcon;
        LinearLayout groceryItem;

        public ViewHolder(@NonNull GroceryItemBinding itemView, OnGroceryClickListener onGroceryClickListener) {
            super(itemView.getRoot());
            title = itemView.title;
            secondText = itemView.secondText;
            thirdText = itemView.thirdText;
            statusIcon = itemView.isDoneIcon;
            groceryItem = itemView.groceryItem;

            this.onGroceryClickListener = onGroceryClickListener;
            if (!isArchived)
                itemView.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onGroceryClickListener.onGroceryClick(getItem(getAdapterPosition()));
            notifyItemChanged(getAdapterPosition());
        }

    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        GroceryItemBinding binding = GroceryItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding, onGroceryClick);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        Grocery grocery = getItem(position);

        holder.itemView.setAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_from_left_to_right));
        holder.title.setText(grocery.getName());
        String xSign = context.getString(R.string.x_sign);
        String groceryPiecesText = xSign + grocery.getPieces();
        holder.secondText.setText(groceryPiecesText);
        String quantity = String.valueOf(grocery.component3());
        holder.thirdText.setText(quantity + " ₺");

        if (grocery.isDone()) {
            holder.title.setPaintFlags(holder.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.secondText.setPaintFlags(holder.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_round_check_circle));
        } else {
            holder.title.setPaintFlags(holder.title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.secondText.setPaintFlags(holder.title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_round_check_circle_outline));
        }

        holder.groceryItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(context, "Uzun tıklamaaa", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    public interface OnGroceryClickListener {
        void onGroceryClick(Grocery grocery);
    }

    public static final DiffUtil.ItemCallback<Grocery> DIFF_CALLBACK = new DiffUtil.ItemCallback<Grocery>() {
        @Override
        public boolean areItemsTheSame(@NonNull @NotNull Grocery oldItem, @NonNull @NotNull Grocery newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(Grocery oldItem, Grocery newItem) {
            return oldItem.isDone() == newItem.isDone();
        }
    };
}
