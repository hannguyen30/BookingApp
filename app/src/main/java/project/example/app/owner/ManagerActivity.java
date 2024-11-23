package project.example.app.owner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import project.example.app.R;
import project.example.app.adapters.HotelAdapter;
import project.example.app.adapters.ImageAdapter;
import project.example.app.authen.CurrentUserManager;
import project.example.app.classes.Hotel;
import project.example.app.data.FirebaseHelper;

public class ManagerActivity extends AppCompatActivity {

    private FirebaseHelper firebaseHelper;
    private ArrayList<Hotel> hotels;
    private HotelAdapter adapter;
    private boolean isManagerMode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_manager);
        firebaseHelper = new FirebaseHelper();
        FloatingActionButton fabtn_add = findViewById(R.id.fabtn_add);
        Button btn_backToOwnerHome = findViewById(R.id.btn_backToOwnerHome);
        SearchView searchView = findViewById(R.id.sv_searchview);
        ListView lvw_ownerHotelList = findViewById(R.id.lvw_managerHotels);

        isManagerMode = getIntent().getBooleanExtra("isManagerMode", false); // Mặc định là false

        hotels = new ArrayList<>();
        adapter = new HotelAdapter(this, R.layout.item_manager, hotels);
        adapter.setManagerMode(isManagerMode);


        adapter.setOnHotelActionListener(new HotelAdapter.OnHotelActionListener() {
            @Override
            public void onEditClick(Hotel hotel) {
                // Chuyển đến activity sửa thông tin khách sạn
                if (hotel != null) {
                    // Tạo Intent chuyển đến HotelDetailActivity
                    Intent intent = new Intent(ManagerActivity.this, HotelDetailActivity.class);

                    // Truyền các thông tin của khách sạn qua Intent
                    intent.putExtra("hotelId", hotel.getId());
                    intent.putExtra("hotelName", hotel.getName());
                    intent.putExtra("hotelAddress", hotel.getAddress());
                    intent.putExtra("hotelPrice", hotel.getPrice());
                    intent.putExtra("hotelRate", hotel.getRate());
                    intent.putExtra("hotelNumRooms", hotel.getNumRooms());
                    intent.putExtra("hotelProvinceID", hotel.getProvinceID());
                    intent.putExtra("hotelAmenities", hotel.getAmenities());
                    intent.putExtra("hotelImageUrls",hotel.getImageUrls());
                    intent.putExtra("hotelNumRooms", hotel.getNumRooms());
                    intent.putExtra("hotelNumMaxGuest", hotel.getNumMaxGuest());
                    intent.putExtra("hotelPrice", hotel.getPrice());

                    // Khởi chạy HotelDetailActivity
                    startActivity(intent);
                }

            }
            @Override
            public void onDeleteClick(Hotel hotel) {
                // Xác nhận xóa và thực hiện xóa khách sạn
                new AlertDialog.Builder(ManagerActivity.this)
                        .setTitle("Xóa khách sạn")
                        .setMessage("Bạn có chắc chắn muốn xóa khách sạn này?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            // Thực hiện xóa khách sạn
                            firebaseHelper.removeHotel(hotel.getId(), new FirebaseHelper.HotelRemoveCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(ManagerActivity.this, "Khách sạn đã được xóa", Toast.LENGTH_SHORT).show();
                                    loadHotelsByUser(CurrentUserManager.getInstance().getUserId()); // Tải lại danh sách khách sạn
                                }

                                @Override
                                public void onError(Exception e) {
                                    Toast.makeText(ManagerActivity.this, "Lỗi khi xóa khách sạn", Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("Không", null)
                        .show();
            }
        });
        lvw_ownerHotelList.setAdapter(adapter);
        CurrentUserManager currentUserManager = CurrentUserManager.getInstance();
        loadHotelsByUser(currentUserManager.getUserId());

        fabtn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagerActivity.this, HotelPostingActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btn_backToOwnerHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagerActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    private void loadHotelsByUser(String userId) {
        firebaseHelper.getHotelsByUserId(userId, new FirebaseHelper.HotelListCallback() {
            @Override
            public void onCallback(List<Hotel> hotelsList) {
                hotels.clear();
                hotels.addAll(hotelsList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                // Handle the error
            }
        });
    }


}


