package com.allan.pixivalhelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class Spider {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36";
    private static final String REFERER_IZ = "https://pixiviz.pwp.app/rank/";
    private static final String HOST_IZ = "i.pixiv.re";
    private static final String REFERER_IC = "https://pixivic.com/";
    private static final String HOST_IC = "o.acgpic.net";
    private static final String REFERER_EL = "https://pixivel.moe/";
    private static final String HOST_EL = "proxy.pixivel.moe";

    public static class Proxy{
        String host = HOST_IZ;
        String referer = REFERER_IZ;

        public String getHost() {
            return host;
        }

        public String getReferer() {
            return referer;
        }
    }
    Proxy proxy = new Proxy();

    /**
     * Create url
     * @param mode day, week, month, day_male, day_female, week_rookie
     * @param date eg.2022-03-10
     * @param page start from 1 (not 0)
     * @return accessible url
     */
    private String urlBuilder(String mode, String date, String page){
        return "https://pixiviz.pwp.app/api/v1/illust/rank?mode=" + mode + "&date=" + date + "&page=" + page;
    }

    private String urlBuilder(String mode, String date){
        return urlBuilder(mode, date, String.valueOf(1));
    }

    private String urlBuilder(String mode, String date, int page){
        return urlBuilder(mode, date, String.valueOf(page));
    }

    /**
     * Build request headers
     * @return headers
     */
    private Map<String, String> headersBuilder(){
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", USER_AGENT);
        headers.put("Referer", proxy.getReferer());
        return headers;
    }

    /**
     * Connect url and get the response json string resource
     * @param url accessible url
     * @return json string resource
     */
    private String connect(String url){
        String res = null;
        try {
            res = Jsoup.connect(url)
                    .headers(headersBuilder())
                    .ignoreContentType(true)
                    .method(Connection.Method.GET)
                    .maxBodySize(0)
                    .timeout(1000000)
                    .execute()
                    .body()
                    .replaceAll("\\{\"illusts\":", "")
                    .replaceAll("]\\}", "]");
        } catch (Exception e){
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Parse the json resource
     * @param res string json resource
     * @return PixivItem list
     */
    private PixivItem[] parse(String res){
        try {
            JSONArray array = new JSONArray(res);
            PixivItem[] item_list = new PixivItem[array.length()];
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                if (!jsonObject.getString("type").equals("illust"))
                    continue;
                PixivItem item = new PixivItem();
                // 图片id
                item.id = jsonObject.getString("id");
                // 标题
                item.title = jsonObject.getString("title");
                // 缩略图地址集
                item.thumbnail_url = new JSONObject(jsonObject.getString("image_urls")).getString("medium")
                        .replaceAll("i.pximg.net", proxy.getHost());
                // 描述
                item.caption = jsonObject.getString("caption");
                // 图片数量
                item.page_count = jsonObject.getString("page_count");
                // 作者id
                item.user_id = new JSONObject(jsonObject.getString("user")).getString("id");
                // 作者姓名
                item.user_name = new JSONObject(jsonObject.getString("user")).getString("name");
                // 作者头像图标
                item.user_icon = new JSONObject(new JSONObject(jsonObject.getString("user"))
                        .getString("profile_image_urls")).getString("medium")
                        .replaceAll("i.pximg.net", proxy.getHost());
                // tags
                item.tags_array = new JSONArray(jsonObject.getString("tags"));
                // 作图工具
                item.tools = jsonObject.getString("tools")
                        .replaceAll("\\[", "")
                        .replaceAll("]", "")
                        .replaceAll("\"", "")
                        .replaceAll(",", ", ");
                // 创作日期
                item.create_date = jsonObject.getString("create_date")
                        .replace("2022-", "2022年").replace("-", "月")
                        .replace("T", "日 ");
                // 图片地址
                if (Objects.equals(item.page_count, String.valueOf(1))) {
                    item.image_url = new JSONObject(jsonObject.getString("meta_single_page"))
                            .getString("original_image_url")
                            .replaceAll("i.pximg.net", proxy.getHost());
                    item.image_urls_array = new JSONArray("[]");
                }
                else {
                    item.image_url = "";
                    item.image_urls_array = new JSONArray(jsonObject.getString("meta_pages")
                            .replaceAll("i.pximg.net", proxy.getHost()));
                }
                // 访问数量
                item.view = jsonObject.getString("total_view");
                // 收藏书签
                item.bookmarks = jsonObject.getString("total_bookmarks");
                item_list[i] = item;
            }
            return item_list;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Start working
     * @param mode day, week, month, day_male, day_female, week_rookie
     * @param date eg.2022.03.10
     * @param page string value of 1, 2, 3...
     * @return PixivItem list
     */
    public PixivItem[] start(String mode, String date, String page){
        return parse(connect(urlBuilder(mode, date, page)));
    }

    public PixivItem[] start(String mode, String data){
        return start(mode, data, String.valueOf(1));
    }

    public PixivItem[] start(String mode, String data, int page){
        return start(mode, data, String.valueOf(page));
    }

    public static class PixivItem {
        String id;
        String title;
        String thumbnail_url;
        String caption;
        String user_id;
        String user_name;
        String user_icon;
        JSONArray tags_array;
        String tools;
        String create_date;
        String page_count;
        String image_url;
        JSONArray image_urls_array;
        String view;
        String bookmarks;
    }
}


