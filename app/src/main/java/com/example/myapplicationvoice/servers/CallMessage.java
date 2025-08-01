package com.example.myapplicationvoice.servers;

import com.example.myapplicationvoice.userinformation.NetworkUser;

public class CallMessage
{
    public String ipAddress;
    public String firstName;
    public String lastName;
    public ActionType actionType;

    public CallMessage(ActionType actionType, String ipAddress, String firstName, String lastName)
    {
        this.actionType = actionType;
        this.ipAddress = ipAddress;
        this.firstName = firstName;
        this.lastName = lastName;
    }


    @Override
    public String toString() {
        return "CallMessage{" +
                "actionType=" + actionType +
                ", ipAddress='" + ipAddress + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
