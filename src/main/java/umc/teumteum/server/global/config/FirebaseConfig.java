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
        String base64 = System.getenv("FIREBASE_CREDENTIALS_BASE64");
        if (base64 == null || base64.isBlank()) {
          throw new IllegalStateException("FIREBASE_CREDENTIALS_BASE64 is not set");
        }

        byte[] decoded = Base64.getDecoder().decode(base64);

        File temp = new File("config/firebase-adminsdk.json");
        temp.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(temp)) {
          fos.write(decoded);
        }

        options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(new FileInputStream(temp)))
            .build();
      } else {
        FileInputStream account = new FileInputStream(firebaseConfigPath);
        options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(account))
            .build();
      }

      FirebaseApp.initializeApp(options);
    }
  }

  @Bean
  public FirebaseMessaging firebaseMessaging() {
    return FirebaseMessaging.getInstance();
  }
}
