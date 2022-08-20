package buaa.uavswarm.urbanmissionmap.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import buaa.uavswarm.urbanmissionmap.R;

public class ConfirmFormationDialog extends DialogFragment {
    private View mView;
    private TextView mTvContent;
    private String content = "001";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        mView = inflater.inflate(R.layout.dialog_confirm, container);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        initView();
        return mView;
    }

    private void initView() {
        TextView mTvSure = (TextView) mView.findViewById(R.id.tv_sure);
        TextView mTvCancel = (TextView) mView.findViewById(R.id.tv_cancel);
        mTvContent = (TextView) mView.findViewById(R.id.dialog_formationno);
        mTvContent.setText(content);
        mTvSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onSureClick();
                }
                dismiss();
            }
        });
        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onCancelClick();
                }
                dismiss();
            }
        });
    }

    public void setContent(String content) {
        this.content = content;

    }

    public interface onDialogClickListener {
        public void onSureClick();

        public void onCancelClick();
    }

    private onDialogClickListener mListener;

    public void setDialogClickListener(onDialogClickListener mListener) {
        this.mListener = mListener;
    }

}
