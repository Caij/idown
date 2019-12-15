package com.caij.idown;

import androidx.appcompat.app.AppCompatActivity;

import io.reactivex.BackpressureStrategy;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
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


public class MainActivity extends AppCompatActivity {

    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView textView = findViewById(R.id.tv);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RxDownload rxDownload = new RxDownload();
                disposable = rxDownload.down("https://gdown.baidu.com/data/wisegame/6a07541e4b3c7cee/weixin_1560.apk",
                        new FileData(new File(getFilesDir(), "test.apk")), 500)
                        .toFlowable(BackpressureStrategy.LATEST)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSubscriber<Progress>() {

                            private int i;

                            @Override
                            public void onNext(Progress progress) {
                                Log.d("DFAD", progress.read + " / " + progress.total + "  " + (i++));
                            }

                            @Override
                            public void onError(Throwable t) {
                                Log.d("DFAD", "error " + t.getMessage());
                            }

                            @Override
                            public void onComplete() {
                                Log.d("DFAD", "complete");
                            }
                        });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) disposable.dispose();
    }
}
