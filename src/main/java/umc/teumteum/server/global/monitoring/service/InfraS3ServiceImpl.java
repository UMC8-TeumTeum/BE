package umc.teumteum.server.global.monitoring.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
@RequiredArgsConstructor
public class InfraS3ServiceImpl implements InfraS3Service{

  private final S3Client s3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  @Value("${cloud.aws.s3.bucket-path}")
  private String prefix;

  @Override
  public List<String> listObjects() {
    ListObjectsV2Request request = ListObjectsV2Request.builder()
        .bucket(bucket)
        .prefix(prefix)
        .maxKeys(10)
        .build();

    ListObjectsV2Response response = s3Client.listObjectsV2(request);

    return response.contents().stream()
        .map(S3Object::key)
        .collect(Collectors.toList());
  }
}
