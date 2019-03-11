package com.example.labourondemand;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class ServicesFinal implements Serializable {

    private String skill, customerUID, description, feedback, serviceId, status;
    private Long numOfLabourers, customerAmount;
    private Double rating;
    private HashMap<String, Long> labourerResponses;
    private ArrayList<String> images, selectedLabourerUID;
    private Customer customer;
    private ArrayList<Labourer> labourers;
    private GeoPoint destination;
    private String startTime, endTime;

    public ServicesFinal() {
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public String getCustomerUID() {
        return customerUID;
    }

    public void setCustomerUID(String customerUID) {
        this.customerUID = customerUID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getNumOfLabourers() {
        return numOfLabourers;
    }

    public void setNumOfLabourers(Long numOfLabourers) {
        this.numOfLabourers = numOfLabourers;
    }

    public Long getCustomerAmount() {
        return customerAmount;
    }

    public void setCustomerAmount(Long customerAmount) {
        this.customerAmount = customerAmount;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public HashMap<String, Long> getLabourerResponses() {
        return labourerResponses;
    }

    public void setLabourerResponses(HashMap<String, Long> labourerResponses) {
        this.labourerResponses = labourerResponses;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public ArrayList<String> getSelectedLabourerUID() {
        return selectedLabourerUID;
    }

    public void setSelectedLabourerUID(ArrayList<String> selectedLabourerUID) {
        this.selectedLabourerUID = selectedLabourerUID;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public ArrayList<Labourer> getLabourers() {
        return labourers;
    }

    public void setLabourers(ArrayList<Labourer> labourers) {
        this.labourers = labourers;
    }

    public GeoPoint getDestination() {
        return destination;
    }

    public void setDestination(GeoPoint destination) {
        this.destination = destination;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
