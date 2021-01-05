package com.refraginc.cinemovie.search;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchData implements Parcelable {
    public static final Parcelable.Creator<SearchData> CREATOR = new Parcelable.Creator<SearchData>() {
        @Override
        public SearchData createFromParcel(Parcel source) {
            return new SearchData( source );
        }

        @Override
        public SearchData[] newArray(int size) {
            return new SearchData[size];
        }
    };
    String title;
    String imgPath;
    String rating;
    String date;
    String id;
    String type;

    public SearchData() {
    }

    protected SearchData(Parcel in) {
        this.title = in.readString();
        this.imgPath = in.readString();
        this.rating = in.readString();
        this.date = in.readString();
        this.id = in.readString();
        this.type = in.readString();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString( this.title );
        dest.writeString( this.imgPath );
        dest.writeString( this.rating );
        dest.writeString( this.date );
        dest.writeString( this.id );
        dest.writeString( this.type );
    }
}
