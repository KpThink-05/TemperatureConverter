package com.kpthink.temperatureconverter;

import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DateFormat;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {

    private final List<HistoryItem> list;
    private final Context ctx;

    public HistoryAdapter(Context ctx, List<HistoryItem> list) {
        this.ctx = ctx;
        this.list = list;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        HistoryItem item = list.get(position);
        holder.tvConversion.setText(item.conversionText);
        holder.tvTimestamp.setText(relativeTime(item.timestamp));

        holder.btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("conversion", item.conversionText);
            clipboard.setPrimaryClip(clip);
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvConversion, tvTimestamp;
        ImageButton btnCopy;
        VH(View v) {
            super(v);
            tvConversion = v.findViewById(R.id.tvConversion);
            tvTimestamp = v.findViewById(R.id.tvTimestamp);
            btnCopy = v.findViewById(R.id.btnCopyHistoryItem);
        }
    }

    private String relativeTime(long ts) {
        long diff = System.currentTimeMillis() - ts;
        long sec = diff/1000;
        if (sec < 60) return sec + "s ago";
        long min = sec/60;
        if (min < 60) return min + "m ago";
        long hr = min/60;
        if (hr < 24) return hr + "h ago";
        return DateFormat.getDateTimeInstance().format(ts);
    }
}
