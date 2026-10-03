/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package de.diegurus.mustangtest.lineitemnotes;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.mustangproject.IncludedNote;
import org.mustangproject.Invoice;
import org.mustangproject.Item;
import org.mustangproject.Product;
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
    public static void main(String[] args) {
        Invoice inv = new Invoice()
                .setNumber("TestRE-2026-09-0001")
                .setIssueDate(new Date())
                .setDeliveryDate(new Date())
                .setSender(new TradeParty("diegurus - zurawski zurawski poppl rohland GbR", "Juister Str. 3", "65199", "Wiesbaden", "DE"))
                .setRecipient(new TradeParty("Musterkunde", "Musterweg 9", "99900", "Musterstadt", "DE"));

        List<IncludedNote> headerNotes = Arrays.asList(
                // Add free text company data
                new IncludedNote("AAB-CONTENT-HEADER", SubjectCode.AAB),
                new IncludedNote("AAI-CONTENT-HEADER", SubjectCode.AAI),
                new IncludedNote("PMD-CONTENT-HEADER", SubjectCode.PMD),
                new IncludedNote("PMT-CONTENT-HEADER", SubjectCode.PMT),
                new IncludedNote("REG-CONTENT-HEADER", SubjectCode.REG),
                new IncludedNote("SUR-CONTENT-HEADER", SubjectCode.SUR));
        inv.setNotesWithSubjectCode(headerNotes);

        Item item = new Item()
                .setProduct(new Product("Datenbank Design", "DB Design - MS SQL 2026", "HUR", BigDecimal.valueOf(20.0)))
                .setPrice(BigDecimal.valueOf(125.0))
                .setQuantity(BigDecimal.valueOf(8.0));
        item.addNote("DB Design nach Vorgabe des Kunden inkl.");
        inv.addItem(item);

        item = new Item()
                .setProduct(new Product("Dokumentation", "Erstellen einer Kurzdokumentation für die ausgeführten Tätigkeiten", "HUR", BigDecimal.valueOf(20.0)))
                .setPrice(BigDecimal.valueOf(95.0))
                .setQuantity(BigDecimal.valueOf(2.5));
        List<IncludedNote> lineItemNotes = Arrays.asList(
                new IncludedNote("AAB-CONTENT-LINE", SubjectCode.AAB),
                new IncludedNote("AAB-CONTENT-LINE#2", SubjectCode.AAB),
                new IncludedNote("AAI-CONTENT-LINE", SubjectCode.AAI),
                new IncludedNote("SUR-CONTENT-LINE", SubjectCode.SUR)
        );
        item.addNotes(lineItemNotes);
        item.addNote("CONTENT-LINE-NO-SUBJECTCODE");
        inv.addItem(item);

        String theProfile = "EXTENDED"; // EN16931

        ZUGFeRD2PullProvider zf2p = new ZUGFeRD2PullProvider();
        zf2p.setProfile(Profiles.getByName(theProfile));
        zf2p.generateXML(inv);
        String zfXML = new String(zf2p.getXML(), StandardCharsets.UTF_8);

        System.out.println(zfXML);
    }
}
