/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sf.biocapture.ws.access.pojo;

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Marcel
 */
@XmlRootElement(name = "demographic")
@XmlAccessorType(XmlAccessType.FIELD)
public class InputComponent implements Serializable {

    @XmlElement(name = "id")
    private Integer id;
    @XmlElement(name = "label")
    private String label;
    @XmlElement(name = "input_type")
    private InputTypeEnum inputTypeEnum;
    @XmlElement(name = "regex_pattern")
    private String regexPattern;
    @XmlElement(name = "description")
    private String description;
    @XmlElement(name = "schema")
    private String schema;
    @XmlElement(name = "data_source")
    private List<InputDataSource> inputDataSources;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public InputTypeEnum getInputTypeEnum() {
        return inputTypeEnum;
    }

    public void setInputTypeEnum(InputTypeEnum inputTypeEnum) {
        this.inputTypeEnum = inputTypeEnum;
    }

    public String getRegexPattern() {
        return regexPattern;
    }

    public void setRegexPattern(String regexPattern) {
        this.regexPattern = regexPattern;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public List<InputDataSource> getInputDataSources() {
        return inputDataSources;
    }

    public void setInputDataSources(List<InputDataSource> inputDataSources) {
        this.inputDataSources = inputDataSources;
    }

    
}
