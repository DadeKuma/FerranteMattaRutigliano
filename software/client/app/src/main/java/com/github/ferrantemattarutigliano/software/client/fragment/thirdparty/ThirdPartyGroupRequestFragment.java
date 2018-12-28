package com.github.ferrantemattarutigliano.software.client.fragment.thirdparty;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.github.ferrantemattarutigliano.software.client.R;
import com.github.ferrantemattarutigliano.software.client.model.GroupRequestDTO;
import com.github.ferrantemattarutigliano.software.client.view.thirdparty.ThirdPartyRequestView;

public class ThirdPartyGroupRequestFragment extends Fragment{
    private ThirdPartyRequestView thirdPartyRequestView;

    public ThirdPartyGroupRequestFragment() {}

    public static ThirdPartyGroupRequestFragment newInstance() {
        return new ThirdPartyGroupRequestFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_group_request, container, false);
        final Button confirmButton = v.findViewById(R.id.button_group_request_send);
        final CheckBox subscribeCheck = v.findViewById(R.id.check_group_request_subscribe);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean subscribe = subscribeCheck.isChecked();
                GroupRequestDTO request = new GroupRequestDTO(); //TODO criteria??
                thirdPartyRequestView.onGroupRequest(request);
            }
        });
        return v;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ThirdPartyRequestView) {
            thirdPartyRequestView = (ThirdPartyRequestView) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ThirdPartyRequestView");
        }
    }
}