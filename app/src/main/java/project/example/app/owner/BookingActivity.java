package project.example.app.owner;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

import project.example.app.R;
import project.example.app.fragments.BookingCanceledFragment;
import project.example.app.fragments.BookingDoneFragment;
import project.example.app.fragments.BookingOnactiFragment;

public class BookingActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_owner_booking);

        bottomNavigationView = findViewById(R.id.bottomNavigationView_booking);
        replaceFragment(new BookingOnactiFragment());

        // Ánh xạ bottomNavigationView từ layout
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.onActi) {
                replaceFragment(new BookingOnactiFragment());
            } else if (itemId == R.id.done) {
                replaceFragment(new BookingDoneFragment());
            } else if (itemId == R.id.canceled) {
                replaceFragment(new BookingCanceledFragment());
            }
            return true;
        });
    }
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.framelayout_main, fragment);
        fragmentTransaction.commit();
    }
}