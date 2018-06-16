package uk.co.novinet.service;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

public class Member {
    private Long id;
    private String username;
    private String name;
    private String emailAddress;
    private String mpName;
    private String schemes;
    private Boolean mpEngaged = false;
    private Boolean mpSympathetic = false;
    private String mpConstituency;
    private String mpParty;
    private String industry;
    private String token;
    private Boolean completedMembershipForm;
    private String howDidYouHearAboutLcag;

    public Member() {
    }

    public Member(
            Long id,
            String username,
            String name,
            String emailAddress,
            String mpName,
            String schemes,
            Boolean mpEngaged,
            Boolean mpSympathetic,
            String mpConstituency,
            String mpParty,
            String industry,
            String token,
            Boolean completedMembershipForm,
            String howDidYouHearAboutLcag) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.emailAddress = emailAddress;
        this.mpName = mpName;
        this.schemes = schemes;
        this.mpEngaged = mpEngaged;
        this.mpSympathetic = mpSympathetic;
        this.mpConstituency = mpConstituency;
        this.mpParty = mpParty;
        this.industry = industry;
        this.token = token;
        this.completedMembershipForm = completedMembershipForm;
        this.howDidYouHearAboutLcag = howDidYouHearAboutLcag;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getMpName() {
        return mpName;
    }

    public void setMpName(String mpName) {
        this.mpName = mpName;
    }

    public String getSchemes() {
        return schemes;
    }

    public void setSchemes(String schemes) {
        this.schemes = schemes;
    }

    public Boolean getMpEngaged() {
        return mpEngaged;
    }

    public void setMpEngaged(Boolean mpEngaged) {
        this.mpEngaged = mpEngaged;
    }

    public Boolean getMpSympathetic() {
        return mpSympathetic;
    }

    public void setMpSympathetic(Boolean mpSympathetic) {
        this.mpSympathetic = mpSympathetic;
    }

    public String getMpConstituency() {
        return mpConstituency;
    }

    public void setMpConstituency(String mpConstituency) {
        this.mpConstituency = mpConstituency;
    }

    public String getMpParty() {
        return mpParty;
    }

    public void setMpParty(String mpParty) {
        this.mpParty = mpParty;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return reflectionToString(this);
    }

    public Boolean hasCompletedMembershipForm() {
        return completedMembershipForm;
    }

    public void setCompletedMembershipForm(Boolean completedMembershipForm) {
        this.completedMembershipForm = completedMembershipForm;
    }

    public String getHowDidYouHearAboutLcag() {
        return howDidYouHearAboutLcag;
    }

    public void setHowDidYouHearAboutLcag(String howDidYouHearAboutLcag) {
        this.howDidYouHearAboutLcag = howDidYouHearAboutLcag;
    }
}
