package hcmute.edu.vn.personalfinancetracker.Model;

public class Expense {
    private String name;
    private double amount;
    private String date;
    private boolean isExpense;

    public Expense() {
    }

    public Expense(String name, double amount, String date, boolean isExpense) {
        this.name = name;
        this.amount = amount;
        this.date = date;
        this.isExpense = isExpense;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public boolean isExpense() {
        return isExpense;
    }

    public String getFormattedAmount() {
        if (isExpense) {
            return String.format("-$%.2f", Math.abs(amount));
        } else {
            return String.format("+$%.2f", amount);
        }
    }

    public String getFirstLetter() {
        if (name != null && !name.isEmpty()) {
            return name.substring(0, 1).toUpperCase();
        }
        return "X";
    }
}
