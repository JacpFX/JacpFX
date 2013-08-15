/*
 * Copyright (C) 2010,2011.
 * AHCP Project (http://code.google.com/p/jacp)
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.jacp.demo.entity;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

/**
 * @author Andy Moncsek, Patrick Symmangk
 *
 */
public class ContactDTO {
    private String parentName;
    private int amount;
    private ObservableList<Contact> contacts = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
    private List<XYChart.Data<String, Number>> seriesOneData;
    private List<XYChart.Data<String, Number>> seriesTwoData;
    private List<XYChart.Data<String, Number>> seriesThreeData;
    private List<XYChart.Data<String, Number>> seriesFourData;

    public ContactDTO(final String parentName, final int amount) {
        this.parentName = parentName;
        this.amount = amount;
    }

    public ContactDTO() {
        // TODO Auto-generated constructor stub
    }

    public int getAmount() {
        return this.amount;
    }

    public void setAmount(final int amount) {
        this.amount = amount;
    }

    public String getParentName() {
        return this.parentName;
    }

    public void setParentName(final String parentName) {
        this.parentName = parentName;
    }

    public ObservableList<Contact> getContacts() {
        return this.contacts;
    }

    public void setContacts(final ObservableList<Contact> contacts) {
        this.contacts = contacts;
    }

    public List<XYChart.Data<String, Number>> getSeriesOneData() {
        return this.seriesOneData;
    }

    public void setSeriesOneData(final List<XYChart.Data<String, Number>> seriesOneData) {
        this.seriesOneData = seriesOneData;
    }

    public List<XYChart.Data<String, Number>> getSeriesTwoData() {
        return this.seriesTwoData;
    }

    public void setSeriesTwoData(final List<XYChart.Data<String, Number>> seriesTwoData) {
        this.seriesTwoData = seriesTwoData;
    }

    public List<XYChart.Data<String, Number>> getSeriesThreeData() {
        return this.seriesThreeData;
    }

    public void setSeriesThreeData(final List<XYChart.Data<String, Number>> seriesThreeData) {
        this.seriesThreeData = seriesThreeData;
    }

    public List<XYChart.Data<String, Number>> getSeriesFourData() {
        return this.seriesFourData;
    }

    public void setSeriesFourData(final List<XYChart.Data<String, Number>> seriesFourData) {
        this.seriesFourData = seriesFourData;
    }

    @Override
    public String toString() {
        return "ContactDTO [getAmount()=" + getAmount() + ", getParentName()=" + getParentName() + "]";
    }

}
