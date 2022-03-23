package com.allan.pixivalhelper;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.bumptech.glide.Glide;

import java.util.Calendar;

import static com.bumptech.glide.request.target.Target.SIZE_ORIGINAL;


public class MainActivity extends AppCompatActivity {

    Spider.PixivItem[] item_list;
    RecyclerView recyclerView;
    Spinner spinner;
    static String mode;
    static String date;
    static String page = "1";
    static int width;
    static final int padding = 10;
    Calendar calendar = Calendar.getInstance();

    private static final String[] PERMISSIONS_STORAGE ={
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.image_recyclerView);
        verifyStoragePermissions(this);

        Button refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(v -> start());

        TextView page_num = findViewById(R.id.page_num);
//        自选页数
//        page_num.setOnClickListener();
        Button next = findViewById(R.id.next_page);
        Button last = findViewById(R.id.last_page);
        TextView next_t = findViewById(R.id.next_page_text);
        TextView last_t = findViewById(R.id.last_page_text);
        next.setOnClickListener(v -> {
            if (page.equals("18"))
                Toast.makeText(this, "已经到底了呢", Toast.LENGTH_SHORT).show();
            else{
                page = String.valueOf(Integer.parseInt(page) + 1);
                page_num.setText(page);
                start();
            }
            if (page.equals("18")) {
                next.setVisibility(View.INVISIBLE);
                next_t.setVisibility(View.INVISIBLE);
            }
            if (last.getVisibility() == View.INVISIBLE) {
                last.setVisibility(View.VISIBLE);
                last_t.setVisibility(View.VISIBLE);
            }
        });
        last.setOnClickListener(v -> {
            if (page.equals("1"))
                Toast.makeText(this, "已经是第一页了呢", Toast.LENGTH_SHORT).show();
            else{
                page = String.valueOf(Integer.parseInt(page) - 1);
                page_num.setText(page);
                start();
            }
            if (page.equals("1")) {
                last.setVisibility(View.INVISIBLE);
                last_t.setVisibility(View.INVISIBLE);
            }
            if (next.getVisibility() == View.INVISIBLE) {
                next.setVisibility(View.VISIBLE);
                next_t.setVisibility(View.VISIBLE);
            }
        });

        initDate();
        Button date_select = findViewById(R.id.choose_date);
        date_select.setText(date);
        date_select.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = getLayoutInflater().inflate(R.layout.date_layout, null);
            DatePicker datePicker = view.findViewById(R.id.date_data);
            calendar = Calendar.getInstance();
            datePicker.setMinDate(1189612800000L);
            datePicker.setMaxDate(System.currentTimeMillis() - 172800000);
            builder.setView(view);
            builder.create();
            AlertDialog dialog = builder.show();
            final int[] old_year = {calendar.get(Calendar.YEAR)};
            final int[] old_month = {calendar.get(Calendar.MONTH)};
            final int[] old_day = {calendar.get(Calendar.DAY_OF_MONTH)};
            datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH) - 2,
                    (view1, year, monthOfYear, dayOfMonth) -> {
                if (year == old_year[0] && monthOfYear == old_month[0] && dayOfMonth == old_day[0]) {
                    String month, day;
                    if (((monthOfYear + 1) / 10) == 0)
                        month = "0" + (monthOfYear + 1);
                    else
                        month = (monthOfYear + 1) + "";
                    if ((dayOfMonth / 10) == 0)
                        day = "0" + dayOfMonth;
                    else
                        day = dayOfMonth + "";
                    date = year + "-" + month + "-" + day;
                    date_select.setText(date);
                    dialog.dismiss();
                    start();
                }
                else {
                    old_year[0] = year;
                    old_month[0] = monthOfYear;
                    old_day[0] = dayOfMonth;
                }
            });
        });

        spinner = findViewById(R.id.choose_modes);
        String[] modes = getResources().getStringArray(R.array.modes);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, modes);
        adapter.setDropDownViewResource(R.layout.my_drop_down_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new OnSelectedListener());
        spinner.setSelection(0);

        width = (getResources().getDisplayMetrics().widthPixels - padding * 4) / 2;
    }

    private static void verifyStoragePermissions(Activity activity){
        int permission = ActivityCompat.
                checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE");
        if (permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, 1);
        }
    }

    private void initDate(){
        calendar.add(Calendar.DATE, -2);
        String month, day;
        if (((calendar.get(Calendar.MONTH) + 1) / 10) == 0)
            month = "0" + (calendar.get(Calendar.MONTH) + 1);
        else
            month = (calendar.get(Calendar.MONTH + 1)) + "";
        if ((calendar.get(Calendar.DAY_OF_MONTH) / 10) == 0)
            day = "0" + calendar.get(Calendar.DAY_OF_MONTH);
        else
            day = calendar.get(Calendar.DAY_OF_MONTH) + "";
        date = calendar.get(Calendar.YEAR) +"-"+ month + "-" + day;
    }

    private class OnSelectedListener implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0){
                mode = "day";
            }
            else if (position == 1){
                mode = "week";
            }
            else if (position == 2){
                mode = "month";
            }
            else if (position == 3){
                mode = "day_male";
            }
            else if (position == 4){
                mode = "day_female";
            }
            else {
                mode = "week_rookie";
            }
            start();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private void start(){
        try {
            Thread thread = new Thread(() -> item_list =  new Spider().start(mode, date, page));
            thread.start();
            thread.join();
            initView(recyclerView, item_list);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initView(RecyclerView recyclerView, Spider.PixivItem[] list) {
        ImageAdapter adapter = new ImageAdapter(list);
        StaggeredGridLayoutManager manager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setItemViewCacheSize(30);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }

    public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
        private Spider.PixivItem[] item_list;

        public ImageAdapter(Spider.PixivItem[] list) {
            this.item_list = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_view, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Spider.PixivItem item = item_list[position];
            if (item != null) {
                Glide.with(holder.itemView).load(item.thumbnail_url).override(width, SIZE_ORIGINAL)
                        .placeholder(R.drawable.loading).into(holder.image);
                holder.image.setOnClickListener(v1 -> {
                    Intent intent = new Intent(MainActivity.this, ImageInfoActivity.class);
                    intent.putExtra("title", item.title);
                    intent.putExtra("thumbnail_url", item.thumbnail_url);
                    intent.putExtra("caption", item.caption);
                    intent.putExtra("user_id", item.user_id);
                    intent.putExtra("user_name", item.user_name);
                    intent.putExtra("user_icon", item.user_icon);
                    intent.putExtra("create_date", item.create_date);
                    intent.putExtra("image_url", item.image_url);
                    intent.putExtra("page_count", item.page_count);
                    intent.putExtra("tags_array", item.tags_array.toString());
                    intent.putExtra("image_urls_array", item.image_urls_array.toString());
                    intent.putExtra("view", item.view);
                    intent.putExtra("bookmarks", item.bookmarks);
                    intent.putExtra("tools", item.tools);
                    startActivity(intent);
                });
            }
        }

        @Override
        public int getItemCount() {
            return item_list.length;
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            ImageView image;
            public ViewHolder(View view){
                super(view);
                image = view.findViewById(R.id.image_item);
            }
        }
    }
}