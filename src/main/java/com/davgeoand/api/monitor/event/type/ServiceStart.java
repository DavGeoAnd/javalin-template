package com.davgeoand.api.monitor.event.type;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.*;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder(toBuilder = true)
public class ServiceStart extends Event {
    private String buildVersion;
    private String gitBranch;
    private String gitCommitId;
    private long startTime;
    private long serviceStartDuration;
    private String javaVersion;

    @Override
    public Point toPoint() {
        return Point.measurement("service.start")
                .addField("build.version", buildVersion)
                .addField("git.branch", gitBranch)
                .addField("git.commit.id", gitCommitId)
                .addField("start.time", startTime)
                .addField("service.start.duration", serviceStartDuration)
                .addField("java.version", javaVersion)
                .time(time, WritePrecision.MS);
    }
}
