package ListViewHelpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.carl.basketballshotlog2.R;

import java.util.ArrayList;
import java.util.Date;

import Helpers.DateConverter;

/**
 * Created by Carl on 6/25/2015.
 */
public class ShotAdapter extends ArrayAdapter<Shot> {
    Context context;
    ArrayList<Shot> shots;
    DateConverter dateConverter;
    public ShotAdapter(Context context, ArrayList<Shot> shots){
        super(context,0,shots);
        this.shots =shots;
        this.context = context;
        this.dateConverter = new DateConverter();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Shot shot = shots.get(position);//getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.layout_shot_item, parent, false);
            viewHolder.tv_made_miss = (TextView) convertView.findViewById(R.id.tv_miss_made);
            viewHolder.tv_percent = (TextView) convertView.findViewById(R.id.tv_percent);
            viewHolder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
            viewHolder.tv_made_miss.setText("Made "+shot.getMade() + " of "+ shot.getAttempted());
            viewHolder.tv_percent.setText(" => "+shot.getPercent()+"%");
            viewHolder.tv_date.setText(dateConverter.convertDateToText(shot.getDate()));

        // Return the completed view to render on screen
        return convertView;
    }

    static class ViewHolder
    {
        TextView tv_made_miss;
        TextView tv_percent;
        TextView tv_date;
    }
}
