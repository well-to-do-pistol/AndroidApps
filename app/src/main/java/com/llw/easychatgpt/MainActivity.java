package com.llw.easychatgpt;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    TextView welcomeTextView;
    EditText messageEditText;
    ImageButton sendButton;
    List<Message> messagesList;
    MessageAdapter messageAdapter;
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

//    OkHttpClient client = new OkHttpClient();
    OkHttpClient client = new OkHttpClient.Builder()
        .readTimeout(180, TimeUnit.SECONDS)
        .build(); //解决长文本超时问题


//    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.colorPrimaryDark);
        }

        messagesList = new ArrayList<>();

        recyclerView = findViewById(R.id.recycler_view);
        welcomeTextView = findViewById(R.id.welcome_text);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_btn);

        //setup recycler view
        messageAdapter = new MessageAdapter(messagesList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        sendButton.setOnClickListener((v)->{
            String question = null;
            if(messageEditText.getText().toString().equals("")){question = "你好, 我的宝贝女友";}else{ question = messageEditText.getText().toString().trim();}
            Log.e("GGGGGGGG",question);
            addToChat(question,Message.SENT_BY_ME);
            messageEditText.setText("");
            try {
                startWenXin(question);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            welcomeTextView.setVisibility(View.GONE);

        });
    }

    void addToChat(String message, String sentBy){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messagesList.add(new Message(message,sentBy));
                messageAdapter.notifyDataSetChanged(); //数组一改变就修改视图
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });

    }

    void addResponse(String response){
        Log.d("RRR","ccc");
        messagesList.remove(messagesList.size()-1);
        addToChat(response, Message.SENT_BY_BOT);
    }

    void callAPI(String question, String token){

        MediaType mediaType = MediaType.parse("application/json");
        String jsonTemplate = "{\"user_id\":\"74170531\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}],\"temperature\":0.8,\"top_p\":0.8,\"penalty_score\":1,\"system\":\"你是陈韦昊的女朋友兼职女仆。你很喜欢夸奖我，平常以一句夸奖自己的优美的句子并介绍自己的话开始。以赞美我的优美句子结束，你称呼我为你的主人。\",\"stop\":[],\"disable_search\":false,\"enable_citation\":false,\"enable_trace\":false,\"max_output_tokens\":2000}";
        String jsonString = String.format(jsonTemplate, question);
        RequestBody body = RequestBody.create(mediaType, jsonString);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-4.0-8k-0329?access_token=" + token)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                addResponse("1Failed to load response due to "+e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()){
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = null;
                    try {
                        jsonResponse = new JSONObject(responseBody);
                        String result = jsonResponse.getString("result");
                        addResponse(result.trim()); // Use result
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }else {
//                    addResponse("2Failed to load response due to "+response.body().string());
                }
            }
        });
    }

    public static final String API_KEY = "";
    public static final String SECRET_KEY = "";

    static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

    void startWenXin(String question) throws IOException, JSONException { //先拿到access_token
        //okhttp, 我是CWH的小女仆, 主人请稍等, 我正努力打字哟...
        messagesList.add(new Message("我是CWH的小女仆, 主人请稍等, 我正努力打字哟...",Message.SENT_BY_BOT));
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + API_KEY
                + "&client_secret=" + SECRET_KEY);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Handle failure
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // Handle response
                try {
                    String responseBody = response.body().string();
                    Log.d("Response", responseBody); // Log the response body
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String accessToken = jsonResponse.getString("access_token");
                    callAPI(question, accessToken);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }


}