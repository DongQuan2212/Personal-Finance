package hcmute.edu.vn.personalfinancetracker.Model;

import java.util.Date;
import java.util.List;

public class User {
    private String userId; // Thêm trường userId
    private String email;
    private String fullname;
    private Date birthday;
    private String career;
    private double salary;
    private List<Expense> expenses;


    public User() {
    }

    public User(String userId, String email, String fullname, Date birthday, String career, List<Expense> expenses) {
        this.userId = userId;
        this.email = email;
        this.fullname = fullname;
        this.birthday = birthday;
        this.career = career;
        this.expenses = expenses;
    }

    // Getters và setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getCareer() {
        return career;
    }

    public void setCareer(String career) {
        this.career = career;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
    }
}