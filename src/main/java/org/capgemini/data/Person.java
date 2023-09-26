package org.capgemini.data;

public class Person {
    private String vorname;
    private String name;
    private String mail;
    private boolean flagPkwWay;
    private boolean flagPkwTime;
    private boolean flagPTTime;

    public Person(String vorname, String name, String mail, boolean flagPkwWay, boolean flagPkwTime, boolean flagPTTime) {
        this.vorname = vorname;
        this.name = name;
        this.mail = mail;
        this.flagPkwWay = flagPkwWay;
        this.flagPkwTime = flagPkwTime;
        this.flagPTTime = flagPTTime;
    }

    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public boolean isFlagPkwWay() {
        return flagPkwWay;
    }

    public void setFlagPkwWay(boolean flagPkwWay) {
        this.flagPkwWay = flagPkwWay;
    }

    public boolean isFlagPkwTime() {
        return flagPkwTime;
    }

    public void setFlagPkwTime(boolean flagPkwTime) {
        this.flagPkwTime = flagPkwTime;
    }

    public boolean isFlagPTTime() {
        return flagPTTime;
    }

    public void setFlagPTTime(boolean flagPTTime) {
        this.flagPTTime = flagPTTime;
    }
}
