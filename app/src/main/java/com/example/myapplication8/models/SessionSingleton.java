package com.example.myapplication8.models;


import java.util.HashMap;

public class SessionSingleton
{
    private static SessionSingleton instance;
    private Session session;
    private HashMap wifiHashMap;
    private HashMap cellHashMap;

    public static SessionSingleton getInstance(){
        if(null == instance)
        {
            instance = new SessionSingleton();
        }
        return instance;
    }

    public Session getSession(){
        return session;
    }

    public void setSession(Session session){
        this.session = session;
    }

    /**
     * Get cell hashmap
     *
     * @return cellHashMap
     */
    public HashMap getCellHashMap(){
        return cellHashMap;
    }

    public void setCellHashMap(HashMap cellHashMap){
        this.cellHashMap = cellHashMap;
    }

    /**
     * Get wifi hashmap
     *
     * @return wifiHashMap
     */
    public HashMap getWifiHashMap(){
        return wifiHashMap;
    }

    public void setWifiHashMap(HashMap wifiHashMap){
        this.wifiHashMap = wifiHashMap;
    }

}
