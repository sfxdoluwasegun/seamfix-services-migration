/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sf.biocapture.ws.access.pojo;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Marcel
 */
@XmlRootElement(name = "data_source")
@XmlAccessorType(XmlAccessType.FIELD)
public class InputDataSource implements Serializable {

    @XmlElement(name = "id")
    private Integer id;
    @XmlElement(name = "value")
    private String value;
    @XmlElement(name = "data_fk")
    private Integer dataFk;
    @XmlElement(name = "input_fk")
    private Integer inputFk;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getDataFk() {
        return dataFk;
    }

    public void setDataFk(Integer dataFk) {
        this.dataFk = dataFk;
    }

    public Integer getInputFk() {
        return inputFk;
    }

    public void setInputFk(Integer inputFk) {
        this.inputFk = inputFk;
    }

}
