package umc.teumteum.server.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
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
        String json = System.getenv("FIREBASE_CREDENTIALS_JSON");
        if (json == null || json.isBlank()) {
          throw new IllegalStateException("FIREBASE_CREDENTIALS_JSON is not set");
        }

        File temp = new File("config/firebase-adminsdk.json");
        temp.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(temp)) {
          writer.write(json);
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
