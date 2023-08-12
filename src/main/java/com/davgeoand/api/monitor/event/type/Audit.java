package com.davgeoand.api.monitor.event.type;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.*;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder(toBuilder = true)
public class Audit extends Event {
    private String requestPath;
    private String status;
    private String method;
    private String response;
    private Float requestDuration;
    private String traceId;

    @Override
    public Point toPoint() {
        return Point.measurement("audit")
                .addTag("request.path", requestPath)
                .addTag("status", status)
                .addTag("method", method)
                .addField("response", response)
                .addField("request.duration", requestDuration)
                .addField("trace.id", traceId)
                .time(time, WritePrecision.MS);
    }
}
