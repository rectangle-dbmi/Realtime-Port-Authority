package rectangledbmi.com.pittsburghrealtimetracker.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import rectangledbmi.com.pittsburghrealtimetracker.R;
import rectangledbmi.com.pittsburghrealtimetracker.ui.views.CustomImageView;

public class AboutAdapter extends RecyclerView.Adapter<AboutAdapter.ContribViewHolder> {

    private ArrayList<TextListData> contribList;
    private Activity activity;

    public AboutAdapter(final Activity activity, final ArrayList<TextListData> contribList) {
        this.contribList = contribList;
        this.activity = activity;
    }

    @Override
    public AboutAdapter.ContribViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.item_text, null);
        return new ContribViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final AboutAdapter.ContribViewHolder holder, int position) {
        holder.v.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));

        TextListData contrib = contribList.get(position);
        TextView title = (TextView) holder.v.findViewById(R.id.title);
        TextView subtitle = (TextView) holder.v.findViewById(R.id.subtitle);

        if (contrib.title != null) {
            title.setVisibility(View.VISIBLE);
            title.setText(contrib.title);
        } else title.setVisibility(View.GONE);

        if (contrib.subtitle != null) {
            subtitle.setVisibility(View.VISIBLE);
            subtitle.setText(contrib.subtitle);
        } else subtitle.setVisibility(View.GONE);

        if (contrib.image != null) {
            CustomImageView image = (CustomImageView) holder.v.findViewById(R.id.image);
            image.setVisibility(View.VISIBLE);
            image.setImageDrawable(new ColorDrawable(Color.parseColor("#bdbdbd")));

            Glide.with(activity).load(contribList.get(position).image).into(image);
        } else {
            holder.v.findViewById(R.id.image).setVisibility(View.GONE);
        }

        if (contrib.primary != null) {
            holder.v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, contribList.get(holder.getAdapterPosition()).primary));
                }
            });
            holder.v.setClickable(true);
        } else holder.v.setClickable(false);
    }

    @Override
    public int getItemCount() {
        return contribList.size();
    }

    public static class ContribViewHolder extends RecyclerView.ViewHolder {
        public View v;

        public ContribViewHolder(View v) {
            super(v);
            this.v = v;
        }
    }

    public static class TextListData implements Parcelable {

        public static final Creator<TextListData> CREATOR = new Creator<TextListData>() {
            public TextListData createFromParcel(Parcel in) {
                return new TextListData(in);
            }

            public TextListData[] newArray(int size) {
                return new TextListData[size];
            }
        };

        public String image, title, subtitle;
        public Uri primary;

        public TextListData(String image, String title, String subtitle, Uri primary) {
            this.image = image;
            this.title = title;
            this.subtitle = subtitle;
            this.primary = primary;
        }

        public TextListData(Parcel in) {
            ReadFromParcel(in);
        }

        private void ReadFromParcel(Parcel in) {
            image = in.readString();
            title = in.readString();
            subtitle = in.readString();
            primary = Uri.parse(in.readString());
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(image);
            out.writeString(title);
            out.writeString(subtitle);
            out.writeString(primary.toString());
        }

        @Override
        public int describeContents() {
            return 0;
        }
    }
}
