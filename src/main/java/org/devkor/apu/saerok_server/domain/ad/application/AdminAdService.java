package org.devkor.apu.saerok_server.domain.ad.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.ad.core.entity.Ad;
import org.devkor.apu.saerok_server.domain.ad.core.repository.AdRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminAdService {

    private final AdRepository adRepository;

    @Transactional(readOnly = true)
    public List<Ad> listAds() {
        return adRepository.findAll();
    }

    public Ad createAd(String name,
                       String memo,
                       String objectKey,
                       String contentType,
                       String targetUrl) {
        Ad ad = Ad.create(name, memo, objectKey, contentType, targetUrl);
        return adRepository.save(ad);
    }

    public Ad updateAd(Long id,
                       String name,
                       String memo,
                       String objectKey,
                       String contentType,
                       String targetUrl) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 광고 id예요."));
        ad.update(name, memo, objectKey, contentType, targetUrl);
        return ad;
    }

    public void deleteAd(Long id) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 광고 id예요."));
        adRepository.delete(ad);
    }
}
