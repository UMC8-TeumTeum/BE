package umc.teumteum.server.global.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeSerializer extends StdSerializer<LocalTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public TimeSerializer() {
        super(LocalTime.class);
    }

    @Override
    public void serialize(LocalTime localTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (localTime.equals(LocalTime.MAX)) {
            jsonGenerator.writeString("24:00");
        } else {
            jsonGenerator.writeString(localTime.format(FORMATTER));
        }
    }
}
