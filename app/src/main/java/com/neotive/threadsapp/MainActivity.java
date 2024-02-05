package com.neotive.threadsapp;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.neotive.threadsapp.databinding.ActivityMainBinding;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

public class MainActivity extends AppCompatActivity {

    private static final String THREAD_RESULT_KEY = "thread_result";

    private static final int MESSAGE_SUCCESS = 0;
    private static final int MESSAGE_ERROR = 1;

    private Context context = this;

    private Random random = new Random();

    private ActivityMainBinding binding;

    private Handler messagesHandler = new Handler(Looper.getMainLooper(), msg -> {
        // Отрабатывает на главном потоке
        switch (msg.what) {
            case MESSAGE_SUCCESS:
                Bundle data = msg.getData();
                String result = data.getString(THREAD_RESULT_KEY);
                Toast.makeText(context, "Success: " + result, Toast.LENGTH_SHORT).show();
                return true;
            case MESSAGE_ERROR:
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater(), null, false);
        setContentView(binding.getRoot());

        binding.messageButton.setOnClickListener(v -> new MessageThread().start());
        binding.loadingButton.setOnClickListener(v -> {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.loadingButton.setVisibility(View.GONE);
            new LoadingThread().start();
        });
    }

    class MessageThread extends Thread {

        @Override
        public void run() {
            for (int i = 0; i < 100000; i++) {
                System.out.println(i);
            }
            if (random.nextBoolean()) {
                messagesHandler.sendEmptyMessage(MESSAGE_ERROR);
            } else {
                String result = String.valueOf(UUID.randomUUID());
                // Отправляем из побочного потока в главный
                Bundle data = new Bundle();
                data.putString(THREAD_RESULT_KEY, result);
                Message message = new Message();
                message.what = MESSAGE_SUCCESS;
                message.setData(data);
                messagesHandler.sendMessage(message);
            }
        }
    }

    class LoadingThread extends Thread {

        @Override
        public void run() {
            for (int i = 0; i < 100000; i++) {
                System.out.println(i);
            }
            runOnUiThread(() -> {
                binding.progressBar.setVisibility(View.GONE);
                binding.loadingButton.setVisibility(View.VISIBLE);
            });
        }
    }
}
