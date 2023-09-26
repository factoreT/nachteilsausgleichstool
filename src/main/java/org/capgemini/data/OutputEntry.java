package org.capgemini.data;

public class OutputEntry {
    private String id;

    private String name;

    private String vorname;

    private double sum;

    private String submitDate;

    private String filename;

    public OutputEntry(String id, String name, String firstname, double sum, String submitDate, String filename) {
        this.id = id;
        this.name = name;
        this.vorname = firstname;
        this.sum = sum;
        this.submitDate = submitDate;
        this.filename = filename;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public String getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(String submitDate) {
        this.submitDate = submitDate;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
