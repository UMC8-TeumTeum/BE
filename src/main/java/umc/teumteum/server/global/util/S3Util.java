package umc.teumteum.server.global.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class S3Util {

    @Value("${cloud.aws.s3.bucket.url:https://mock-bucket.s3.amazonaws.com/}")
    private String bucketUrl;

    public String toUrl(String key) {
        return bucketUrl.endsWith("/") ? bucketUrl + key : bucketUrl + "/" + key;
    }
}
