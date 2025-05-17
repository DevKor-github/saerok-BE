package org.devkor.apu.saerok_server.global.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CloudFrontUrlService {

    @Value("${aws.cloudfront.image-domain}")
    private String cloudFrontDomain;

    public String toImageUrl(String objectKey) {
        return cloudFrontDomain + "/" + objectKey;
    }
}
