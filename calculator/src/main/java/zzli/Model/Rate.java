package zzli.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.data.pref.Preferences;



/**
 * Created by catfish on 2016-12-16.
 */
public class Rate implements Rating, Preference {

    public String uuid;
    public long userId;
    public long itemId;
    public double value;
    public long timestamp;
    public Rate(long userId, long itemId, double value, long timestamp) {
        this.userId = userId;
        this.itemId = itemId;
        this.value = value;
        this.timestamp = timestamp;
        this.uuid=userId+"-"+itemId+"-"+value;
    }

    public Rate(){}

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public long getItemId() {
        return itemId;
    }

    @JsonIgnore
    public org.grouplens.lenskit.data.pref.Preference getPreference() {
        return this;
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    public double getValue() {
        return value;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public boolean equals(Object o) {
        return o instanceof Rating? Ratings.equals(this, (Rating)o):(o instanceof Preference? Preferences.equals(this, (Preference)o):false);
    }

    public int hashCode() {
        return Ratings.hashRating(this);
    }

    @Override
    public String toString() {
        return userId+"-"+itemId+"-"+value;
    }
}
