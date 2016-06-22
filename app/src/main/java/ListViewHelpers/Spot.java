package ListViewHelpers;

/**
 * Created by Carl on 6/27/2015.
 */
public class Spot {
    public Spot( int spid,String spot) {
        this.spot = spot;
        this.spid = spid;
    }

    public String getSpot() {
        return spot;
    }

    public void setSpot(String spot) {
        this.spot = spot;
    }

    public int getSpid() {
        return spid;
    }

    public void setSpid(int spid) {
        this.spid = spid;
    }

    private String spot;
    private int spid;
}
