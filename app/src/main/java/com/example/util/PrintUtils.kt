package com.example.util

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.local.entity.ContainerEntity
import com.example.model.RentCalculation

object PrintUtils {

    fun shareSlipText(context: Context, text: String, title: String = "Share Gate Pass / Slip") {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        context.startActivity(shareIntent)
    }

    fun generateGateOutPassText(container: ContainerEntity, calc: RentCalculation, companyName: String = "CONTAINER DEPOT & LOGISTICS"): String {
        return buildString {
            appendLine("==========================================")
            appendLine("        $companyName")
            appendLine("         OFFICIAL GATE OUT PASS")
            appendLine("==========================================")
            appendLine("Gate Pass No: ${container.gatePassNo}")
            appendLine("Date & Time : ${DateUtils.formatDateTime(container.gateOutDate ?: System.currentTimeMillis())}")
            appendLine("Status      : AUTHORIZED & CLEARED")
            appendLine("------------------------------------------")
            appendLine("CONTAINER DETAILS:")
            appendLine("Container No: ${container.containerNo}")
            appendLine("Size / Type : ${container.size}")
            appendLine("Line / Owner: ${container.line}")
            appendLine("Condition   : ${container.condition}")
            appendLine("Seal No     : ${container.sealNo.ifBlank { "N/A" }}")
            appendLine("Yard Bay    : ${container.yardBay}")
            appendLine("------------------------------------------")
            appendLine("SHIPPING & CARGO:")
            appendLine("Booking / BL: ${container.bookingNo.ifBlank { "N/A" }}")
            appendLine("NOC No      : ${container.nocNo.ifBlank { "N/A" }}")
            appendLine("Shipper     : ${container.shipper.ifBlank { "N/A" }}")
            appendLine("Consignee   : ${container.consignee.ifBlank { "N/A" }}")
            appendLine("------------------------------------------")
            appendLine("TRANSPORT & DRIVER:")
            appendLine("Transporter : ${container.transporter}")
            appendLine("Truck / Reg : ${container.truckNo}")
            appendLine("Driver Name : ${container.driverName}")
            appendLine("CNIC No     : ${container.driverCnic}")
            appendLine("Cell Phone  : ${container.driverCell}")
            appendLine("------------------------------------------")
            appendLine("RENT & DURATION BREAKDOWN:")
            appendLine("Gate In Date: ${DateUtils.formatDateTime(container.gateInDate)}")
            appendLine("Gate Out Dt : ${DateUtils.formatDateTime(container.gateOutDate ?: System.currentTimeMillis())}")
            appendLine("Total Days  : ${calc.totalDays} Days (${calc.months} Mo, ${calc.remainderDays} Days)")
            appendLine("Free Days   : ${calc.freeDays} Days")
            appendLine("Billable    : ${calc.billableDays} Days")
            appendLine("Storage Rent: ${DateUtils.formatCurrency(calc.storageRent)}")
            appendLine("Handling/LOLO: ${DateUtils.formatCurrency(calc.handlingCharges)}")
            appendLine("Repair/Clean: ${DateUtils.formatCurrency(calc.repairCharges)}")
            appendLine("Other/NOC   : ${DateUtils.formatCurrency(calc.otherCharges)}")
            appendLine("------------------------------------------")
            appendLine("TOTAL BILLED: ${DateUtils.formatCurrency(calc.grossTotal)}")
            appendLine("TOTAL PAID  : ${DateUtils.formatCurrency(calc.paidAmount)}")
            appendLine("NET BALANCE : ${DateUtils.formatCurrency(calc.pendingAmount)}")
            appendLine("CLEARANCE   : ${if (calc.pendingAmount <= 0.01) "FULLY CLEARED (PAID)" else "PENDING DUES"}")
            appendLine("------------------------------------------")
            appendLine("Cleared By  : ${container.clearedBy.ifBlank { "Depot Security Incharge" }}")
            appendLine("Authorized  : [ VERIFIED & SIGNED ]")
            appendLine("==========================================")
        }
    }

    fun generateGateInSlipText(container: ContainerEntity, companyName: String = "CONTAINER DEPOT & LOGISTICS"): String {
        return buildString {
            appendLine("==========================================")
            appendLine("        $companyName")
            appendLine("     EQUIPMENT INTERCHANGE RECEIPT (EIR)")
            appendLine("               GATE IN SLIP")
            appendLine("==========================================")
            appendLine("EIR / Slip No : ${container.eirNo}")
            appendLine("Gate In Date  : ${DateUtils.formatDateTime(container.gateInDate)}")
            appendLine("Stack Slot    : ${container.yardBay}")
            appendLine("------------------------------------------")
            appendLine("CONTAINER DATA:")
            appendLine("Container No  : ${container.containerNo}")
            appendLine("Size / Type   : ${container.size}")
            appendLine("Shipping Line : ${container.line}")
            appendLine("Condition     : ${container.condition}")
            appendLine("Seal No       : ${container.sealNo.ifBlank { "N/A" }}")
            appendLine("------------------------------------------")
            appendLine("REFERENCES & PARTIES:")
            appendLine("Booking / BL  : ${container.bookingNo.ifBlank { "N/A" }}")
            appendLine("NOC Number    : ${container.nocNo.ifBlank { "N/A" }}")
            appendLine("Shipper       : ${container.shipper.ifBlank { "N/A" }}")
            appendLine("Consignee     : ${container.consignee.ifBlank { "N/A" }}")
            appendLine("------------------------------------------")
            appendLine("VEHICLE & DRIVER VERIFICATION:")
            appendLine("Transporter   : ${container.transporter}")
            appendLine("Vehicle / No  : ${container.truckNo}")
            appendLine("Driver Name   : ${container.driverName}")
            appendLine("Driver CNIC   : ${container.driverCnic}")
            appendLine("Driver Cell   : ${container.driverCell}")
            appendLine("------------------------------------------")
            appendLine("RENTAL TARIFF SETUP:")
            appendLine("Rate Type     : ${container.rateType}")
            appendLine("Daily Rate    : ${DateUtils.formatCurrency(container.dailyRate)}/day")
            appendLine("Monthly Rate  : ${DateUtils.formatCurrency(container.monthlyRate)}/month")
            appendLine("Free Days     : ${container.freeDays} Days")
            appendLine("Handling/LOLO : ${DateUtils.formatCurrency(container.handlingCharges)}")
            appendLine("Initial Paid  : ${DateUtils.formatCurrency(container.paidAmount)}")
            appendLine("------------------------------------------")
            appendLine("Inspector     : Yard Operations Desk")
            appendLine("Driver Sign   : ______________")
            appendLine("Officer Sign  : ______________")
            appendLine("==========================================")
        }
    }

    fun generateHtmlSlip(container: ContainerEntity, calc: RentCalculation, isGateOut: Boolean, companyName: String): String {
        val title = if (isGateOut) "CONTAINER GATE OUT PASS" else "GATE IN RECEIPT / EIR"
        val slipNo = if (isGateOut) container.gatePassNo else container.eirNo
        val dateStr = DateUtils.formatDateTime(if (isGateOut) (container.gateOutDate ?: System.currentTimeMillis()) else container.gateInDate)

        return """
            <!DOCTYPE html>
            <html>
            <head>
            <meta charset="utf-8">
            <style>
                body { font-family: 'Courier New', monospace, sans-serif; margin: 20px; color: #111; }
                .header { text-align: center; border-bottom: 2px solid #000; padding-bottom: 10px; margin-bottom: 15px; }
                .company { font-size: 18px; font-weight: bold; }
                .title { font-size: 16px; font-weight: bold; background: #eee; padding: 4px; margin-top: 5px; }
                .section { margin-bottom: 12px; border-bottom: 1px dashed #777; padding-bottom: 8px; }
                .sec-title { font-weight: bold; text-decoration: underline; margin-bottom: 4px; font-size: 13px; }
                .row { display: flex; justify-content: space-between; margin-bottom: 3px; font-size: 12px; }
                .label { font-weight: bold; }
                .value { text-align: right; }
                .badge { display: inline-block; padding: 4px 8px; font-weight: bold; border: 2px solid #000; margin-top: 10px; }
                .footer { margin-top: 25px; display: flex; justify-content: space-between; font-size: 11px; }
                .sign-box { border-top: 1px solid #000; width: 140px; text-align: center; padding-top: 5px; }
            </style>
            </head>
            <body>
                <div class="header">
                    <div class="company">$companyName</div>
                    <div>Container Depot & Logistics Terminal</div>
                    <div class="title">$title</div>
                    <div style="font-size: 12px; margin-top: 5px;">Doc Ref: $slipNo | Date: $dateStr</div>
                </div>

                <div class="section">
                    <div class="sec-title">1. CONTAINER INFORMATION</div>
                    <div class="row"><span class="label">Container No:</span><span class="value" style="font-size: 14px; font-weight: bold;">${container.containerNo}</span></div>
                    <div class="row"><span class="label">Size / Type:</span><span class="value">${container.size}</span></div>
                    <div class="row"><span class="label">Shipping Line:</span><span class="value">${container.line}</span></div>
                    <div class="row"><span class="label">Condition:</span><span class="value">${container.condition}</span></div>
                    <div class="row"><span class="label">Seal No / Yard Bay:</span><span class="value">${container.sealNo.ifBlank { "N/A" }} / ${container.yardBay}</span></div>
                </div>

                <div class="section">
                    <div class="sec-title">2. SHIPPER, CONSIGNEE & BOOKING</div>
                    <div class="row"><span class="label">Booking / BL No:</span><span class="value">${container.bookingNo.ifBlank { "N/A" }}</span></div>
                    <div class="row"><span class="label">NOC Certificate No:</span><span class="value">${container.nocNo.ifBlank { "N/A" }}</span></div>
                    <div class="row"><span class="label">Shipper (Sender):</span><span class="value">${container.shipper.ifBlank { "N/A" }}</span></div>
                    <div class="row"><span class="label">Consignee (Receiver):</span><span class="value">${container.consignee.ifBlank { "N/A" }}</span></div>
                </div>

                <div class="section">
                    <div class="sec-title">3. TRANSPORTER & DRIVER DETAILS</div>
                    <div class="row"><span class="label">Transporter:</span><span class="value">${container.transporter}</span></div>
                    <div class="row"><span class="label">Truck / Trailer No:</span><span class="value">${container.truckNo}</span></div>
                    <div class="row"><span class="label">Driver Name:</span><span class="value">${container.driverName}</span></div>
                    <div class="row"><span class="label">Driver CNIC:</span><span class="value">${container.driverCnic}</span></div>
                    <div class="row"><span class="label">Cell / Phone:</span><span class="value">${container.driverCell}</span></div>
                </div>

                <div class="section">
                    <div class="sec-title">4. RENT & DURATION CALCULATIONS</div>
                    <div class="row"><span class="label">Gate In Date:</span><span class="value">${DateUtils.formatDateTime(container.gateInDate)}</span></div>
                    ${if (isGateOut) "<div class=\"row\"><span class=\"label\">Gate Out Date:</span><span class=\"value\">" + DateUtils.formatDateTime(container.gateOutDate ?: System.currentTimeMillis()) + "</span></div>" else ""}
                    <div class="row"><span class="label">Total Elapsed Duration:</span><span class="value">${calc.totalDays} Days (${calc.months} Mo, ${calc.remainderDays} Days)</span></div>
                    <div class="row"><span class="label">Free Storage Days:</span><span class="value">${calc.freeDays} Days</span></div>
                    <div class="row"><span class="label">Billable Days:</span><span class="value">${calc.billableDays} Days</span></div>
                    <div class="row"><span class="label">Storage Rent (${calc.rateType}):</span><span class="value">${DateUtils.formatCurrency(calc.storageRent)}</span></div>
                    <div class="row"><span class="label">Handling Charges (LOLO):</span><span class="value">${DateUtils.formatCurrency(calc.handlingCharges)}</span></div>
                    <div class="row"><span class="label">Repair / Cleaning Charges:</span><span class="value">${DateUtils.formatCurrency(calc.repairCharges)}</span></div>
                    <div class="row"><span class="label">Other / Admin Fee:</span><span class="value">${DateUtils.formatCurrency(calc.otherCharges)}</span></div>
                    <div class="row" style="font-weight: bold; font-size: 13px; border-top: 1px solid #000; padding-top: 4px;"><span class="label">GROSS TOTAL CHARGES:</span><span class="value">${DateUtils.formatCurrency(calc.grossTotal)}</span></div>
                    <div class="row"><span class="label">Total Amount Paid:</span><span class="value">${DateUtils.formatCurrency(calc.paidAmount)}</span></div>
                    <div class="row" style="font-weight: bold; color: ${if (calc.pendingAmount <= 0) "#008000" else "#b00"}; font-size: 13px;"><span class="label">OUTSTANDING / PENDING:</span><span class="value">${DateUtils.formatCurrency(calc.pendingAmount)}</span></div>
                </div>

                <div style="text-align: center;">
                    <div class="badge">
                        ${if (calc.pendingAmount <= 0.01) "STATUS: FULLY PAID & AUTHORIZED FOR GATE OUT" else "STATUS: DUES PENDING - CLEARANCE REQUIRED"}
                    </div>
                </div>

                <div class="footer">
                    <div class="sign-box">Driver Signature</div>
                    <div class="sign-box">Yard Incharge / Stamp</div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    fun printSlip(context: Context, htmlContent: String, jobName: String = "Container Slip") {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false
            override fun onPageFinished(view: WebView?, url: String?) {
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
    }
}
