package hcmute.edu.vn.personalfinancetracker.Model;

import com.google.firebase.firestore.PropertyName;

import java.text.DecimalFormat;
import java.util.Date;

public class Expense {
    private int id;
    private String name;
    private double amount;
    private Date date;
    private boolean isExpense;

    public Expense() {
    }

    public Expense(int id,String name, double amount, Date date, boolean isExpense) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.date = date;
        this.isExpense = isExpense;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @PropertyName("isExpense")
    public boolean isExpense() {
        return isExpense;
    }

    @PropertyName("isExpense")
    public void setExpense(boolean expense) {
        isExpense = expense;
    }

    // Các phương thức tiện ích
    public String getFormattedAmount() {
        DecimalFormat formatter = new DecimalFormat("#,###"+"VNĐ");
        return (isExpense ? "-" : "+") + formatter.format(amount);
    }

    public String getFirstLetter() {
        return name.isEmpty() ? "" : name.substring(0, 1).toUpperCase();
    }
}
