package hcmute.edu.vn.personalfinancetracker.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.personalfinancetracker.Model.Expense;
import hcmute.edu.vn.personalfinancetracker.R;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenses;
    private Context context;

    public ExpenseAdapter(Context context, List<Expense> expenses) {
        this.context = context;
        this.expenses = expenses;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.expense_item, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);

        // Hiển thị tên
        holder.nameTextView.setText(expense.getName());

        // Hiển thị ngày (định dạng Date thành String)
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);
        String formattedDate = dateFormat.format(expense.getDate());
        holder.dateTextView.setText(formattedDate);

        // Định dạng số tiền
        String formattedAmount = expense.getFormattedAmount();
        holder.amountTextView.setText(formattedAmount);

        // Lấy chữ cái đầu của tên làm icon
        String firstLetter = expense.getFirstLetter();
        holder.iconTextView.setText(firstLetter);

        // Đặt màu cho số tiền: đỏ nếu là chi tiêu, xanh nếu là thu nhập
        int textColor = expense.isExpense() ?
                ContextCompat.getColor(context, R.color.expense_red) :
                ContextCompat.getColor(context, R.color.income_green);
        holder.amountTextView.setTextColor(textColor);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void addExpense(Expense expense) {
        expenses.add(0, expense);
        notifyItemInserted(0);
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView dateTextView;
        TextView amountTextView;
        TextView iconTextView;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.expenseNameText);
            dateTextView = itemView.findViewById(R.id.expenseDateText);
            amountTextView = itemView.findViewById(R.id.expenseAmountText);
            iconTextView = itemView.findViewById(R.id.expenseIconText);
        }
    }
}