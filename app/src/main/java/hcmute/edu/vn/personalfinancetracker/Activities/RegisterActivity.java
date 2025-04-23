package hcmute.edu.vn.personalfinancetracker.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import hcmute.edu.vn.personalfinancetracker.Model.User;
import hcmute.edu.vn.personalfinancetracker.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText emailEditText, passwordEditText, confirmPasswordEditText, fullnameEditText, birthdayEditText, careerEditText, salaryEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize EditText fields
        try {
            emailEditText = findViewById(R.id.ed_email);
            passwordEditText = findViewById(R.id.ed_password);
            fullnameEditText = findViewById(R.id.ed_fullname);
            birthdayEditText = findViewById(R.id.ed_birthday);
            careerEditText = findViewById(R.id.ed_career);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize EditText fields: ", e);
            Toast.makeText(this, "Lỗi giao diện, vui lòng kiểm tra lại", Toast.LENGTH_LONG).show();
            return;
        }
        Button registerButton = findViewById(R.id.btn_register);
        TextView loginRedirect = findViewById(R.id.tx_login);


        registerButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            String fullname = fullnameEditText.getText().toString().trim();
            String birthdayStr = birthdayEditText.getText().toString().trim();
            String career = careerEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || fullname.isEmpty() || birthdayStr.isEmpty() || career.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                Date birthday = sdf.parse(birthdayStr);


                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User registered successfully with email: " + email);
                                String userId = mAuth.getCurrentUser().getUid();
                                Log.d(TAG, "User ID: " + userId);
                                // Thêm userId vào đối tượng User
                                User user = new User(userId, email, fullname, birthday, career);
                                db.collection("users").document(userId).set(user)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "User data saved to Firestore for userId: " + userId);
                                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Failed to save user data to Firestore: ", e);
                                            Toast.makeText(RegisterActivity.this, "Lưu thông tin thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            if (mAuth.getCurrentUser() != null) {
                                                mAuth.getCurrentUser().delete();
                                            }
                                        });
                            } else {
                                Log.e(TAG, "User registration failed: ", task.getException());
                                Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid salary format: ", e);
                Toast.makeText(RegisterActivity.this, "Lương không hợp lệ", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Invalid date format: ", e);
                Toast.makeText(RegisterActivity.this, "Ngày sinh không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        loginRedirect.setOnClickListener(v -> {
            finish();
        });
    }
}