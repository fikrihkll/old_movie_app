package com.refraginc.cinemovie.livechat;

import android.os.Parcel;
import android.os.Parcelable;

public class ChatData implements Parcelable {
    public static final Parcelable.Creator<ChatData> CREATOR = new Parcelable.Creator<ChatData>() {
        @Override
        public ChatData createFromParcel(Parcel source) {
            return new ChatData( source );
        }

        @Override
        public ChatData[] newArray(int size) {
            return new ChatData[size];
        }
    };
    String from;
    String message;
    String date;
    String myId;

    public ChatData() {
    }

    protected ChatData(Parcel in) {
        this.from = in.readString();
        this.message = in.readString();
        this.date = in.readString();
        this.myId = in.readString();
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMyId() {
        return myId;
    }

    public void setMyId(String myId) {
        this.myId = myId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString( this.from );
        dest.writeString( this.message );
        dest.writeString( this.date );
        dest.writeString( this.myId );
    }
}
