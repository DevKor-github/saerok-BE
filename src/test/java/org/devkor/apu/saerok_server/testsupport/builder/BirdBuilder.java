package org.devkor.apu.saerok_server.testsupport.builder;

import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdDescription;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdName;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdTaxonomy;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

/**
 * Builder for creating and persisting Bird fixtures in tests.
 */
public class BirdBuilder {
    private final TestEntityManager em;
    private String korName = "까치";
    private String sciName = "Pica pica";
    private BirdTaxonomy taxonomy;
    private Double bodyLengthCm = 25.0;
    private String nibrUrl = null;

    public BirdBuilder(TestEntityManager em) {
        this.em = em;
        // default taxonomy values
        this.taxonomy = new BirdTaxonomy();
        taxonomy.setPhylumEng("Chordata");
        taxonomy.setPhylumKor("극피동물");
        taxonomy.setClassEng("Aves");
        taxonomy.setClassKor("조류");
        taxonomy.setOrderEng("Passeriformes");
        taxonomy.setOrderKor("참새목");
        taxonomy.setFamilyEng("Pycnonotidae");
        taxonomy.setFamilyKor("직박구리과");
        taxonomy.setGenusEng("Hypsipetes");
        taxonomy.setGenusKor("직박구리속");
        taxonomy.setSpeciesEng("Hypsipetes amaurotis");
        taxonomy.setSpeciesKor("직박구리");
    }

    public BirdBuilder korName(String korName) {
        this.korName = korName;
        return this;
    }

    public BirdBuilder sciName(String sciName) {
        this.sciName = sciName;
        return this;
    }

    public BirdBuilder bodyLengthCm(Double length) {
        this.bodyLengthCm = length;
        return this;
    }

    public BirdBuilder thumbnailUrl(String url) {
        this.nibrUrl = url;
        return this;
    }

    /**
     * Builds and persists the Bird.
     */
    public Bird build() {
        Bird bird = new Bird();

        // name
        BirdName name = new BirdName();
        name.setKoreanName(korName);
        name.setScientificName(sciName);
        ReflectionTestUtils.setField(bird, "name", name);

        // taxonomy
        ReflectionTestUtils.setField(bird, "taxonomy", taxonomy);

        // description
        BirdDescription desc = new BirdDescription();
        ReflectionTestUtils.setField(bird, "description", desc);

        // additional fields
        ReflectionTestUtils.setField(bird, "bodyLengthCm", bodyLengthCm);
        ReflectionTestUtils.setField(bird, "nibrUrl", nibrUrl);

        em.persist(bird);
        em.flush();
        return bird;
    }
}
