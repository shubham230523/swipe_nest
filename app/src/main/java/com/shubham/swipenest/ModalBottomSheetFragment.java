package com.shubham.swipenest;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.RoundedCorner;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ModalBottomSheetFragment extends BottomSheetDialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_modal_bottom_sheet, container, false);
        //FrameLayout frameLayout = view.setBackground(new RoundedCorner(30));
        view.findViewById(R.id.llEdit).setOnClickListener( v -> {
            Toast.makeText(requireActivity(), "Edit option pressed", Toast.LENGTH_SHORT).show();
        });
        view.findViewById(R.id.llDelete).setOnClickListener( v -> {
            Toast.makeText(requireActivity(), "Delete option pressed", Toast.LENGTH_SHORT).show();
        });
        view.findViewById(R.id.btnCancel).setOnClickListener( v -> {
            BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
            if(dialog!=null){
                dialog.dismiss();
            }
        });
        return view;
    }
}