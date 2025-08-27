package com.example.rentify_roomrentmanagement;

public class Rooms {

    public String room_id, room_name, tenant_name, tenant_phone, thumb_tenant_url, tenant_id;
    public int room_rent, room_no;
    public boolean is_occupied, is_rent_paid;

    public Rooms(){

    }

    public Rooms(String room_id, String room_name, int room_no, String tenant_id, String tenant_name, String tenant_phone, String thumb_tenant_url,
                 int room_rent, boolean is_occupied, boolean is_rent_paid) {
        this.room_id = room_id;
        this.room_name = room_name;
        this.tenant_name = tenant_name;
        this.tenant_phone = tenant_phone;
        this.thumb_tenant_url = thumb_tenant_url;
        this.room_rent = room_rent;
        this.tenant_id = tenant_id;
        this.is_occupied = is_occupied;
        this.is_rent_paid = is_rent_paid;
        this.room_no = room_no;
    }

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    public String getRoom_name() {
        return room_name;
    }

    public void setRoom_name(String room_name) {
        this.room_name = room_name;
    }

    public int getRoom_no() {
        return room_no;
    }

    public void setRoom_no(int room_no) {
        this.room_no = room_no;
    }

    public String getTenant_id() {
        return tenant_id;
    }

    public void setTenant_id(String tenant_id) {
        this.tenant_id = tenant_id;
    }

    public String getTenant_name() {
        return tenant_name;
    }

    public void setTenant_name(String tenant_name) {
        this.tenant_name = tenant_name;
    }

    public String getTenant_phone() {
        return tenant_phone;
    }

    public void setTenant_phone(String tenant_phone) {
        this.tenant_phone = tenant_phone;
    }

    public String getThumb_tenant_url() {
        return thumb_tenant_url;
    }

    public void setThumb_tenant_url(String thumb_tenant_url) {
        this.thumb_tenant_url = thumb_tenant_url;
    }

    public int getRoom_rent() {
        return room_rent;
    }

    public void setRoom_rent(int room_rent) {
        this.room_rent = room_rent;
    }

    public boolean isIs_occupied() {
        return is_occupied;
    }

    public void setIs_occupied(boolean is_occupied) {
        this.is_occupied = is_occupied;
    }

    public boolean isIs_rent_paid() {
        return is_rent_paid;
    }

    public void setIs_rent_paid(boolean is_rent_paid) {
        this.is_rent_paid = is_rent_paid;
    }
}
