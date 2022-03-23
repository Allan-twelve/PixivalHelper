package com.allan.pixivalhelper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.request.target.Target.SIZE_ORIGINAL;

public class ImageInfoActivity extends AppCompatActivity {
    Spider.PixivItem item = new Spider.PixivItem();
    int pageCount = 1;
    String new_url;
    int url_id;
    static int width;
    static final int padding = 100;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_info);
        Intent intent = this.getIntent();
        try {
            item.title = intent.getStringExtra("title");
            item.thumbnail_url =  intent.getStringExtra("thumbnail_url");
            item.caption = intent.getStringExtra("caption");
            item.user_id = intent.getStringExtra("user_id");
            item.user_name = intent.getStringExtra("user_name");
            item.user_icon = intent.getStringExtra("user_icon");
            item.create_date = intent.getStringExtra("create_date");
            item.image_url = intent.getStringExtra("image_url");
            item.page_count = intent.getStringExtra("page_count");
            item.tags_array = new JSONArray(intent.getStringExtra("tags_array"));
            item.image_urls_array = new JSONArray(intent.getStringExtra("image_urls_array"));
            item.view = intent.getStringExtra("view");
            item.bookmarks = intent.getStringExtra("bookmarks");
            item.tools = intent.getStringExtra("tools");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        width = getResources().getDisplayMetrics().widthPixels - padding * 2;

        LinearLayout layout = findViewById(R.id.page_visible);
        ImageView image = findViewById(R.id.image_info);
        if (item.page_count.equals("1")) {
            new_url = item.image_url;
            Glide.with(this).load(item.thumbnail_url).override(width, SIZE_ORIGINAL).placeholder(R.drawable.info_loading).into(image);
            layout.removeAllViews();
        }
        else {
            try {
                url_id = 0;

                new_url = new JSONObject(item.image_urls_array.getJSONObject(url_id)
                        .getString("image_urls")).getString("original")
                        .replaceAll("i.pximg.net", new Spider.Proxy().getHost());
                String url = new JSONObject(item.image_urls_array.getJSONObject(url_id)
                        .getString("image_urls")).getString("medium")
                        .replaceAll("i.pximg.net", new Spider.Proxy().getHost());
                Glide.with(this).load(url).override(width, SIZE_ORIGINAL).placeholder(R.drawable.info_loading).into(image);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            TextView page = findViewById(R.id.page_num_info);
            page.setText("1/" + item.page_count);
            TextView last_t = findViewById(R.id.last_info_text);
            TextView next_t = findViewById(R.id.next_info_text);
            Button last = findViewById(R.id.last_page_info);
            Button next = findViewById(R.id.next_page_info);
            last.setOnClickListener(v -> {
                if (pageCount > 1) {
                    pageCount -= 1;
                    page.setText(pageCount + "/" + item.page_count);
                    try {
                        url_id = pageCount - 1;
                        new_url = new JSONObject(item.image_urls_array.getJSONObject(url_id)
                                .getString("image_urls")).getString("original")
                                .replaceAll("i.pximg.net", new Spider.Proxy().getHost());
                        String url = new JSONObject(item.image_urls_array.getJSONObject(url_id)
                                .getString("image_urls")).getString("medium")
                                .replaceAll("i.pximg.net", new Spider.Proxy().getHost());
                        Glide.with(this).load(url).override(width, SIZE_ORIGINAL).placeholder(R.drawable.info_loading).into(image);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (pageCount == 1) {
                    last.setVisibility(View.INVISIBLE);
                    last_t.setVisibility(View.INVISIBLE);
                }
                if (next.getVisibility() == View.INVISIBLE) {
                    next.setVisibility(View.VISIBLE);
                    next_t.setVisibility(View.VISIBLE);
                }
            });
            next.setOnClickListener(v -> {
                if (pageCount < Integer.parseInt(item.page_count)) {
                    pageCount += 1;
                    page.setText(pageCount + "/" + item.page_count);
                    try {
                        url_id = pageCount - 1;
                        new_url = new JSONObject(item.image_urls_array.getJSONObject(url_id)
                                .getString("image_urls")).getString("original")
                                .replaceAll("i.pximg.net", new Spider.Proxy().getHost());
                        String url = new JSONObject(item.image_urls_array.getJSONObject(url_id)
                                .getString("image_urls")).getString("medium")
                                .replaceAll("i.pximg.net", new Spider.Proxy().getHost());
                        Glide.with(this).load(url).override(width, SIZE_ORIGINAL).placeholder(R.drawable.info_loading).into(image);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (pageCount == Integer.parseInt(item.page_count)) {
                    next.setVisibility(View.INVISIBLE);
                    next_t.setVisibility(View.INVISIBLE);
                }
                if (last.getVisibility() == View.INVISIBLE) {
                    last.setVisibility(View.VISIBLE);
                    last_t.setVisibility(View.VISIBLE);
                }
            });
        }
        TextView title = findViewById(R.id.image_title);
        title.setText(item.title);
        TextView caption = findViewById(R.id.caption);
        caption.setText(Html.fromHtml(item.caption, source -> {
            try {
                InputStream is = (InputStream) new URL(source).getContent();
                Drawable d = Drawable.createFromStream(is, "src");
                d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                is.close();
                return d;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }, null));
        caption.setClickable(true);
        caption.setMovementMethod(LinkMovementMethod.getInstance());

        ImageView userIcon = findViewById(R.id.user_icon);
        Glide.with(this).load(item.user_icon).placeholder(R.drawable.info_loading).into(userIcon);
        userIcon.setOnClickListener(v -> {
            Intent intent_user = new Intent(ImageInfoActivity.this, UserActivity.class);
            intent_user.putExtra("user_id", item.user_id);
            startActivity(intent_user);
        });
        TextView userName = findViewById(R.id.user_name);
        userName.setOnClickListener(v -> {
            Intent intent_user = new Intent(ImageInfoActivity.this, UserActivity.class);
            intent_user.putExtra("user_id", item.user_id);
            startActivity(intent_user);
        });
        userName.setText(item.user_name);

        TextView view = findViewById(R.id.total_view);
        view.setText(item.view);
        TextView book = findViewById(R.id.total_book);
        book.setText(item.bookmarks);
        TextView create_date = findViewById(R.id.create_date);
        create_date.setText(item.create_date);
        TextView tools = findViewById(R.id.tools);
        tools.setText(item.tools);

        Button download = findViewById(R.id.download_img);
        download.setOnClickListener(new DownloadClick());
        List<String> tags_list = new ArrayList<>();
        try {
            for (int i = 0; i < item.tags_array.length(); i++){
                JSONObject object = item.tags_array.getJSONObject(i);
                if (!object.getString("translated_name").equals("null"))
                    tags_list.add("#" + object.getString("translated_name"));
                else
                    tags_list.add("#" + object.getString("name"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RecyclerView recycler = findViewById(R.id.tags);
        TagsAdapter adapter = new TagsAdapter(tags_list);
        StaggeredGridLayoutManager manager =
                new StaggeredGridLayoutManager((tags_list.size() / 2 + 1), StaggeredGridLayoutManager.HORIZONTAL);
        recycler.setItemViewCacheSize(10);
        recycler.setLayoutManager(manager);
        recycler.setAdapter(adapter);
    }

    class DownloadClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            final String[] toast = {null};
            Thread thread = new Thread(() -> {
                try {
                    URL u = new URL(new_url);
                    InputStream inputStream = u.openStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                    if (item.page_count.equals("1"))
                        toast[0] = saveFile(bitmap, item.title + ".png");
                    else
                        toast[0] = saveFile(bitmap, item.title + "_" + url_id + ".png");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            Toast.makeText(ImageInfoActivity.this,"正在开始下载...", Toast.LENGTH_SHORT).show();
            try {
                thread.start();
                thread.join();
                Toast.makeText(ImageInfoActivity.this, toast[0], Toast.LENGTH_SHORT).show();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.ViewHolder> {
        private List<String> list;

        public TagsAdapter(List<String> l){
            this.list = l;
        }

        @Override
        public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tags_layout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String s = list.get(position);
            if (s != null){
                holder.textView.setText(s);
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            TextView textView;
            public ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.tags_layout);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    public String saveFile(Bitmap bitmap, String filename) throws IOException{
        String path = Environment.getExternalStorageDirectory().getPath() + "/DCIM/PixivImage/";
        File local = new File(path);
        if (!local.exists() || !local.isDirectory())
            local.mkdirs();
        File file = new File(path + filename);
        if (file.exists())
            return  "该图片已经存在";
        else {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(file);
            intent.setData(uri);
            sendBroadcast(intent);
            return  "图片保存在：" + file.getAbsolutePath();
        }
    }
}