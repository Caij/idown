package com.caij.idown;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.caij.down.core.FileData;
import com.caij.down.core.Progress;
import com.caij.down.rx.RxDownload;

import java.io.File;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView textView = findViewById(R.id.tv);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RxDownload rxDownload = new RxDownload();
                rxDownload.down("http://gdown.baidu.com/data/wisegame/8d4cfd1d83733bec/wangzherongyao_43011515.apk",
                        new FileData(new File(getFilesDir(), "test.apk")))
                        .sample(500, MILLISECONDS, true)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSubscriber<Progress>() {
                            @Override
                            public void onNext(Progress progress) {
                                Log.d("DFAD", progress.read + " / " + progress.total);
                                textView.setText(progress.read + " / " + progress.total);
                            }

                            @Override
                            public void onError(Throwable t) {
                                Log.d("DFAD", "error" + t.getMessage());
                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            }
        });
    }
}
