package com.refraginc.cinemovie;

import android.os.Parcel;
import android.os.Parcelable;

public class Connector implements Parcelable {
    public static final Parcelable.Creator<Connector> CREATOR = new Parcelable.Creator<Connector>() {
        @Override
        public Connector createFromParcel(Parcel source) {
            return new Connector( source );
        }

        @Override
        public Connector[] newArray(int size) {
            return new Connector[size];
        }
    };
    private String id;

    public Connector() {
    }

    protected Connector(Parcel in) {
        this.id = in.readString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString( this.id );
    }
}
