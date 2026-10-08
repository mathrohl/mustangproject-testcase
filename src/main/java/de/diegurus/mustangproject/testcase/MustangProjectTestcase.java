/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package de.diegurus.mustangproject.testcase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
//
// This has been tested with 
// * NetBeans IDE 25 & 27 / JDK 17 / MustangLib 2.26.0 - o.k.
// * NetBeans IDE 27 / JDK 17 / MustangLib 2.25.1-SNAPSHOT - o.k.
// * NetBeans IDE 27 / JDK 17 / MustangLib 2.24.0 - * ERROR *
//   MustangLib 2.24.0 silently does not include the line item notes with
//   SubjectCode(s)
//
public class MustangProjectTestcase {

    public static void main(String[] args) throws ParseException {
        String salesTaxCategoryCode = "S"; // Standard tax rate        
        BigDecimal salesTaxRate = BigDecimal.valueOf(20.0); // French VAT rate applies
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
        inv.setRecipient(new TradeParty("Musterkunde AG", "Musterweg 9", "99900", "Musterstadt", "DE")
                .addVATID("DE772689730")
                .setGlobalID("3210123450000") // GLN
                .setGlobalIDScheme("0088"));
        inv.setDeliveryAddress(new TradeParty("MK AG / IT-Abteilung", "Musterweg 9a", "99900", "Musterstadt", "DE")
                .setGlobalID("3210123450016") // GLN
                .setGlobalIDScheme("0088"));

        List<IncludedNote> headerNotes = Arrays.asList(
                // Add free text company data
                new IncludedNote("AAB-CONTENT-HEADER - Mention d'escompte.", SubjectCode.AAB),
                new IncludedNote("AAI-CONTENT-HEADER - Information générale.", SubjectCode.AAI),
                new IncludedNote("PMD-CONTENT-HEADER - Informations de règlement.", SubjectCode.PMD),
                new IncludedNote("PMT-CONTENT-HEADER - Mention de l’indemnité forfaitaire de 40 € pour frais de recouvrement.", SubjectCode.PMT),
                new IncludedNote("REG-CONTENT-HEADER - Informations réglementaires.", SubjectCode.REG),
                new IncludedNote("SUR-CONTENT-HEADER - Remarques fournisseur.", SubjectCode.SUR));
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
        item.addNote("Datenbank Design nach Vorgabe des Kunden mit von diesem zur Vefügung gestellten Testdaten.");
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
                new IncludedNote("AAB-CONTENT-LINE - Mention d'escompte.", SubjectCode.AAB),
                new IncludedNote("AAB-CONTENT-LINE#2 - Mention d'escompte.", SubjectCode.AAB),
                new IncludedNote("AAI-CONTENT-LINE - Information générale.", SubjectCode.AAI),
                new IncludedNote("SUR-CONTENT-LINE - Remarques fournisseur.", SubjectCode.SUR)
        );
        item.addNotes(lineItemNotes);
        item.addNote("CONTENT-LINE-NO-SUBJECTCODE - Free text may appear only once.");
        inv.addItem(item);

        String theProfile = "EXTENDED";
        ZUGFeRD2PullProvider zf2p = new ZUGFeRD2PullProvider();
        zf2p.setProfile(Profiles.getByName(theProfile));
        zf2p.generateXML(inv);
        String zfXML = new String(zf2p.getXML(), StandardCharsets.UTF_8);

        System.out.println(zfXML);

        writeFacturxXml(zfXML);
    }

    //
    // String filename = "Testdata/factur-x.xml
    // File file = new File(filename);
    // File absoluteFile = file.getAbsoluteFile();
    // File absoluteDirectory = absoluteFile.getParentFile();
    // System.out.println(absoluteDirectory.toString());
    //
    private static void writeFacturxXml(String zfXML) {
        FileOutputStream fos;
        String filename = "Testdata/factur-x.xml";

        try {
            fos = new FileOutputStream(filename);
            fos.write(zfXML.getBytes("UTF8"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MustangProjectTestcase.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MustangProjectTestcase.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            File file = new File(filename);
            String successMsg = "The XML has been witten as\n" + file.getAbsoluteFile() + "\n";
            System.out.println(successMsg);
        }
    }
}
