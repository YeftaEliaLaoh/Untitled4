package com.example.myapplication8.models;

public class Cell
{
    private String cellReference;
    private int psc;
    private int cellId;
    private int mnc;
    private int mcc;
    private int lac;
    private String radioType;

    public String getCellReference()
    {
        return cellReference;
    }

    public void setCellReference(String cellReference)
    {
        this.cellReference = cellReference;
    }

    public int getPsc()
    {
        return psc;
    }

    public void setPsc(int psc)
    {
        this.psc = psc;
    }

    public int getCellId()
    {
        return cellId;
    }

    public void setCellId(int cellId)
    {
        this.cellId = cellId;
    }

    public int getMnc()
    {
        return mnc;
    }

    public void setMnc(int mnc)
    {
        this.mnc = mnc;
    }

    public int getMcc()
    {
        return mcc;
    }

    public void setMcc(int mcc)
    {
        this.mcc = mcc;
    }

    public int getLac()
    {
        return lac;
    }

    public void setLac(int lac)
    {
        this.lac = lac;
    }

    public String getRadioType()
    {
        return radioType;
    }

    public void setRadioType(String radioType)
    {
        this.radioType = radioType;
    }

}
