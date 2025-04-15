package org.devkor.apu.saerok_server.domain.dex.bird.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class BirdTaxonomy {

    @Column(name = "phylum_eng", nullable = false)
    private String phylumEng;

    @Column(name = "phylum_kor", nullable = false)
    private String phylumKor;

    @Column(name = "class_eng", nullable = false)
    private String classEng;

    @Column(name = "class_kor", nullable = false)
    private String classKor;

    @Column(name = "order_eng", nullable = false)
    private String orderEng;

    @Column(name = "order_kor", nullable = false)
    private String orderKor;

    @Column(name = "family_eng", nullable = false)
    private String familyEng;

    @Column(name = "family_kor", nullable = false)
    private String familyKor;

    @Column(name = "genus_eng", nullable = false)
    private String genusEng;

    @Column(name = "genus_kor", nullable = false)
    private String genusKor;

    @Column(name = "species_eng", nullable = false)
    private String speciesEng;

    @Column(name = "species_kor", nullable = false)
    private String speciesKor;

    protected BirdTaxonomy() { }

    public BirdTaxonomy(String phylumEng, String phylumKor, String classEng, String classKor, String orderEng, String orderKor, String familyEng, String familyKor, String genusEng, String genusKor, String speciesEng, String speciesKor) {
        this.phylumEng = phylumEng;
        this.phylumKor = phylumKor;
        this.classEng = classEng;
        this.classKor = classKor;
        this.orderEng = orderEng;
        this.orderKor = orderKor;
        this.familyEng = familyEng;
        this.familyKor = familyKor;
        this.genusEng = genusEng;
        this.genusKor = genusKor;
        this.speciesEng = speciesEng;
        this.speciesKor = speciesKor;
    }
}
