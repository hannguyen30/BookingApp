package project.example.app.customer;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import project.example.app.R;
import project.example.app.adapters.ReviewAdapter;
import project.example.app.authen.CurrentUserManager;
import project.example.app.classes.Review;
import project.example.app.data.FirebaseHelper;

public class MyReviewActivity extends AppCompatActivity {

    private FirebaseHelper firebaseHelper;
    private ArrayList<Review> reviews;
    private ReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_customer_my_review);

        firebaseHelper = new FirebaseHelper();
        CurrentUserManager currentUserManager = CurrentUserManager.getInstance();

        reviews = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, R.layout.item_review, reviews);

        ListView lvw_myReview = findViewById(R.id.lvw_myReview);
        lvw_myReview.setAdapter(reviewAdapter);
        loadReviews(currentUserManager.getUserEmail());

    }

    private void loadReviews(String userMail){
        firebaseHelper.getReviewsByUserMail(userMail, new FirebaseHelper.ReviewCallback() {
            @Override
            public void onSuccess(List<Review> reviewList) {
                reviews.clear();
                reviews.addAll(reviewList);
                reviewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("HotelDetailActivity", "Lỗi khi lấy reviews", e);
            }
        });
    }
}