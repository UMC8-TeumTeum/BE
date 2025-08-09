package umc.teumteum.server.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseConfig {

  @Value("${spring.profiles.active:dev}")
  private String activeProfile;

  @Value("${firebase.config.path:config/firebase-adminsdk.json}")
  private String firebaseConfigPath;

  @PostConstruct
  public void initializeFirebase() throws IOException {
    if (FirebaseApp.getApps().isEmpty()) {
      FirebaseOptions options;
      if ("prod".equals(activeProfile)) {
        String path = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (path == null || path.isBlank()) {
          throw new IllegalStateException("GOOGLE_APPLICATION_CREDENTIALS is not set");
        }
        try (FileInputStream in = new FileInputStream(path)) {
          options = FirebaseOptions.builder()
              .setCredentials(GoogleCredentials.fromStream(in))
              .build();
        }
      } else {
        try (FileInputStream in = new FileInputStream(firebaseConfigPath)) {
          options = FirebaseOptions.builder()
              .setCredentials(GoogleCredentials.fromStream(in))
              .build();
        }
      }
      FirebaseApp.initializeApp(options);
    }
  }

  @Bean
  public FirebaseMessaging firebaseMessaging() {
    return FirebaseMessaging.getInstance();
  }
}
