package hcmute.edu.vn.personalfinancetracker.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.personalfinancetracker.Adapter.ExpenseAdapter;
import hcmute.edu.vn.personalfinancetracker.Model.Expense;
import hcmute.edu.vn.personalfinancetracker.R;

public class AllExpensesActivity extends AppCompatActivity {

    private static final String TAG = "AllExpensesActivity";
    private RecyclerView expenseRecyclerView;
    private ExpenseAdapter expenseAdapter;
    private List<Expense> expenseList;
    private CalendarView calendarView;
    private ImageView backButton;
    private TextView totalIncomeTextView;
    private TextView totalExpenseTextView;
    private TextView dateListExpense;
    private TextView dateTotalExpense;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SimpleDateFormat dateFormat;
    private DecimalFormat currencyFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_all_expense);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        currencyFormat = new DecimalFormat("#,### VND");

        // Check if user is logged in
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "No user is logged in");
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        expenseRecyclerView = findViewById(R.id.expenseRecyclerView);
        calendarView = findViewById(R.id.calendarView);
        backButton = findViewById(R.id.backButton);
        totalIncomeTextView = findViewById(R.id.totalIncomeTextView);
        totalExpenseTextView = findViewById(R.id.totalExpenseTextView);
        dateListExpense = findViewById(R.id.tx_expense_dayly);
        dateTotalExpense = findViewById(R.id.tx_total_expense);

        // Set up RecyclerView
        expenseList = new ArrayList<>();
        expenseAdapter = new ExpenseAdapter(this, expenseList);
        expenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        expenseRecyclerView.setAdapter(expenseAdapter);

        // Load expenses for the current date by default
        Date currentDate = new Date();
        calendarView.setDate(currentDate.getTime());
        loadExpensesByDate(currentDate);

        // Set up CalendarView listener
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(year, month, dayOfMonth);
            Date newSelectedDate = selectedCalendar.getTime();
            loadExpensesByDate(newSelectedDate);
        });

        // Set up back button listener
        backButton.setOnClickListener(v -> finish());
    }

    private void loadExpensesByDate(Date date) {
        String userId = mAuth.getCurrentUser().getUid();
        String selectedDateStr = dateFormat.format(date);

        // Cập nhật tiêu đề với ngày được chọn
        dateListExpense.setText("Chi tiêu ngày " + selectedDateStr);
        dateTotalExpense.setText("Tổng chi tiêu ngày " + selectedDateStr);

        db.collection("users").document(userId).collection("expenses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    expenseList.clear();
                    double totalIncome = 0.0;
                    double totalExpense = 0.0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Expense expense = doc.toObject(Expense.class);
                        // So sánh ngày (bỏ qua giờ)
                        String expenseDateStr = dateFormat.format(expense.getDate());
                        if (expenseDateStr.equals(selectedDateStr)) {
                            expenseList.add(expense);
                            if (expense.isExpense()) {
                                totalExpense += expense.getAmount();
                            } else {
                                totalIncome += expense.getAmount();
                            }
                            Log.d(TAG, "Loaded Expense for " + selectedDateStr + ": " + expense.getName());
                        }
                    }

                    // Cập nhật RecyclerView
                    expenseAdapter.notifyDataSetChanged();

                    // Cập nhật tổng thu nhập và chi tiêu
                    totalIncomeTextView.setText("Tổng thu nhập: " + currencyFormat.format(totalIncome));
                    totalExpenseTextView.setText("Tổng chi tiêu: " + currencyFormat.format(totalExpense));

                    Log.d(TAG, "Loaded " + expenseList.size() + " expenses for " + selectedDateStr);

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load expenses: ", e);
                    Toast.makeText(this, "Lỗi khi tải danh sách chi tiêu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}