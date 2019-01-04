package org.thingsboard.server.common.data.alarmstatistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.common.data.BaseData;
import org.thingsboard.server.common.data.EntityType;

/**
 * Created by ztao at 2019/1/3 15:15.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AlarmEntityCountInfo extends BaseData<AlarmStatisticsId> {
    private String entityId;
    private EntityType entityType;
    private String entityName;
    private AlarmEntityCount alarmCount;
}
