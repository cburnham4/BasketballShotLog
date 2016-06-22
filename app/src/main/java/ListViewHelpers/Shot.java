package ListViewHelpers;

import java.text.DecimalFormat;

/**
 * Created by Carl on 6/25/2015.
 */
public class Shot {
    public Shot(int made, int attempted, String date, int sid) {
        this.made = made;
        this.attempted = attempted;
        double p = ((made+0.0)/attempted)*100;
        DecimalFormat df = new DecimalFormat("#.00");
        this.percent = df.format(p);
        this.date = date;
        this.sid = sid;
    }

    private int sid;
    private int made;
    private int attempted;
    private String percent;
    private String date;

    public int getMade() {
        return made;
    }

    public void setMade(int made) {
        this.made = made;
    }

    public int getAttempted() {
        return attempted;
    }

    public void setAttempted(int attempted) {
        this.attempted = attempted;
    }

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getSid(){return sid;}




}
