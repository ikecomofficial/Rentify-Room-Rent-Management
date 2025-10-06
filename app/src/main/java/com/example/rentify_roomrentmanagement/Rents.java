package com.example.rentify_roomrentmanagement;

public class Rents {

    public String payment_mode, tenant_name, rent_timestamp;
    public int rent_amount;

    public Rents(){

    }

    public Rents(String rent_timestamp, String payment_mode, String tenant_name, int rent_amount) {
        this.payment_mode = payment_mode;
        this.tenant_name = tenant_name;
        this.rent_amount = rent_amount;
        this.rent_timestamp = rent_timestamp;
    }

    public String getRent_timestamp() {
        return rent_timestamp;
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
