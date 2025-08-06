package umc.teumteum.server.global.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateTimeSerializer extends StdSerializer<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public DateTimeSerializer() {
        super(LocalDateTime.class);
    }
    @Override
    public void serialize(LocalDateTime dateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if(dateTime.toLocalTime().equals(LocalTime.MIDNIGHT)){
            String dayBefore = dateTime.toLocalDate().minusDays(1).toString();
            jsonGenerator.writeString(dayBefore+"T24:00");
        } else{
            jsonGenerator.writeString(dateTime.format(FORMATTER));
        }
    }
}
