package com.example.gastrak;

// Jacob Pauls
// 300273666
// GasStationObject.java

import android.util.Log;

import java.sql.Time;

public class GasStationObject {

    // hold name
    private static final String TAG = "GasStationObject";

    // fields for the gas station object
    private String Id;
    private String Name;
    private String Address;
    private double Latitude;
    private double Longitude;
    private String PhoneNumber;
    private String PlusCode;
    private double Rating;
    private String Uri;
    private double UserRatingsTotal;
    private double Price;
    private String Time;

    public GasStationObject() {
        Log.d(TAG, "GasStationObject created.");
    }

    public String getId() {
        return Id;
    }

    public String getName() {
        return Name;
    }

    public String getAddress() {
        return Address;
    }

    public double getLatitude() {
        return Latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public String getPlusCode() {
        return PlusCode;
    }

    public double getRating() {
        return Rating;
    }

    public String getUri() {
        return Uri;
    }

    public double getUserRatingsTotal() {
        return UserRatingsTotal;
    }

    public double getPrice() {
        return Price;
    }

    public String getTime() {
        return Time;
    }

    public void setId(String id) {
        Id = id;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public void setPlusCode(String plusCode) {
        PlusCode = plusCode;
    }

    public void setRating(double rating) {
        Rating = rating;
    }

    public void setUri(String uri) {
        Uri = uri;
    }

    public void setUserRatingsTotal(double userRatingsTotal) {
        UserRatingsTotal = userRatingsTotal;
    }

    public void setPrice(double price) {
        Price = price;
    }

    public void setTime(String time) {
        Time = time;
    }

    @Override
    public String toString() {
        return "GasStationObject{" +
                "Id='" + Id + '\'' +
                ", Name='" + Name + '\'' +
                ", Address='" + Address + '\'' +
                ", Latitude=" + Latitude +
                ", Longitude=" + Longitude +
                ", PhoneNumber='" + PhoneNumber + '\'' +
                ", PlusCode='" + PlusCode + '\'' +
                ", Rating=" + Rating +
                ", Uri='" + Uri + '\'' +
                ", UserRatingsTotal=" + UserRatingsTotal +
                ", Price=" + Price +
                ", Time='" + Time + '\'' +
                '}';
    }

}
