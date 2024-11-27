package project.example.app.customer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import project.example.app.R;
import project.example.app.adapters.HotelAdapter; // Đảm bảo bạn đã import Adapter cho khách sạn
import project.example.app.adapters.ProvinceAdapter;
import project.example.app.classes.Hotel;
import project.example.app.classes.Province;
import project.example.app.data.FirebaseHelper;

public class ProvinceSearchActivity extends AppCompatActivity {

    private ArrayList<Province> provinces;
    private ArrayList<Province> originalProvinces; // Danh sách tỉnh ban đầu
    private ProvinceAdapter provinceAdapter;
    private HotelAdapter hotelAdapter; // Adapter cho danh sách khách sạn
    private ListView lvwProv, lvwHotels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_customer_province_search);

        Button btn_backToSearch = findViewById(R.id.btn_backToSearch);
        SearchView searchView = findViewById(R.id.sv_searchview);
        lvwProv = findViewById(R.id.lvw_prov);
        lvwHotels = findViewById(R.id.lvw_hotels);
        FirebaseHelper firebaseHelper = new FirebaseHelper();

        originalProvinces = new ArrayList<>();
        provinces = new ArrayList<>();

        provinceAdapter = new ProvinceAdapter(this, R.layout.item_province, provinces);
        hotelAdapter = new HotelAdapter(this, R.layout.item_hotel, new ArrayList<>());

        lvwProv.setAdapter(provinceAdapter);
        lvwHotels.setAdapter(hotelAdapter);

        firebaseHelper.getAllProvinces(new FirebaseHelper.ProvinceListCallback() {
            @Override
            public void onCallback(List<Province> provinceList) {
                originalProvinces.clear();
                originalProvinces.addAll(provinceList);
                provinces.clear();
                provinces.addAll(originalProvinces);
                provinceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Log.e("GetAllProvinces", "Error: " + e.getMessage());
            }
        });

        lvwProv.setOnItemClickListener((parent, view, position, id) -> {
            Province selectedProvince = provinces.get(position);
            Intent intent = new Intent(ProvinceSearchActivity.this, HomeActivity.class);
            intent.putExtra("selectedProvinceName", selectedProvince.getName());
            intent.putExtra("selectedProvinceId", selectedProvince.getId());
            Log.d("abasdsafasdfsdafsd", "Error: " + selectedProvince.getId());
            startActivity(intent);
            finish();
        });

        btn_backToSearch.setOnClickListener(v -> finish());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    provinces.clear();
                    provinces.addAll(originalProvinces);
                    provinceAdapter.notifyDataSetChanged();
                    lvwProv.setVisibility(View.VISIBLE);
                    lvwHotels.setVisibility(View.GONE);
                } else {
                    searchHotelsOrProvinces(newText);
                }
                return false;
            }
        });
        lvwHotels.setOnItemClickListener((parent, view, position, id) -> {
            Hotel selectedHotel = hotelAdapter.getItem(position); // Lấy khách sạn được chọn
            if (selectedHotel != null) {
                Intent intent = new Intent(ProvinceSearchActivity.this, HotelDetailActivity.class);
                intent.putExtra("hotelId", selectedHotel.getId()); // Gửi ID khách sạn để lấy thông tin chi tiết
                intent.putExtra("ownerId", selectedHotel.getOwnerId());
                intent.putExtra("hotelName", selectedHotel.getName());
                intent.putExtra("hotelAddress", selectedHotel.getAddress());
                intent.putExtra("hotelProvinceID", selectedHotel.getProvinceID());
                intent.putExtra("hotelAmenities", selectedHotel.getAmenities());
                intent.putExtra("hotelImageUrls", selectedHotel.getImageUrls());
                intent.putExtra("hotelNumRooms", selectedHotel.getNumRooms());
                intent.putExtra("hotelNumMaxGuest", selectedHotel.getNumMaxGuest());
                intent.putExtra("hotelPrice", selectedHotel.getPrice());

                startActivity(intent);
            }
        });
    }

    private void searchHotelsOrProvinces(String query) {
        provinces.clear();
        String queryLower = query.toLowerCase();

        // Tìm kiếm trong danh sách tỉnh
        for (Province province : originalProvinces) {
            String provinceName = province.getName() != null ? province.getName().toLowerCase() : "";
            String provinceId = province.getId() != null ? province.getId().toLowerCase() : "";

            if (provinceName.contains(queryLower) || provinceId.contains(queryLower)) {
                provinces.add(province);
            }
        }

        if (!provinces.isEmpty()) {
            // Hiển thị danh sách tỉnh nếu có kết quả phù hợp
            lvwProv.setVisibility(View.VISIBLE);
            lvwHotels.setVisibility(View.GONE);
            provinceAdapter.notifyDataSetChanged();
        } else {
            // Nếu không tìm thấy tỉnh nào, tìm kiếm khách sạn theo tên
            FirebaseHelper firebaseHelper = new FirebaseHelper();
            firebaseHelper.getHotelsByProvinceOrName(query, new FirebaseHelper.HotelListCallback() {
                @Override
                public void onCallback(List<Hotel> hotelList) {
                    if (!hotelList.isEmpty()) {
                        // Hiển thị danh sách khách sạn nếu tìm thấy kết quả phù hợp
                        lvwProv.setVisibility(View.GONE);
                        lvwHotels.setVisibility(View.VISIBLE);
                        hotelAdapter.clear();
                        hotelAdapter.addAll(hotelList);
                        hotelAdapter.notifyDataSetChanged();
                    } else {
                        // Nếu không tìm thấy kết quả nào
                        lvwProv.setVisibility(View.GONE);
                        lvwHotels.setVisibility(View.GONE);
                        Toast.makeText(ProvinceSearchActivity.this, "Không tìm thấy kết quả", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e("SearchHotels", "Error: " + e.getMessage());
                }
            });
        }
    }
}
