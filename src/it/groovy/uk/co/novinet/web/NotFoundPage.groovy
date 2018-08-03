package uk.co.novinet.web

import geb.Page

class NotFoundPage extends Page {

    static url = "http://localhost:8383"

    static at = { title == "Loan Charge Action Group | Token Not Found" }

    static content = {

    }
}
