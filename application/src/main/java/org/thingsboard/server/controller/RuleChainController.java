/**
 * Copyright © 2016-2018 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.controller;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.rule.engine.api.ScriptEngine;
import org.thingsboard.server.common.data.DataConstants;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.Event;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.RuleChainId;
import org.thingsboard.server.common.data.id.RuleNodeId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageData;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.common.data.plugin.ComponentLifecycleEvent;
import org.thingsboard.server.common.data.rule.RuleChain;
import org.thingsboard.server.common.data.rule.RuleChainMetaData;
import org.thingsboard.server.common.msg.TbMsg;
import org.thingsboard.server.common.msg.TbMsgMetaData;
import org.thingsboard.server.dao.event.EventService;
import org.thingsboard.server.service.script.JsInvokeService;
import org.thingsboard.server.service.script.RuleNodeJsScriptEngine;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api")
public class RuleChainController extends BaseController {

    public static final String RULE_CHAIN_ID = "ruleChainId";
    public static final String RULE_NODE_ID = "ruleNodeId";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private EventService eventService;

    @Autowired
    private JsInvokeService jsInvokeService;


    /**
    * @Description: 1.2.9.11 查询登录用户规则链列表
    * @Author: ShenJi
    * @Date: 2019/1/31
    * @Param: []
    * @return: java.util.List<org.thingsboard.server.common.data.rule.RuleChain>
    */
    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
    @RequestMapping(value = "/beidouapp/ruleChains", method = RequestMethod.GET)
    @ResponseBody
    public List<RuleChain> getRuleChains(@RequestParam(required = false) String textSearch,
										 @RequestParam(required = false) String tenantIdStr) throws ThingsboardException {

        List<RuleChain> retRuleChainsList = null;
        switch (getCurrentUser().getAuthority()){
            case SYS_ADMIN:
            	if (null == textSearch)
                	retRuleChainsList = ruleChainService.findRuleChains();
            	else if (null == tenantIdStr){
					retRuleChainsList = ruleChainService.findRuleChainsByTextSearch(textSearch);
				} else {
					TenantId tenantId = new TenantId(toUUID(tenantIdStr));
					checkTenantId(tenantId);
					retRuleChainsList = ruleChainService.findRuleChainsByTenantIdAndTextSearch(tenantId,textSearch);
				}

                break;
            case TENANT_ADMIN:
				if (null == textSearch)
                	retRuleChainsList = ruleChainService.findRuleChainsByTenantId(getCurrentUser().getTenantId());
				else
					retRuleChainsList = ruleChainService.findRuleChainsByTenantIdAndTextSearch(getCurrentUser().getTenantId(),textSearch);
                break;
            case CUSTOMER_USER:
                retRuleChainsList = null;
                break;
                default:
                    throw new ThingsboardException(ThingsboardErrorCode.AUTHENTICATION);
        }
        return retRuleChainsList;
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/ruleChain/{ruleChainId}", method = RequestMethod.GET)
    @ResponseBody
    public RuleChain getRuleChainById(@PathVariable(RULE_CHAIN_ID) String strRuleChainId) throws ThingsboardException {
        checkParameter(RULE_CHAIN_ID, strRuleChainId);
        try {
            RuleChainId ruleChainId = new RuleChainId(toUUID(strRuleChainId));
            return checkRuleChain(ruleChainId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @RequestMapping(value = "/ruleChain/{ruleChainId}/metadata", method = RequestMethod.GET)
    @ResponseBody
    public RuleChainMetaData getRuleChainMetaData(@PathVariable(RULE_CHAIN_ID) String strRuleChainId,
												  @RequestParam(required = false) String tenantIdStr) throws ThingsboardException {
        checkParameter(RULE_CHAIN_ID, strRuleChainId);
		RuleChainId ruleChainId = null;
        try {
        	switch (getCurrentUser().getAuthority()){
				case TENANT_ADMIN:
					ruleChainId = new RuleChainId(toUUID(strRuleChainId));
					checkRuleChain(ruleChainId);
					return ruleChainService.loadRuleChainMetaData(getTenantId(), ruleChainId);
				case SYS_ADMIN:
/*					TenantId tenantId = new TenantId(toUUID(tenantIdStr));
					checkTenantId(tenantId);*/

					ruleChainId = new RuleChainId(toUUID(strRuleChainId));
					RuleChain ruleChain = ruleChainService.findRuleChainById(null, ruleChainId);

					if (null != ruleChain)
						return ruleChainService.loadRuleChainMetaData(ruleChain.getTenantId(), ruleChainId);
					else
						return null;
					default:
						throw new ThingsboardException(ThingsboardErrorCode.AUTHENTICATION);
			}

        } catch (Exception e) {
            throw handleException(e);
        }
    }


    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/ruleChain", method = RequestMethod.POST)
    @ResponseBody
    public RuleChain saveRuleChain(@RequestBody RuleChain ruleChain) throws ThingsboardException {
        try {
            boolean created = ruleChain.getId() == null;
            ruleChain.setTenantId(getCurrentUser().getTenantId());
            RuleChain savedRuleChain = checkNotNull(ruleChainService.saveRuleChain(ruleChain));

            actorService.onEntityStateChange(ruleChain.getTenantId(), savedRuleChain.getId(),
                    created ? ComponentLifecycleEvent.CREATED : ComponentLifecycleEvent.UPDATED);

            logEntityAction(savedRuleChain.getId(), savedRuleChain,
                    null,
                    created ? ActionType.ADDED : ActionType.UPDATED, null);

            return savedRuleChain;
        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.RULE_CHAIN), ruleChain,
                    null, ruleChain.getId() == null ? ActionType.ADDED : ActionType.UPDATED, e);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/ruleChain/{ruleChainId}/root", method = RequestMethod.POST)
    @ResponseBody
    public RuleChain setRootRuleChain(@PathVariable(RULE_CHAIN_ID) String strRuleChainId) throws ThingsboardException {
        checkParameter(RULE_CHAIN_ID, strRuleChainId);
        try {
            RuleChainId ruleChainId = new RuleChainId(toUUID(strRuleChainId));
            RuleChain ruleChain = checkRuleChain(ruleChainId);
            TenantId tenantId = getCurrentUser().getTenantId();
            RuleChain previousRootRuleChain = ruleChainService.getRootTenantRuleChain(tenantId);
            if (ruleChainService.setRootRuleChain(getTenantId(), ruleChainId)) {

                previousRootRuleChain = ruleChainService.findRuleChainById(getTenantId(), previousRootRuleChain.getId());

                actorService.onEntityStateChange(previousRootRuleChain.getTenantId(), previousRootRuleChain.getId(),
                        ComponentLifecycleEvent.UPDATED);

                logEntityAction(previousRootRuleChain.getId(), previousRootRuleChain,
                        null, ActionType.UPDATED, null);

                ruleChain = ruleChainService.findRuleChainById(getTenantId(), ruleChainId);

                actorService.onEntityStateChange(ruleChain.getTenantId(), ruleChain.getId(),
                        ComponentLifecycleEvent.UPDATED);

                logEntityAction(ruleChain.getId(), ruleChain,
                        null, ActionType.UPDATED, null);

            }
            return ruleChain;
        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.RULE_CHAIN),
                    null,
                    null,
                    ActionType.UPDATED, e, strRuleChainId);
            throw handleException(e);
        }
    }

	@PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @RequestMapping(value = "/ruleChain/metadata", method = RequestMethod.POST)
    @ResponseBody
    public RuleChainMetaData saveRuleChainMetaData(@RequestBody RuleChainMetaData ruleChainMetaData,
                                                   @RequestParam(required = false) String tenantIdStr) throws ThingsboardException {
        RuleChainMetaData savedRuleChainMetaData = null;
        RuleChain ruleChain = null;
        try {
            switch (getCurrentUser().getAuthority()){
                case TENANT_ADMIN:
                    ruleChain = checkRuleChain(ruleChainMetaData.getRuleChainId());
                    savedRuleChainMetaData = checkNotNull(ruleChainService.saveRuleChainMetaData(getTenantId(), ruleChainMetaData));

                    actorService.onEntityStateChange(ruleChain.getTenantId(), ruleChain.getId(), ComponentLifecycleEvent.UPDATED);

                    logEntityAction(ruleChain.getId(), ruleChain,
                            null,
                            ActionType.UPDATED, null, ruleChainMetaData);

                    return savedRuleChainMetaData;

                case SYS_ADMIN:
                    /*checkNotNull(tenantIdStr);
                    Tenant tenant = tenantService.findTenantById(new TenantId(toUUID(tenantIdStr)));
                    if (null == tenant)
                        throw new ThingsboardException("tenantId Not Find",ThingsboardErrorCode.INVALID_ARGUMENTS);
*/
                    ruleChain = ruleChainService.findRuleChainById(null, ruleChainMetaData.getRuleChainId());

                    savedRuleChainMetaData = checkNotNull(ruleChainService.saveRuleChainMetaData(ruleChain.getTenantId(), ruleChainMetaData));

                    actorService.onEntityStateChange(ruleChain.getTenantId(), ruleChain.getId(), ComponentLifecycleEvent.UPDATED);

                    logEntityAction(ruleChain.getId(), ruleChain,
                            null,
                            ActionType.UPDATED, null, ruleChainMetaData);

                    return savedRuleChainMetaData;

                    default:
                        throw new ThingsboardException(ThingsboardErrorCode.AUTHENTICATION);
            }

        } catch (Exception e) {

            logEntityAction(emptyId(EntityType.RULE_CHAIN), null,
                    null, ActionType.UPDATED, e, ruleChainMetaData);

            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/ruleChains", params = {"limit"}, method = RequestMethod.GET)
    @ResponseBody
    public TextPageData<RuleChain> getRuleChains(
            @RequestParam int limit,
            @RequestParam(required = false) String textSearch,
            @RequestParam(required = false) String idOffset,
            @RequestParam(required = false) String textOffset) throws ThingsboardException {
        try {
            TenantId tenantId = getCurrentUser().getTenantId();
            TextPageLink pageLink = createPageLink(limit, textSearch, idOffset, textOffset);
            return checkNotNull(ruleChainService.findTenantRuleChains(tenantId, pageLink));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/ruleChain/{ruleChainId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteRuleChain(@PathVariable(RULE_CHAIN_ID) String strRuleChainId) throws ThingsboardException {
        checkParameter(RULE_CHAIN_ID, strRuleChainId);
        try {
            RuleChainId ruleChainId = new RuleChainId(toUUID(strRuleChainId));
            RuleChain ruleChain = checkRuleChain(ruleChainId);

            ruleChainService.deleteRuleChainById(getTenantId(), ruleChainId);

            actorService.onEntityStateChange(ruleChain.getTenantId(), ruleChain.getId(), ComponentLifecycleEvent.DELETED);

            logEntityAction(ruleChainId, ruleChain,
                    null,
                    ActionType.DELETED, null, strRuleChainId);

        } catch (Exception e) {
            logEntityAction(emptyId(EntityType.RULE_CHAIN),
                    null,
                    null,
                    ActionType.DELETED, e, strRuleChainId);
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/ruleNode/{ruleNodeId}/debugIn", method = RequestMethod.GET)
    @ResponseBody
    public JsonNode getLatestRuleNodeDebugInput(@PathVariable(RULE_NODE_ID) String strRuleNodeId) throws ThingsboardException {
        checkParameter(RULE_NODE_ID, strRuleNodeId);
        try {
            RuleNodeId ruleNodeId = new RuleNodeId(toUUID(strRuleNodeId));
            TenantId tenantId = getCurrentUser().getTenantId();
            List<Event> events = eventService.findLatestEvents(tenantId, ruleNodeId, DataConstants.DEBUG_RULE_NODE, 2);
            JsonNode result = null;
            if (events != null) {
                for (Event event : events) {
                    JsonNode body = event.getBody();
                    if (body.has("type") && body.get("type").asText().equals("IN")) {
                        result = body;
                        break;
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAuthority('TENANT_ADMIN')")
    @RequestMapping(value = "/ruleChain/testScript", method = RequestMethod.POST)
    @ResponseBody
    public JsonNode testScript(@RequestBody JsonNode inputParams) throws ThingsboardException {
        try {
            String script = inputParams.get("script").asText();
            String scriptType = inputParams.get("scriptType").asText();
            JsonNode argNamesJson = inputParams.get("argNames");
            String[] argNames = objectMapper.treeToValue(argNamesJson, String[].class);

            String data = inputParams.get("msg").asText();
            JsonNode metadataJson = inputParams.get("metadata");
            Map<String, String> metadata = objectMapper.convertValue(metadataJson, new TypeReference<Map<String, String>>() {});
            String msgType = inputParams.get("msgType").asText();
            String output = "";
            String errorText = "";
            ScriptEngine engine = null;
            try {
                engine = new RuleNodeJsScriptEngine(jsInvokeService, getCurrentUser().getId(), script, argNames);
                TbMsg inMsg = new TbMsg(UUIDs.timeBased(), msgType, null, new TbMsgMetaData(metadata), data, null, null, 0L);
                switch (scriptType) {
                    case "update":
                        output = msgToOutput(engine.executeUpdate(inMsg));
                        break;
                    case "generate":
                        output = msgToOutput(engine.executeGenerate(inMsg));
                        break;
                    case "filter":
                        boolean result = engine.executeFilter(inMsg);
                        output = Boolean.toString(result);
                        break;
                    case "switch":
                        Set<String> states = engine.executeSwitch(inMsg);
                        output = objectMapper.writeValueAsString(states);
                        break;
                    case "json":
                        JsonNode json = engine.executeJson(inMsg);
                        output = objectMapper.writeValueAsString(json);
                        break;
                    case "string":
                        output = engine.executeToString(inMsg);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported script type: " + scriptType);
                }
            } catch (Exception e) {
                log.error("Error evaluating JS function", e);
                errorText = e.getMessage();
            } finally {
                if (engine != null) {
                    engine.destroy();
                }
            }
            ObjectNode result = objectMapper.createObjectNode();
            result.put("output", output);
            result.put("error", errorText);
            return result;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    private String msgToOutput(TbMsg msg) throws Exception {
        ObjectNode msgData = objectMapper.createObjectNode();
        if (!StringUtils.isEmpty(msg.getData())) {
            msgData.set("msg", objectMapper.readTree(msg.getData()));
        }
        Map<String, String> metadata = msg.getMetaData().getData();
        msgData.set("metadata", objectMapper.valueToTree(metadata));
        msgData.put("msgType", msg.getType());
        return objectMapper.writeValueAsString(msgData);
    }

}
