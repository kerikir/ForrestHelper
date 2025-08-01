package com.example.myapplicationvoice.servers;

import com.example.myapplicationvoice.userinformation.NetworkUser;

import java.util.Objects;

public class GeopositionMessage
{
    public String ipAddress;
    public String firstName;
    public String lastName;
    public double latitude;
    public double longitude;


    public GeopositionMessage(String ipAddress, String firstName, String lastName,
                              double latitude, double longitude)
    {
        this.ipAddress = ipAddress;
        this.firstName = firstName;
        this.lastName = lastName;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    @Override
    public String toString() {
        return "CallMessage{" +
                "ipAddress='" + ipAddress + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        GeopositionMessage that = (GeopositionMessage) o;
        return ipAddress.equals(that.ipAddress);
    }


    @Override
    public int hashCode()
    {
        return Objects.hash(ipAddress);
    }
}
