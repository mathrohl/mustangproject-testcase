/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package de.diegurus.mustangtest.lineitemnotes;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.mustangproject.Charge;
import org.mustangproject.IncludedNote;
import org.mustangproject.Invoice;
import org.mustangproject.Item;
import org.mustangproject.Product;
import org.mustangproject.ReferencedDocument;
import org.mustangproject.SubjectCode;
import org.mustangproject.TradeParty;
import org.mustangproject.ZUGFeRD.Profiles;
import org.mustangproject.ZUGFeRD.ZUGFeRD2PullProvider;

/**
 *
 * @author mro
 */
public class MustangTestLineItemNotes {

    //
    // Tested with 
    // * NetBeans IDE 27 / JDK 17 / MustangLib 2.26.0 - o.k.
    // * NetBeans IDE 27 / JDK 17 / MustangLib 2.25.1-SNAPSHOT - o.k.
    // * NetBeans IDE 27 / JDK 17 / MustangLib 2.24.0 - * ERROR *
    //   the MustangLib 2.24.0 does not include the line item notes with
    //   SubjectCode(s)
    //
    public static void main(String[] args) throws ParseException {
        String salesTaxCategoryCode = "S"; // Standard tax rate        
        BigDecimal salesTaxRate = BigDecimal.valueOf(19.0);        
        String custOrderDocNumber = "EDI-2026-000069";
        ReferencedDocument refDocCustOrder;
        refDocCustOrder = new ReferencedDocument()
                .setIssuerAssignedID(custOrderDocNumber)
                .setReferenceTypeCode("105"); // 105 - Purchase order
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        Invoice inv = new Invoice()
                .setNumber("TestRE-2026-09-0001")
                .setIssueDate(new Date())
                .setDeliveryDate(new Date());
        Date invoicDueDate = dateFormat.parse("20991231");
        inv.setDueDate(invoicDueDate);
        inv.setSender(new TradeParty("diegurus - zurawski zurawski poppl rohland GbR", "Juister Str. 3", "65199", "Wiesbaden", "DE")
                .addVATID("DE318445037"));
        inv.setRecipient(new TradeParty("Musterkunde", "Musterweg 9", "99900", "Musterstadt", "DE")
                .addVATID("DE772689730"));

        List<IncludedNote> headerNotes = Arrays.asList(
                // Add free text company data
                new IncludedNote("AAB-CONTENT-HEADER", SubjectCode.AAB),
                new IncludedNote("AAI-CONTENT-HEADER", SubjectCode.AAI),
                new IncludedNote("PMD-CONTENT-HEADER", SubjectCode.PMD),
                new IncludedNote("PMT-CONTENT-HEADER", SubjectCode.PMT),
                new IncludedNote("REG-CONTENT-HEADER", SubjectCode.REG),
                new IncludedNote("SUR-CONTENT-HEADER", SubjectCode.SUR));
        inv.setNotesWithSubjectCode(headerNotes);

        inv.addCharge(new Charge(new BigDecimal("9.00"))
                .setReason("Manueller Datentansfer auf den Kundenserver.")
                .setReasonCode("FC")
                .setTaxCategoryCode(salesTaxCategoryCode)
                .setTaxRateApplicablePercent(salesTaxRate)
        );

        Item item = new Item()
                .setProduct(new Product("Datenbank Design", "DB Design - MS SQL 2026", "HUR", salesTaxRate))
                .setPrice(BigDecimal.valueOf(125.0))
                .setQuantity(BigDecimal.valueOf(8.0));
        item.addNote("DB Design nach Vorgabe des Kunden mit von diesem zur Vefügung gestellten Testdaten.");
        refDocCustOrder.setLineID("1");
        item.setBuyerOrderReferencedDocument(refDocCustOrder);
        inv.addItem(item);

        item = new Item()
                .setProduct(new Product("Dokumentation", "Erstellen einer Kurzdokumentation für die ausgeführten Tätigkeiten.", "HUR", salesTaxRate))
                .setPrice(BigDecimal.valueOf(95.0))
                .setQuantity(BigDecimal.valueOf(2.5));
        refDocCustOrder.setLineID("2");
        item.setBuyerOrderReferencedDocument(refDocCustOrder);
        //
        // Charge reason code (UNCL7161)
        // Source: https://docs.peppol.eu/poacc/billing/3.0/codelist/UNCL7161/
        //
        item.addAllowance(new Charge(new BigDecimal("0.99"))
                .setReason("Allowance reason text for DAP.")
                .setReasonCode("DAP") // Needs to be one of the above reason codes.
                .setTaxRateApplicablePercent(salesTaxRate)
        );

        List<IncludedNote> lineItemNotes = Arrays.asList(
                new IncludedNote("AAB-CONTENT-LINE", SubjectCode.AAB),
                new IncludedNote("AAB-CONTENT-LINE#2", SubjectCode.AAB),
                new IncludedNote("AAI-CONTENT-LINE", SubjectCode.AAI),
                new IncludedNote("SUR-CONTENT-LINE", SubjectCode.SUR)
        );
        item.addNotes(lineItemNotes);
        item.addNote("CONTENT-LINE-NO-SUBJECTCODE");
        inv.addItem(item);

        String theProfile = "EXTENDED";
        ZUGFeRD2PullProvider zf2p = new ZUGFeRD2PullProvider();
        zf2p.setProfile(Profiles.getByName(theProfile));
        zf2p.generateXML(inv);
        String zfXML = new String(zf2p.getXML(), StandardCharsets.UTF_8);

        System.out.println(zfXML);
    }
}
