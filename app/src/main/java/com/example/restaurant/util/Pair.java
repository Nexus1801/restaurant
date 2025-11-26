package com.example.restaurant.util;

public class Pair {
    private int month;
    private double number;

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public double getNumber() {
        return number;
    }

    public void setNumber(double number) {
        this.number = number;
    }

    public Pair(int month, double number) {
        super();
        this.month = month;
        this.number = number;
    }
}
