package org.thingsboard.server.common.data.asset;

import lombok.Data;

@Data
public class AssetExInfo extends Asset{

    private String basicinfo;
    private String warningRuleCfg;//告警规则配置

}
