package uk.co.novinet.web

import geb.Page

class MembershipFormPage extends Page {

    static url = "http://localhost:8383"

    static at = { title == "Loan Charge Action Group | Membership Form" }

    static content = {
        nameInput { $("#name") }
        emailAddressInput { $("#emailAddress") }
        mpNameInput { $("#mpName") }
        mpPartyInput { $("#mpParty") }
        mpConstituencyInput { $("#mpConstituency") }
        mpEngagedInput { $("#mpEngaged") }
        mpSympatheticInput { $("#mpSympathetic") }
        schemesInput { $("#schemes") }
        industryInput { $("#industry") }
        identificationInput { $("#identification") }
        proofOfSchemeInvolvementInput { $("#proofOfSchemeInvolvement") }
    }
}
