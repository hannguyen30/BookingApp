package project.example.app.customer;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import project.example.app.R;
import project.example.app.classes.Hotel;

public class DateSelectionActivity extends AppCompatActivity {

    private TextView txtv_checkInDate, txtv_dayNum;
    private Button btn_selectDate, btn_incDay, btn_decDay, btn_done;
    private Calendar checkInCalendar = Calendar.getInstance(); // Lưu ngày check-in
    private int selectedDays = 1; // Số đêm đã chọn
    private Hotel selectedHotel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_date_selection);

        // Khởi tạo các thành phần UI
        txtv_checkInDate = findViewById(R.id.txtv_checkInDate);
        txtv_dayNum = findViewById(R.id.txtv_dayNum);
        btn_selectDate = findViewById(R.id.btn_selectDate);
        btn_incDay = findViewById(R.id.btn_dateInc);
        btn_decDay = findViewById(R.id.btn_dateDec);
        btn_done = findViewById(R.id.btn_done);

        // Cập nhật ngày check-in mặc định là hôm nay
        updateCheckInDateDisplay();

        Intent intent = getIntent();
        selectedHotel = new Hotel(
                intent.getStringExtra("hotelId"),
                intent.getStringExtra("ownerId"),
                intent.getStringExtra("hotelName"),
                intent.getStringExtra("hotelAddress"),
                intent.getStringExtra("hotelProvinceID"),
                intent.getStringExtra("hotelAmenities"),
                intent.getStringExtra("hotelImageUrls"),
                intent.getIntExtra("hotelNumRooms", 0),
                intent.getIntExtra("hotelNumMaxGuest", 0),
                intent.getIntExtra("hotelPrice", 0),
                intent.getIntExtra("hotelNumReviews", 0),
                intent.getIntExtra("hotelRate", 0),
                intent.getBooleanExtra("roomStatus", true)
        );

        // Chọn ngày check-in
        btn_selectDate.setOnClickListener(v -> {
            // Lấy ngày hiện tại và hiển thị DatePickerDialog
            new DatePickerDialog(DateSelectionActivity.this, (view, year, month, dayOfMonth) -> {
                checkInCalendar.set(Calendar.YEAR, year);
                checkInCalendar.set(Calendar.MONTH, month);
                checkInCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateCheckInDateDisplay();
            }, checkInCalendar.get(Calendar.YEAR), checkInCalendar.get(Calendar.MONTH), checkInCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Tăng số đêm
        btn_incDay.setOnClickListener(v -> {
            selectedDays++;
            updateDayDisplay();
        });

        // Giảm số đêm
        btn_decDay.setOnClickListener(v -> {
            if (selectedDays > 1) {
                selectedDays--;
                updateDayDisplay();
            } else {
                Toast.makeText(DateSelectionActivity.this, "Số đêm phải ít nhất là 1", Toast.LENGTH_SHORT).show();
            }
        });

        // Nút hoàn tất
        btn_done.setOnClickListener(v -> openRoomSelectionActivity());
    }

    // Cập nhật ngày check-in trên giao diện
    private void updateCheckInDateDisplay() {
        String dateFormat = "dd/MM/yyyy"; // Định dạng ngày
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
        txtv_checkInDate.setText(sdf.format(checkInCalendar.getTime()));
    }

    // Cập nhật số ngày lưu trú trên giao diện
    private void updateDayDisplay() {
        txtv_dayNum.setText(String.valueOf(selectedDays));
    }

    private void openRoomSelectionActivity() {
        int dayNum = Integer.parseInt(txtv_dayNum.getText().toString());
        Intent intent = new Intent(this,RoomSelectionActivity.class);
        intent.putExtra("hotelId", selectedHotel.getId());
        intent.putExtra("ownerId", selectedHotel.getOwnerId());
        intent.putExtra("hotelName", selectedHotel.getName());
        intent.putExtra("hotelAddress", selectedHotel.getAddress());
        intent.putExtra("hotelProvinceID", selectedHotel.getProvinceID());
        intent.putExtra("hotelAmenities", selectedHotel.getAmenities());
        intent.putExtra("hotelImageUrls", selectedHotel.getImageUrls());
        intent.putExtra("hotelNumRooms", selectedHotel.getNumRooms());
        intent.putExtra("hotelNumMaxGuest", selectedHotel.getNumMaxGuest());
        intent.putExtra("hotelPrice", selectedHotel.getPrice());
        String checkInDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(checkInCalendar.getTime());
        int totalDays = selectedDays;
        intent.putExtra("checkInDate", checkInDate); // Truyền ngày check-in
        intent.putExtra("selectedDays", totalDays); // Truyền số đêm đã chọn
        startActivity(intent);
    }
}
