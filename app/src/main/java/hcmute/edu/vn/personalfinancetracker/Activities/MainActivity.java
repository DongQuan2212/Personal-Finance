package hcmute.edu.vn.personalfinancetracker.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import hcmute.edu.vn.personalfinancetracker.Adapter.ExpenseAdapter;
import hcmute.edu.vn.personalfinancetracker.Model.Expense;
import hcmute.edu.vn.personalfinancetracker.Model.User;
import hcmute.edu.vn.personalfinancetracker.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView expensesRecyclerView;
    private ExpenseAdapter expenseAdapter;
    private List<Expense> expenseList;
    private ConstraintLayout btnAdd, btnMenu, btnSetting, btnStatistical;
    private TextView txFullname, txSalary, viewAllButton;
    private TextView txPhanTramThuNhap, txPhanTramChiTieu;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if user is logged in
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "No user is logged in");
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize views
        expensesRecyclerView = findViewById(R.id.expensesRecyclerView);
        txFullname = findViewById(R.id.tx_fullname);
        txSalary = findViewById(R.id.tx_salary);
        viewAllButton = findViewById(R.id.tx_view_all_expense);
        btnAdd = findViewById(R.id.ic_add);
        btnMenu = findViewById(R.id.ic_menu);
        btnSetting = findViewById(R.id.ic_setting);
        btnStatistical = findViewById(R.id.ic_statistical);
        txPhanTramThuNhap = findViewById(R.id.tx_phantram_thunhap);
        txPhanTramChiTieu = findViewById(R.id.tx_phantram_chitieu);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        // Set up RecyclerView
        expenseList = new ArrayList<>();
        expenseAdapter = new ExpenseAdapter(this, expenseList);
        expensesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        expensesRecyclerView.setAdapter(expenseAdapter);

        // Load user data
        loadUserData();

        // Load expenses from Firestore
        loadExpenses();

        // Set up FAB click listener
        btnAdd.setOnClickListener(view -> showAddExpenseDialog());

        btnMenu.setOnClickListener(v -> {
            if (btnStatistical.getVisibility() == View.GONE && btnSetting.getVisibility() == View.GONE && btnAdd.getVisibility() == View.GONE) {
                btnAdd.setVisibility(View.VISIBLE);
                btnSetting.setVisibility(View.VISIBLE);
                btnStatistical.setVisibility(View.VISIBLE);

                btnAdd.startAnimation(slideUp);
                btnSetting.startAnimation(slideUp);
                btnStatistical.startAnimation(slideUp);
            } else {
                btnAdd.setVisibility(View.GONE);
                btnSetting.setVisibility(View.GONE);
                btnStatistical.setVisibility(View.GONE);
            }
        });

        btnStatistical.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1; // Tháng bắt đầu từ 0
            Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
            intent.putExtra("year", year);
            intent.putExtra("month", month);
            startActivity(intent);
        });
        viewAllButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AllExpensesActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserData() {
        String userId = mAuth.getCurrentUser().getUid();
        String email = mAuth.getCurrentUser().getEmail();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            txFullname.setText(user.getFullname());
                            DecimalFormat formatter = new DecimalFormat("#,###");
                            String formattedSalary = formatter.format(user.getSalary()) + " VND";
                            txSalary.setText(formattedSalary);
                            Log.d(TAG, "User data loaded: fullname=" + user.getFullname() + ", salary=" + user.getSalary());
                        } else {
                            Log.e(TAG, "Failed to parse user data");
                            Toast.makeText(this, "Không thể tải dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "User document does not exist for userId: " + userId);
                        Toast.makeText(this, "Dữ liệu người dùng không tồn tại", Toast.LENGTH_SHORT).show();
                        User defaultUser = new User(userId, email, "Unknown", new Date(), "Unknown");
                        db.collection("users").document(userId).set(defaultUser)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Default user document created for userId: " + userId);
                                    loadUserData();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to create default user document: ", e);
                                    Toast.makeText(this, "Lỗi khi tạo dữ liệu mặc định: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load user data: ", e);
                    Toast.makeText(this, "Lỗi khi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    private void loadExpenses() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("expenses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    expenseList.clear();
                    for (var doc : queryDocumentSnapshots) {
                        Expense expense = doc.toObject(Expense.class);
                        Log.d(TAG, "Loaded Expense: " + expense.getName() + ", isExpense: " + expense.isExpense());
                        expenseList.add(expense);
                    }
                    expenseAdapter.notifyDataSetChanged();
                    updateIncomeExpensePercentages();
                    Log.d(TAG, "Loaded " + expenseList.size() + " expenses from Firestore");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load expenses: ", e);
                    Toast.makeText(this, "Lỗi khi tải danh sách chi tiêu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void updateIncomeExpensePercentages() {
        double totalIncome = 0.0;

        double totalExpense = 0.0;

        // Tính tổng thu nhập và chi tiêu
        for (Expense expense : expenseList) {
            if (expense.isExpense()) {
                totalExpense += expense.getAmount();
            } else {
                totalIncome += expense.getAmount();
            }
        }

        double total = totalIncome + totalExpense;
        double incomePercentage = 0.0;
        double expensePercentage = 0.0;

        // Tính phần trăm (tránh chia cho 0)
        if (total > 0) {
            incomePercentage = (totalIncome / total) * 100;
            expensePercentage = (totalExpense / total) * 100;
        }

        // Định dạng phần trăm
        DecimalFormat df = new DecimalFormat("#.#");
        txPhanTramThuNhap.setText(df.format(incomePercentage) + "%");
        txPhanTramChiTieu.setText(df.format(expensePercentage) + "%");

        Log.d(TAG, "Updated percentages: Income=" + incomePercentage + "%, Expense=" + expensePercentage + "%");
    }

    private void showAddExpenseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.add_expense_dialog, null);
        builder.setView(dialogView);
        TextInputEditText nameInput = dialogView.findViewById(R.id.expenseNameInput);
        TextInputEditText amountInput = dialogView.findViewById(R.id.expenseAmountInput);
        RadioGroup typeRadioGroup = dialogView.findViewById(R.id.expenseTypeRadioGroup);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button addButton = dialogView.findViewById(R.id.addButton);

        AlertDialog dialog = builder.create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        addButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String amountStr = amountInput.getText().toString().trim();

            if (!name.isEmpty() && !amountStr.isEmpty()) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    boolean isExpense = typeRadioGroup.getCheckedRadioButtonId() == R.id.expenseRadioButton;

                    // Lấy ngày hiện tại dưới dạng Date
                    Date date = new Date();

                    // Get reference to user document
                    String userId = mAuth.getCurrentUser().getUid();
                    DocumentReference userRef = db.collection("users").document(userId);

                    // Get lastExpenseId and increment it
                    userRef.get().addOnSuccessListener(documentSnapshot -> {
                        int lastExpenseId = 0;
                        if (documentSnapshot.exists() && documentSnapshot.contains("lastExpenseId")) {
                            lastExpenseId = documentSnapshot.getLong("lastExpenseId").intValue();
                        }
                        int newExpenseId = lastExpenseId + 1;

                        // Create new expense with id and Date
                        Expense newExpense = new Expense(newExpenseId, name, amount, date, isExpense);

                        // Save expense to sub-collection
                        userRef.collection("expenses").add(newExpense)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d(TAG, "Expense added with ID: " + documentReference.getId());

                                    // Update lastExpenseId in user document
                                    userRef.update("lastExpenseId", newExpenseId)
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d(TAG, "Updated lastExpenseId to: " + newExpenseId);

                                                // Update salary
                                                double salaryChange = isExpense ? -amount : amount;
                                                userRef.update("salary", FieldValue.increment(salaryChange))
                                                        .addOnSuccessListener(aVoid2 -> {
                                                            Log.d(TAG, "Salary updated by " + salaryChange);
                                                            loadUserData();
                                                            expenseList.add(newExpense);
                                                            expenseAdapter.notifyItemInserted(expenseList.size() - 1);
                                                            updateIncomeExpensePercentages();
                                                            Toast.makeText(this, "Đã thêm " + (isExpense ? "chi tiêu" : "thu nhập"), Toast.LENGTH_SHORT).show();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.e(TAG, "Failed to update salary: ", e);
                                                            Toast.makeText(this, "Lỗi khi cập nhật lương: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                        });
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Failed to update lastExpenseId: ", e);
                                                Toast.makeText(this, "Lỗi khi cập nhật ID: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to add expense: ", e);
                                    Toast.makeText(this, "Lỗi khi thêm chi tiêu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get lastExpenseId: ", e);
                        Toast.makeText(this, "Lỗi khi lấy ID cuối cùng: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });

                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Nhập số tiền hợp lệ", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}