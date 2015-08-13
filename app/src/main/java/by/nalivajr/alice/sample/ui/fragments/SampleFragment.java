package by.nalivajr.alice.sample.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import by.nalivajr.alice.annonatations.ui.AutoFragment;
import by.nalivajr.alice.annonatations.ui.InnerView;
import by.nalivajr.alice.sample.R;
import by.nalivajr.alice.sample.ui.CustomView;
import by.nalivajr.alice.tools.Alice;

/**
 * Created by Sergey Nalivko.
 * email: snalivko93@gmail.com
 */
@AutoFragment(layoutId = R.layout.fragment_sample_layout, recursive = true)
public class SampleFragment extends Fragment {

    @InnerView(R.id.cv_test)
    private CustomView view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return Alice.viewTools.createView(this, getActivity());
    }
}
