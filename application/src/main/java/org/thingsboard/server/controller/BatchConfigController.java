package org.thingsboard.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.FutureCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.thingsboard.rule.engine.api.msg.DeviceAttributesEventNotificationMsg;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.asset.Asset;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.batchconfig.DeviceAutoLogon;
import org.thingsboard.server.common.data.batchconfig.DeviceClientAttrib;
import org.thingsboard.server.common.data.batchconfig.DeviceServerAttrib;
import org.thingsboard.server.common.data.batchconfig.DeviceShareAttrib;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.*;
import org.thingsboard.server.common.data.kv.AttributeKvEntry;
import org.thingsboard.server.common.data.kv.BaseAttributeKvEntry;
import org.thingsboard.server.common.data.kv.StringDataEntry;
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.data.relation.RelationTypeGroup;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.common.data.security.DeviceCredentials;
import org.thingsboard.server.common.msg.cluster.SendToClusterMsg;
import org.thingsboard.server.service.security.AccessValidator;
import org.thingsboard.server.service.security.model.SecurityUser;

import javax.annotation.Nullable;
import java.util.*;

@RestController
@Slf4j
public class BatchConfigController extends BaseController {

	@Autowired
	private AccessValidator accessValidator;

	private static ObjectMapper MAPPER = new ObjectMapper();

	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@RequestMapping(value = "/api/batchconfig/devices/{assetId}", method = RequestMethod.GET)
	@ResponseBody
	public List<DeviceAutoLogon> getDevices(@PathVariable("assetId") String assetId,@RequestParam String tenantIdStr) {
		try {

			TenantId tenantIdTmp = new TenantId(toUUID(tenantIdStr));
			checkTenantId(tenantIdTmp);
			TenantId tenantId = tenantService.findTenantById(tenantIdTmp).getId();

			AssetId aid = new AssetId(UUID.fromString(assetId));
			Asset a = checkNotNull(assetService.findAssetById(tenantId, aid));

			List<EntityRelation> relations = checkNotNull(relationService.findByFromAndType(tenantId, a.getId(), "Contains", RelationTypeGroup.COMMON));

			return getDevicesAttrib(tenantId,relations);
		} catch (Exception e) {
			e.printStackTrace();
			handleException(e);
		}

		return null;
	}

	/**
	 * @Description: 获取设备属性并生成列表
	 * @Author: ShenJi
	 * @Date: 2018/12/29
	 * @Param:
	 * @return:
	 */
	private List<DeviceAutoLogon> getDevicesAttrib(TenantId tenantId,List<EntityRelation> relations) {
		List<DeviceAutoLogon> ret = new ArrayList<>();

		relations.stream()
				.filter(p -> {
					return p.getTo().getEntityType() == EntityType.DEVICE;
				})
				.forEach((relation) -> {
					DeviceAutoLogon deviceAutoLogon = new DeviceAutoLogon();
					try {
						List<AttributeKvEntry> attributeKvEntries = attributesService.findAll(tenantId,relation.getTo(),"CLIENT_SCOPE").get();
						Map<String, Object> clientMap = new HashMap<>();
						attributeKvEntries.forEach((attributeKvEntry -> {
							clientMap.put(attributeKvEntry.getKey(),attributeKvEntry.getValue().toString());
						}));
						deviceAutoLogon.setDeviceClientAttrib(MAPPER.readValue(MAPPER.writeValueAsString(clientMap), DeviceClientAttrib.class));

						attributeKvEntries = attributesService.findAll(tenantId,relation.getTo(),"SERVER_SCOPE").get();
						Map<String, Object> serverMap = new HashMap<>();
						attributeKvEntries.forEach((attributeKvEntry -> {
							serverMap.put(attributeKvEntry.getKey(),attributeKvEntry.getValue().toString());
						}));
						deviceAutoLogon.setDeviceServerAttrib(MAPPER.readValue(MAPPER.writeValueAsString(serverMap), DeviceServerAttrib.class));

						attributeKvEntries = attributesService.findAll(tenantId,relation.getTo(),"SHARED_SCOPE").get();
						Map<String, Object> shareMap = new HashMap<>();
						attributeKvEntries.forEach((attributeKvEntry -> {
							shareMap.put(attributeKvEntry.getKey(),attributeKvEntry.getValue().toString());
						}));
						deviceAutoLogon.setDeviceShareAttrib(MAPPER.readValue(MAPPER.writeValueAsString(shareMap), DeviceShareAttrib.class));

						deviceAutoLogon.setSystemDeviceId(relation.getTo().getId().toString());

						ret.add(deviceAutoLogon);

					}
					catch (Exception e){
						log.info("Get device client attrib error:"+relation.getTo().getId());
						e.printStackTrace();
						handleException(e);
					}

				});

		return ret;
	}

	/**
	 * @Description: 批量添加设备设置属性并关联资产
	 * @Author: ShenJi
	 * @Date: 2018/12/28
	 * @Param: [assetId, devicesSaveRequest]
	 * @return: org.springframework.web.context.request.async.DeferredResult<org.springframework.http.ResponseEntity>
	 */
	@PreAuthorize("hasAuthority('SYS_ADMIN')")
	@RequestMapping(value = "/api/batchconfig/devices/{assetId}", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	public DeferredResult<ResponseEntity> saveDevices(@PathVariable("assetId") String assetId,
													  @RequestParam String tenantIdStr,
													  @RequestBody List<DeviceAutoLogon> devicesSaveRequest) {
		try {
			TenantId tenantIdTmp = new TenantId(toUUID(tenantIdStr));
			checkTenantId(tenantIdTmp);
			TenantId tenantId = tenantService.findTenantById(tenantIdTmp).getId();

			AssetId aid = new AssetId(UUID.fromString(assetId));
			Asset a = assetService.findAssetById(tenantId, aid);
			if (a == null) {
				return null;
			}
			devicesSaveRequest.forEach((deviceInfo) -> {

				String uidStr = assetId + "|" + deviceInfo.getDeviceShareAttrib().getIp() + "|" + deviceInfo.getDeviceShareAttrib().getChannel();
				deviceInfo.getDeviceServerAttrib().setName(uidStr.hashCode() + "");
				Device device = deviceService.findDeviceByTenantIdAndName(tenantId, deviceInfo.getDeviceServerAttrib().getName());
				//region 如果设备不存在，创建设备
				if (device == null) {
					device = new Device();
					device.setName(deviceInfo.getDeviceServerAttrib().getName());
					device.setType(deviceInfo.getDeviceShareAttrib().getSensorType());
					try {
						device.setTenantId(getCurrentUser().getTenantId());
						if (getCurrentUser().getAuthority() == Authority.CUSTOMER_USER) {
							if (device.getId() == null || device.getId().isNullUid() ||
									device.getCustomerId() == null || device.getCustomerId().isNullUid()) {
								throw new ThingsboardException("You don't have permission to perform this operation!",
										ThingsboardErrorCode.PERMISSION_DENIED);
							} else {
								checkCustomerId(device.getCustomerId());
							}
						}
						Device savedDevice = deviceService.saveDevice(device);
						device = savedDevice;
						actorService
								.onDeviceNameOrTypeUpdate(
										savedDevice.getTenantId(),
										savedDevice.getId(),
										savedDevice.getName(),
										savedDevice.getType());

						logEntityAction(savedDevice.getId(), savedDevice,
								savedDevice.getCustomerId(),
								device.getId() == null ? ActionType.ADDED : ActionType.UPDATED, null);

						if (device.getId() == null) {
							deviceStateService.onDeviceAdded(savedDevice);
						} else {
							deviceStateService.onDeviceUpdated(savedDevice);
						}

					} catch (ThingsboardException e) {
						e.printStackTrace();
						handleException(e);
					}
				}

				//endregion
				//update attrib
				//region 更新设备属性
				deviceInfo.setSystemDeviceId(device.getUuidId().toString());
				ObjectMapper mapper = new ObjectMapper();
				try {
					DeviceCredentials deviceCredentials = checkNotNull(deviceCredentialsService.findDeviceCredentialsByDeviceId(tenantId, device.getId()));
					deviceInfo.getDeviceShareAttrib().setToken(deviceCredentials.getCredentialsId());

					String t = mapper.writeValueAsString(deviceInfo.getDeviceServerAttrib());
					Map m = mapper.readValue(t, Map.class);
					List<AttributeKvEntry> attributes = new ArrayList<>();
					m.forEach((key, value) -> {
						if (value != null) {
							attributes.add(new BaseAttributeKvEntry(new StringDataEntry(key.toString(), value.toString()), System.currentTimeMillis()));
						}
					});
					EntityId entityId = EntityIdFactory.getByTypeAndUuid(EntityType.DEVICE, device.getUuidId().toString());
					saveAttributes(tenantId, entityId, "SERVER_SCOPE", attributes);

					List<AttributeKvEntry> attributesShare = new ArrayList<>();
					String tShare = mapper.writeValueAsString(deviceInfo.getDeviceShareAttrib());
					Map mShare = mapper.readValue(tShare, Map.class);
					mShare.forEach((key, value) -> {
						if (value != null) {
							attributesShare.add(new BaseAttributeKvEntry(new StringDataEntry(key.toString(), value.toString()), System.currentTimeMillis()));
						}
					});
					saveAttributes(tenantId, entityId, "SHARED_SCOPE", attributesShare);

					List<AttributeKvEntry> attributesClient = new ArrayList<>();
					String tClient = mapper.writeValueAsString(deviceInfo.getDeviceClientAttrib());
					Map mClient = mapper.readValue(tClient, Map.class);
					mClient.forEach((key, value) -> {
						if (value != null) {
							attributesClient.add(new BaseAttributeKvEntry(new StringDataEntry(key.toString(), value.toString()), System.currentTimeMillis()));
						}
					});
					saveAttributes(tenantId, entityId, "CLIENT_SCOPE", attributesClient);

				} catch (Exception e) {
					e.printStackTrace();
				}
				//endregion
				//region 添加关联

				EntityRelation relation = new EntityRelation(a.getId(), device.getId(), "Contains");
				try {
					relationService.saveRelation(getTenantId(), relation);
				} catch (ThingsboardException e) {
					handleException(e);
				}
				//endregion
			});

		} catch (Exception e) {
			handleException(e);
		}

		return null;
	}

	private DeferredResult<ResponseEntity> saveAttributes(TenantId srcTenantId,
														  EntityId entityIdSrc,
														  String scope,
														  List<AttributeKvEntry> attributes) throws ThingsboardException {

		SecurityUser user = getCurrentUser();
		return accessValidator.validateEntityAndCallback(getCurrentUser(), entityIdSrc, (result, tenantId, entityId) -> {
			tsSubService.saveAndNotify(tenantId, entityId, scope, attributes, new FutureCallback<Void>() {
				@Override
				public void onSuccess(@Nullable Void tmp) {
					logAttributesUpdated(user, entityId, scope, attributes, null);
					if (entityId.getEntityType() == EntityType.DEVICE) {
						DeviceId deviceId = new DeviceId(entityId.getId());
						DeviceAttributesEventNotificationMsg notificationMsg = DeviceAttributesEventNotificationMsg.onUpdate(
								user.getTenantId(), deviceId, scope, attributes);
						actorService.onMsg(new SendToClusterMsg(deviceId, notificationMsg));
					}
					result.setResult(new ResponseEntity(HttpStatus.OK));
				}

				@Override
				public void onFailure(Throwable t) {
					logAttributesUpdated(user, entityId, scope, attributes, t);
					AccessValidator.handleError(t, result, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			});
		});
	}

	private void logAttributesUpdated(SecurityUser user, EntityId entityId, String scope, List<AttributeKvEntry> attributes, Throwable e) {
		try {
			logEntityAction(user, (UUIDBased & EntityId) entityId, null, null, ActionType.ATTRIBUTES_UPDATED, toException(e),
					scope, attributes);
		} catch (ThingsboardException te) {
			log.warn("Failed to log attributes update", te);
		}
	}
}
