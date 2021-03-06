package org.thingsboard.server.dao.vassetattrkv;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.UUIDConverter;
import org.thingsboard.server.dao.model.sql.ComposeAssetAttrKV;
import org.thingsboard.server.dao.model.sql.VassetAttrKV;
import org.thingsboard.server.dao.sql.vassetattrkv.ComposeAssetAttrKVJpaRepository;
import org.thingsboard.server.dao.sql.vassetattrkv.VassetAttrKVRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service()
public class VassetAttrKVServiceImpl implements VassetAttrKVService {

	@Autowired
	private VassetAttrKVRepository vassetAttrKVRepository;
	@Autowired
	private ComposeAssetAttrKVJpaRepository composeAssetAttrKVJpaRepository;
	@Override
	public List<VassetAttrKV> getVassetAttrKV() {
		return vassetAttrKVRepository.findAll();
	}

	@Override
	public List<VassetAttrKV> findAll() {
		return vassetAttrKVRepository.findAll();
	}

	@Override
	public List<VassetAttrKV> findbytenantId(String tenantId)
	{
		return vassetAttrKVRepository.findbyTenantId(tenantId);
	}

	@Override
	public List<VassetAttrKV> findbyAttributeKey(String attributeKey,String tenantId)
	{
		return vassetAttrKVRepository.findbyAttributeKey(attributeKey,tenantId);
	}
	@Override
	public List<VassetAttrKV> findbyAttributeKeyAndValueLike(String attributeKey, String tenantId, String strV){
		return vassetAttrKVRepository.findbyAttributeKeyAndValueLinkWithTenantId(attributeKey,tenantId,strV);
	}
	@Override
	public List<VassetAttrKV> findbyAttributeValueLike(String tenantId, String strV)
	{
		return vassetAttrKVRepository.findbyAttributeValueLink(tenantId,strV);
	}

	@Override
	public List<VassetAttrKV> findbyAttributeKey(String attributeKey) {
        return vassetAttrKVRepository.findbyAttributeKey(attributeKey);
	}

	@Override
	public List<VassetAttrKV> findbyAttributeKeyAndValueLike(String attributeKey, String strV) {
		return vassetAttrKVRepository.findbyAttributeKeyAndValueLink(attributeKey,strV);
	}

	@Override
	public List<VassetAttrKV> findbyAttributeValueLike(String strV) {
		return vassetAttrKVRepository.findbyAttributeValueLink(strV);
	}

    @Override
	public List<ComposeAssetAttrKV> findByComposekey(String attrKey1,String attrKey2){
		List<ComposeAssetAttrKV> kvs = composeAssetAttrKVJpaRepository.findByComposekey(attrKey1,attrKey2);
		kvs = kvs.stream().peek(
				item-> item.setEntityId(UUIDConverter.fromString(item.getEntityId()).toString())
		).collect(Collectors.toList());
		return kvs;
	}

    @Override
	public List<ComposeAssetAttrKV> findByTenantIdAndComposekey(String tenantId, String attrKey1, String attrKey2){
//		return composeAssetAttrKVJpaRepository.findByTenantIdAndComposekey(tenantId,attrKey1,attrKey2);
		List<ComposeAssetAttrKV> kvs =  composeAssetAttrKVJpaRepository.findByTenantIdAndComposekey(tenantId,attrKey1,attrKey2);
		kvs = kvs.stream().peek(
				item-> item.setEntityId(UUIDConverter.fromString(item.getEntityId()).toString())
		).collect(Collectors.toList());
		return kvs;
	}

	@Override
	public List<ComposeAssetAttrKV> findByCustomerIdAndComposekey(String customerId, String attrKey1, String attrKey2) {
		List<ComposeAssetAttrKV> kvs =  composeAssetAttrKVJpaRepository.findByCustomerIdAndComposekey(customerId,attrKey1,attrKey2);
		kvs = kvs.stream().peek(
				item-> item.setEntityId(UUIDConverter.fromString(item.getEntityId()).toString())
		).collect(Collectors.toList());
		return kvs;
	}
}
