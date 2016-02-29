package com.ouj.library.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.ouj.library.R;

/**
 * Created by Roye on 2016/1/19.
 */
public class PageFrameActivity extends AppCompatActivity {

    public final static String EXTRA_TITLE = "title";
    public final static String EXTRA_CLASS = "fragmentClassName";

    public static void launchActivity(Activity activity, String title, String fragmentClassName, Bundle extras) {
        Intent intent = new Intent(activity, PageFrameActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_CLASS, fragmentClassName);
        if (extras != null)
            intent.putExtras(extras);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base__activity_page_frame);

        Intent data = getIntent();
        if (data != null) {
            String title = data.getStringExtra(EXTRA_TITLE);
            String fragmentClassName = data.getStringExtra(EXTRA_CLASS);
            Fragment fragment = null;
            try {
                fragment = (Fragment) Class.forName(fragmentClassName).newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (fragment != null) {
                Bundle args = new Bundle();
                args.putString(EXTRA_TITLE, title);
                if (getIntent() != null && getIntent().getExtras() != null) {
                    args.putAll(getIntent().getExtras());
                }
                fragment.setArguments(args);
                FragmentManager fm = getSupportFragmentManager();
                fm.beginTransaction().replace(R.id.fragment_content, fragment)
                        .commitAllowingStateLoss();
            }
        }


    }

}
