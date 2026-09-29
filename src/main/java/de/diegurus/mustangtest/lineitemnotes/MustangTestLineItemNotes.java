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

    public static void main(String[] args) {
        Invoice inv = new Invoice()
                .setNumber("RE2026-09-0001")
                .setIssueDate(new Date())
                .setDeliveryDate(new Date())
                .setSender(new TradeParty("Dienstleister", "Bahnstr. 1", "65205", "Wiesbaden", "DE"))
                .setRecipient(new TradeParty("Musterkunde", "Musterweg 9", "99001", "Musterstadt", "DE"));

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
                .setProduct(new Product("Elektronen schubsen", "Elektronen schubsen", "HUR", BigDecimal.valueOf(20.0)))
                .setPrice(BigDecimal.valueOf(95.0))
                .setQuantity(BigDecimal.valueOf(8.0));
        item.addNote("Total gute Beratung nur bei uns!");
        inv.addItem(item);

        item = new Item()
                .setProduct(new Product("Irgendwas mit Zuhören", "Irgendwas mit Zuhören", "HUR", BigDecimal.valueOf(20.0)))
                .setPrice(BigDecimal.valueOf(120.0))
                .setQuantity(BigDecimal.valueOf(1.0));
        List<IncludedNote> lineItemNotes = Arrays.asList(
                new IncludedNote("AAB-CONTENT-LINE", SubjectCode.AAB),
                new IncludedNote("AAI-CONTENT-LINE", SubjectCode.AAI)
        );
        item.addNotes(lineItemNotes);
        item.addNote("Wir hören ungern zu!!!");
        inv.addItem(item);

        String theProfile = "EXTENDED"; // EN16931

        ZUGFeRD2PullProvider zf2p = new ZUGFeRD2PullProvider();
        zf2p.setProfile(Profiles.getByName(theProfile));
        zf2p.generateXML(inv);
        String zfXML = new String(zf2p.getXML(), StandardCharsets.UTF_8);

        System.out.println(zfXML);
    }
}
