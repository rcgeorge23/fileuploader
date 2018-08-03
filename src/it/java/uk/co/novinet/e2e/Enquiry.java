package uk.co.novinet.e2e;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.Instant;

public class Enquiry {
    private int id;
    private String emailAddress;
    private String name;
    private String mpName;
    private String mpParty;
    private String mpConstituency;
    private Boolean mpEngaged;
    private Boolean mpSympathetic;
    private String schemes;
    private String industry;
    private String howDidYouHearAboutLcag;
    private Boolean memberOfBigGroup;
    private String bigGroupUsername;
    private Boolean hasBeenProcessed;
    private Instant dateCreated;

    public Enquiry(int id, String emailAddress, String name, String mpName, String mpParty, String mpConstituency, Boolean mpEngaged, Boolean mpSympathetic, String schemes, String industry, String howDidYouHearAboutLcag, Boolean memberOfBigGroup, String bigGroupUsername, Boolean hasBeenProcessed, Instant dateCreated) {
        this.id = id;
        this.emailAddress = emailAddress;
        this.name = name;
        this.mpName = mpName;
        this.mpParty = mpParty;
        this.mpConstituency = mpConstituency;
        this.mpEngaged = mpEngaged;
        this.mpSympathetic = mpSympathetic;
        this.schemes = schemes;
        this.industry = industry;
        this.howDidYouHearAboutLcag = howDidYouHearAboutLcag;
        this.memberOfBigGroup = memberOfBigGroup;
        this.bigGroupUsername = bigGroupUsername;
        this.hasBeenProcessed = hasBeenProcessed;
        this.dateCreated = dateCreated;
    }

    public int getId() {
        return id;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getName() {
        return name;
    }

    public String getMpName() {
        return mpName;
    }

    public String getMpParty() {
        return mpParty;
    }

    public String getMpConstituency() {
        return mpConstituency;
    }

    public Boolean getMpEngaged() {
        return mpEngaged;
    }

    public Boolean getMpSympathetic() {
        return mpSympathetic;
    }

    public String getSchemes() {
        return schemes;
    }

    public String getIndustry() {
        return industry;
    }

    public String getHowDidYouHearAboutLcag() {
        return howDidYouHearAboutLcag;
    }

    public Boolean getMemberOfBigGroup() {
        return memberOfBigGroup;
    }

    public String getBigGroupUsername() {
        return bigGroupUsername;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public Boolean getHasBeenProcessed() {
        return hasBeenProcessed;
    }

    public Instant getDateCreated() {
        return dateCreated;
    }
}
