package com.example.rentify_roomrentmanagement;

public class Rents {

    public String rent_date, rent_time, payment_mode, tenant_name, rent_timestamp;
    public int rent_amount;

    public Rents(){

    }

    public Rents(String rent_date, String rent_timestamp, String rent_time, String payment_mode, String tenant_name, int rent_amount) {
        this.rent_date = rent_date;
        this.rent_time = rent_time;
        this.payment_mode = payment_mode;
        this.tenant_name = tenant_name;
        this.rent_amount = rent_amount;
        this.rent_timestamp = rent_timestamp;
    }

    public String getRent_Timestamp() {
        return rent_timestamp;
    }
    public String getRent_date() {
        return rent_date;
    }

    public void setRent_date(String rent_date) {
        this.rent_date = rent_date;
    }

    public String getRent_time() {
        return rent_time;
    }

    public void setRent_time(String rent_time) {
        this.rent_time = rent_time;
    }

    public String getPayment_mode() {
        return payment_mode;
    }

    public void setPayment_mode(String payment_mode) {
        this.payment_mode = payment_mode;
    }

    public String getTenant_name() {
        return tenant_name;
    }

    public void setTenant_name(String tenant_name) {
        this.tenant_name = tenant_name;
    }

    public int getRent_amount() {
        return rent_amount;
    }

    public void setRent_amount(int rent_amount) {
        this.rent_amount = rent_amount;
    }
}
