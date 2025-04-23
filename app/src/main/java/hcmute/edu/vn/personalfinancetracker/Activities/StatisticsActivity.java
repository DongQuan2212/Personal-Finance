package hcmute.edu.vn.personalfinancetracker.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import hcmute.edu.vn.personalfinancetracker.Model.Expense;
import hcmute.edu.vn.personalfinancetracker.R;

public class StatisticsActivity extends AppCompatActivity {

    private static final String TAG = "StatisticsActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView monthTextView, totalIncomeTextView, totalExpenseTextView, differenceTextView;
    private ImageView previousMonth, nextMonth, backButton;
    private BarChart barChart;
    private int currentYear, currentMonth;
    private int daysInMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        monthTextView = findViewById(R.id.monthTextView);
        totalIncomeTextView = findViewById(R.id.totalIncomeTextView);
        totalExpenseTextView = findViewById(R.id.totalExpenseTextView);
        differenceTextView = findViewById(R.id.differenceTextView);
        previousMonth = findViewById(R.id.previousMonth);
        nextMonth = findViewById(R.id.nextMonth);
        backButton = findViewById(R.id.backButton);
        barChart = findViewById(R.id.barChart);

        // Cấu hình cơ bản cho biểu đồ
        setupBarChart();

        // Lấy tháng/năm từ Intent (mặc định là tháng hiện tại)
        Intent intent = getIntent();
        Calendar calendar = Calendar.getInstance();
        currentYear = intent.getIntExtra("year", calendar.get(Calendar.YEAR));
        currentMonth = intent.getIntExtra("month", calendar.get(Calendar.MONTH) + 1);

        // Tính số ngày trong tháng
        updateDaysInMonth();

        // Hiển thị tháng/năm ban đầu
        updateMonthTextView();

        // Load dữ liệu cho tháng hiện tại
        loadStatistics(currentYear, currentMonth);

        // Xử lý nút back
        backButton.setOnClickListener(v -> finish());

        // Xử lý nút previousMonth
        previousMonth.setOnClickListener(v -> {
            if (currentMonth == 1) {
                currentMonth = 12;
                currentYear--;
            } else {
                currentMonth--;
            }
            // Cập nhật số ngày trong tháng
            updateDaysInMonth();
            updateMonthTextView();
            loadStatistics(currentYear, currentMonth);
        });

        // Xử lý nút nextMonth
        nextMonth.setOnClickListener(v -> {
            if (currentMonth == 12) {
                currentMonth = 1;
                currentYear++;
            } else {
                currentMonth++;
            }
            // Cập nhật số ngày trong tháng
            updateDaysInMonth();
            updateMonthTextView();
            loadStatistics(currentYear, currentMonth);
        });
    }

    private void updateDaysInMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(currentYear, currentMonth - 1, 1);
        daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
    private void updateMonthTextView() {
        String monthYearText = "Tháng " + currentMonth + "/" + currentYear;
        monthTextView.setText(monthYearText);
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);
        barChart.setScaleEnabled(true);
        barChart.setPinchZoom(true);
        barChart.setDrawValueAboveBar(true);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(true);
        barChart.animateY(1000);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(10f);
    }

    private void loadStatistics(int year, int month) {
        String userId = mAuth.getCurrentUser().getUid();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1, 0, 0, 0); // Đầu tháng
        Date startOfMonth = calendar.getTime();
        calendar.set(year, month, 1, 0, 0, 0); // Đầu tháng sau
        calendar.add(Calendar.SECOND, -1); // Cuối tháng hiện tại
        Date endOfMonth = calendar.getTime();
        // Maps để lưu trữ thu nhập và chi tiêu theo ngày
        Map<Integer, Double> incomeByDay = new HashMap<>();
        Map<Integer, Double> expenseByDay = new HashMap<>();

        // Khởi tạo giá trị mặc định cho tất cả các ngày trong tháng
        for (int day = 1; day <= daysInMonth; day++) {
            incomeByDay.put(day, 0.0);
            expenseByDay.put(day, 0.0);
        }
        db.collection("users").document(userId).collection("expenses")
                .whereGreaterThanOrEqualTo("date", startOfMonth)
                .whereLessThanOrEqualTo("date", endOfMonth)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalIncome = 0.0;
                    double totalExpense = 0.0;

                    SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());

                    // Tính tổng thu nhập và chi tiêu theo ngày
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Expense expense = doc.toObject(Expense.class);
                        Date date = expense.getDate();
                        int day = Integer.parseInt(dayFormat.format(date));

                        if (expense.isExpense()) {
                            totalExpense += expense.getAmount();
                            expenseByDay.put(day, expenseByDay.getOrDefault(day, 0.0) + expense.getAmount());
                        } else {
                            totalIncome += expense.getAmount();
                            incomeByDay.put(day, incomeByDay.getOrDefault(day, 0.0) + expense.getAmount());
                        }
                    }

                    // Cập nhật giao diện
                    updateUI(totalIncome, totalExpense, incomeByDay, expenseByDay);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load statistics: ", e);
                    totalIncomeTextView.setText("Tổng thu nhập: 0 VND");
                    totalExpenseTextView.setText("Tổng chi tiêu: 0 VND");
                    differenceTextView.setText("Tổng: 0 VND");
                    barChart.clear();
                    barChart.invalidate();
                });
    }

    private void updateUI(double totalIncome, double totalExpense,
                          Map<Integer, Double> incomeByDay, Map<Integer, Double> expenseByDay) {
        // Định dạng số tiền
        DecimalFormat formatter = new DecimalFormat("#,###");
        totalIncomeTextView.setText("Tổng thu nhập: " + formatter.format(totalIncome) + " VND");
        totalExpenseTextView.setText("Tổng chi tiêu: " + formatter.format(totalExpense) + " VND");
        differenceTextView.setText("Tổng: " + formatter.format(totalIncome - totalExpense) + " VND");

        // Vẽ biểu đồ theo ngày
        drawSummaryBarChart(totalIncome, totalExpense);
    }

    private void drawSummaryBarChart(double totalIncome, double totalExpense) {
        // Xóa dữ liệu hiện tại
        barChart.clear();

        // Tạo dữ liệu cho biểu đồ
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, (float) totalIncome)); // Cột 0: Thu nhập
        entries.add(new BarEntry(1, (float) totalExpense)); // Cột 1: Chi tiêu

        BarDataSet dataSet = new BarDataSet(entries, "Thống kê");
        dataSet.setColors(
                ContextCompat.getColor(this, R.color.income_green),
                ContextCompat.getColor(this, R.color.expense_red)
        );
        dataSet.setValueTextSize(12f);
        dataSet.setDrawValues(true);

        // Định dạng số trên cột
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value > 1000000) {
                    return String.format("%.1fM", value / 1000000);
                } else if (value > 1000) {
                    return String.format("%.0fK", value / 1000);
                }
                return String.valueOf((int) value);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.4f);

        // Cấu hình biểu đồ
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getXAxis().setLabelCount(2);
        barChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == 0) return "Thu nhập";
                else if (value == 1) return "Chi tiêu";
                return "";
            }
        });

        // Tắt zoom
        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);

        // Hiệu ứng
        barChart.animateY(1000);

        barChart.invalidate();
    }
}