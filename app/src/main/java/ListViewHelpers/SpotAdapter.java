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

/**
 * Created by Carl on 6/27/2015.
 */
public class SpotAdapter extends ArrayAdapter<Spot> {

    Context context;
    ArrayList<Spot> spots;
    public SpotAdapter(Context context, ArrayList<Spot> spots){
        super(context,0,spots);
        this.spots =spots;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Spot spot = spots.get(position);//getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.layout_spot_item, parent, false);
            viewHolder.tv_spot_name = (TextView) convertView.findViewById(R.id.tv_spot_name);
            viewHolder.img_forward_arrow = (ImageView) convertView.findViewById(R.id.img_forward_arrow);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tv_spot_name.setText(spot.getSpot());
        viewHolder.img_forward_arrow.setImageResource(R.drawable.forward_50);

        // Return the completed view to render on screen
        return convertView;
    }

    static class ViewHolder
    {
        TextView tv_spot_name;
        TextView tv_forward_arrow;
        ImageView img_forward_arrow;
    }
    @Override
    public int getCount() {
        return  spots.size();
    }

}
