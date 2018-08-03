package uk.co.novinet.web

import geb.Page

class LegacyThankYouPage extends Page {

    static url = "http://localhost:8383/legacyThankYou"

    static at = { title == "Loan Charge Action Group | Thank you for submitting the membership form" }

    static content = {

    }
}
