package com.supercilex.robotscouter.ui.scout.viewholder.template;

import android.view.View;
import android.widget.TextView;

import com.supercilex.robotscouter.ui.scout.viewholder.ScoutViewHolderBase;

public class HeaderTemplateViewHolder extends ScoutViewHolderBase<Void, TextView> implements ScoutTemplateViewHolder {
    public HeaderTemplateViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void bind() {
        super.bind();
        mName.setOnFocusChangeListener(this);
    }

    @Override
    public void requestFocus() {
        mName.requestFocus();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) updateMetricName(mName.getText().toString());
    }
}
